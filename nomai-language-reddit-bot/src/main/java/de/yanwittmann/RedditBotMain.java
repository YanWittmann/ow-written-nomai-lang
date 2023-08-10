package de.yanwittmann;


import de.yanwittmann.ow.lang.WrittenNomaiConverter;
import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.NomaiTextCompositor;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import de.yanwittmann.reddit.RedditApiAccess;
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.objects.RedditData;
import masecla.reddit4j.objects.RedditListing;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedditBotMain {

    private static WrittenNomaiConverter converter;

    // input parameters
    private final static File GENERATED_IMAGES_DIR = new File("");
    /**
     * The post url must end in <code>.json?sort=new</code> to get the comments in the correct order.
     */
    private final static String WATCH_POST_URL = "https://www.reddit.com/r/outerwilds/comments/15m8qhc/written_nomai_translator_bot" + ".json?sort=new";

    private final static String REDDIT_CLIENT_ID = "";
    private final static String REDDIT_CLIENT_SECRET = "";
    private final static String REDDIT_USERNAME = "";
    private final static String REDDIT_PASSWORD = "";


    private final static File GENERATED_IMAGES_JSON_FILE = new File(GENERATED_IMAGES_DIR, "generated-files.json");
    private final static JSONArray GENERATED_FILES = parseGeneratedFiles();

    private static JSONArray parseGeneratedFiles() {
        try {
            makeParentDirs(GENERATED_IMAGES_JSON_FILE);
            return GENERATED_IMAGES_JSON_FILE.exists() ? new JSONArray(FileUtils.readFileToString(GENERATED_IMAGES_JSON_FILE, StandardCharsets.UTF_8)) : new JSONArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveGeneratedFiles() {
        try {
            makeParentDirs(GENERATED_IMAGES_JSON_FILE);
            FileUtils.writeStringToFile(GENERATED_IMAGES_JSON_FILE, GENERATED_FILES.toString(2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void makeParentDirs(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public static void main(String[] args) throws IOException, AuthenticationException, InterruptedException {
        converter = new WrittenNomaiConverter();
        converter.setTokenizer(new WrittenNomaiTextTokenizer(
                WrittenNomaiTextTokenizer.class,
                "/ow-lang/cmudict.dict",
                "/ow-lang/cmudict-to-ow.txt"
        ));
        converter.setLineGenerator(new LetterToLineConverter());
        converter.setTransformAlongCurveProvider(WrittenNomaiConverter::lengthDependantUpwardsSpiralBezierCurveProvider);

        runBot();

        // renderText("This is a text in a language that I cannot understand", "");
    }

    /**
     * Set containing the processed IDs in the current run. Used because the indirect way of querying the comments via
     * the .json endpoint sometimes lags behind the actual comments and does not yet contain the response by this bot.
     */
    private final static Set<String> PROCESSED_IDS_IN_THIS_RUN = new HashSet<>();

    private static void runBot() throws IOException, AuthenticationException, InterruptedException {
        Reddit4J client = Reddit4J.rateLimited().setUsername(REDDIT_USERNAME).setPassword(REDDIT_PASSWORD)
                .setClientId(REDDIT_CLIENT_ID).setClientSecret(REDDIT_CLIENT_SECRET)
                .setUserAgent(new UserAgentBuilder().appname("ow-nomai-lang").author(REDDIT_USERNAME).version("0.1"));
        client.connect();

        final RedditApiAccess customClient = new RedditApiAccess(client.getToken()) {
            @Override
            public String getAccessToken() {
                return client.getToken();
            }
        };


        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Fetching comments");

                    final Map<String, String> topLevelComments = fetchAllComments(
                            WATCH_POST_URL,
                            true
                    );
                    for (Map.Entry<String, String> commentEntry : topLevelComments.entrySet()) {
                        try {
                            if (PROCESSED_IDS_IN_THIS_RUN.contains(commentEntry.getKey())) {
                                continue;
                            }
                            handleComment(customClient, commentEntry.getKey(), commentEntry.getValue());
                            PROCESSED_IDS_IN_THIS_RUN.add(commentEntry.getKey());
                        } catch (Exception ignored) {
                        }
                    }

                    // reddit API only returns the first few comments of the post and I was unable to find a way to get
                    // all comments using the API. Therefore, we have to use the .json endpoint to get all comments.
                    // The below code uses the actual API.

                    /*final String subreddit = "outerwilds";
                    final String post = "15m8qhc";

                    final RedditCommentListingEndpointRequest request = client.getCommentsForPost(subreddit, post);
                    final List<RedditComment> commentsResponse = request.submit();

                    for (RedditComment comment : commentsResponse) {
                        try {
                            handleComment(customClient, comment);
                        } catch (IOException e) {
                            System.err.println("Error while handling comment [" + comment.getName() + "]: " + comment.getBody());
                            e.printStackTrace();
                        }
                    }
                } catch (IOException | AuthenticationException e) {
                    System.out.println("Error while fetching comments");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);*/
                } catch (Exception e) {
                    System.out.println("Unknown error while fetching comments");
                    e.printStackTrace();
                } finally {
                    // Reschedule the task to run again after a delay of 20 seconds
                    executor.schedule(this, 20, TimeUnit.SECONDS);
                }
            }
        };
        executor.schedule(task, 0, TimeUnit.SECONDS);
    }

    private static Map<String, String> fetchAllComments(String urlString, boolean onlyWithoutReplies) throws IOException {
        final URL url = new URL(urlString);
        final URLConnection hc = url.openConnection();
        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        final String responseString = IOUtils.toString(hc.getInputStream(), StandardCharsets.UTF_8);
        final JSONArray response = new JSONArray(responseString);

        if (response.isEmpty()) {
            System.out.println("Response is empty, no comments found");
            return new LinkedHashMap<>();
        }

        final Map<String, String> comments = new LinkedHashMap<>();

        for (int i = 0; i < response.length(); i++) {
            final JSONObject commentListing = response.getJSONObject(i);
            final String kind = commentListing.getString("kind");
            if (!kind.equals("Listing")) {
                System.out.println("Unexpected kind: " + kind);
                continue;
            }

            final JSONObject data = commentListing.getJSONObject("data");
            final JSONArray children = data.getJSONArray("children");

            for (int j = 0; j < children.length(); j++) {
                final JSONObject comment = children.getJSONObject(j);
                final String commentKind = comment.getString("kind");
                if (!commentKind.equals("t1")) { // t1 = comment, t3 would be a post
                    continue;
                }

                final JSONObject commentData = comment.getJSONObject("data");
                final String body = commentData.getString("body");
                final String name = commentData.getString("name");

                // check if it has any replies
                if (onlyWithoutReplies) {
                    final JSONObject replies = commentData.optJSONObject("replies");
                    if (replies == null || replies.isNull("data")) {
                        comments.put(name, body);
                    }
                } else {
                    comments.put(name, body);
                }
            }
        }

        return comments;
    }

    /**
     * Uses the reddit4j API to handle a comment.<br>
     * This is unused, as the API does not return all comments.
     */
    private static void handleComment(RedditApiAccess customClient, RedditComment comment) throws IOException {
        final RedditData<RedditListing<RedditData<RedditComment>>> replies = comment.getReplies();

        boolean found = false;
        if (replies != null && replies.getData() != null && replies.getData().getChildren() != null) {
            for (RedditData<RedditComment> reply : replies.getData().getChildren()) {
                if (reply.getData().getBody() == null) {
                    // System.out.println("Reply body is null, this is most likely a comment with an image. Assume found.");
                    found = true;
                    break;
                }
                if (reply.getData().getBody().contains("as written nomai") || reply.getData().getBody().contains("Bot:")) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            return;
        }

        final String commentBody = comment.getBody();

        handleComment(customClient, comment.getName(), commentBody);
    }

    private static void handleComment(RedditApiAccess customClient, String commentId, String commentContent) throws IOException {
        // comment body may look like:
        //   some text
        //   text: <text>
        //   style: <style>
        //   some more text

        if (commentContent == null) {
            System.out.println("Comment body is null on comment [" + commentId + "]");
            // customClient.commentOnCommentWithText(comment.getName(), "Bot: For some reason, I cannot read the contents of your post. This could be because of some formatting you applied whilst posting. Mind trying again?");
            return;
        }

        String text = "";
        String style = "";

        final String[] lines = commentContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("text: ") || line.startsWith("Text: ")) {
                text = line.substring(6);
            } else if (line.startsWith("style: ") || line.startsWith("Style: ")) {
                style = line.substring(7);
            }
        }

        if (text.isEmpty()) {
            return;
        }
        if (text.length() > 120) {
            text = text.substring(0, 120);
        }
        if (style.isEmpty()) {
            style = "wall";
        }

        System.out.println("Processing comment with [" + style + "]: " + text + "\nFull: " + commentContent);

        final RenderResult renderResult = renderText(text, style);

        customClient.commentWithImageAsset(commentId, "\"" + text + "\" as written nomai:",
                "It says: " + renderResult.explanationText,
                null,
                renderResult.outFile.getAbsolutePath(),
                "image/png"
        );
    }

    private static RenderResult renderText(String normalText, String style) throws IOException {
        final Random random = new Random();

        final BufferedImage backgroundImage;
        switch (style) {
            case "space":
            case "stars":
            case "blue":
                backgroundImage = NomaiTextCompositor.BACKGROUND_SPACE;
                break;
            case "black":
                backgroundImage = NomaiTextCompositor.BACKGROUND_BLACK;
                break;
            case "transparent":
                backgroundImage = NomaiTextCompositor.BACKGROUND_TRANSPARENT;
                break;
            case "solanum":
            case "quantum":
            case "moon":
                backgroundImage = NomaiTextCompositor.BACKGROUND_SOLANUM;
                break;
            case "stone":
            case "cliff":
                backgroundImage = NomaiTextCompositor.BACKGROUND_STONE_WALL;
                break;
            case "lamp":
            case "wall lamp":
            case "wall_lamp":
            case "wall 2":
                backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL_2_LAMP;
                break;
            case "wall 3":
                backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL;
                break;
            case "nomai_wall":
            case "scroll_wall":
            case "scroll":
            case "wall":
            case "wall 1":
            default:
                backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL_HANGING_CITY;
        }
        final int backgroundImagePadding = 50;

        if (backgroundImage == null) {
            throw new IllegalStateException("Background image is null");
        }


        final List<String> snippets = converter.getTokenizer().convertTextToBranchSnippets(normalText, false).stream()
                .distinct().limit(3).collect(Collectors.toList());

        final Map<String, WrittenNomaiBranchingLetterNode> snippetTrees = new LinkedHashMap<>();
        final Map<String, List<List<WrittenNomaiTextLetter>>> snippetWordsTrees = new LinkedHashMap<>();
        final Map<String, WrittenNomaiConverter.DrawablesResult> snippetShapes = new LinkedHashMap<>();

        for (String snippet : snippets) {
            final List<List<String>> tokens = converter.getTokenizer().tokenizeToStringTokens(snippet);
            final List<List<WrittenNomaiTextLetter>> words = converter.getTokenizer().convertStringTokensToLetters(tokens);
            final WrittenNomaiBranchingLetterNode tree = WrittenNomaiBranchingLetterNode.fromSentence(words);
            final WrittenNomaiConverter.DrawablesResult shapes = converter.convertNodeTreeToDrawables(random, 10, tree);

            snippetTrees.put(snippet, tree);
            snippetShapes.put(snippet, shapes);
            snippetWordsTrees.put(snippet, words);
        }

        final List<Object> combinedShapes = converter.combineMultipleDrawableBranches(snippetShapes.values());


        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(0, 0));
        renderer.setLineThickness(9);
        renderer.setDotRadius(12);

        renderer.setShapes(combinedShapes);

        final BufferedImage baseRenderedImage = renderer.renderShapes(8000, 8000, 2, new Point2D.Double(4000, 8000));
        final BufferedImage croppedRenderedImage = renderer.cropImageToTarget(baseRenderedImage, 70);

        final NomaiTextCompositor nomaiTextCompositor = new NomaiTextCompositor();

        final BufferedImage blueStyledImage = nomaiTextCompositor.styleNomaiTextLightBlue(croppedRenderedImage);

        final BufferedImage resizedStyledImage = LanguageRenderer.resizeImageMaintainAspectRatio(blueStyledImage, backgroundImage.getWidth() - backgroundImagePadding * 2, backgroundImage.getHeight() - backgroundImagePadding * 2);

        final BufferedImage styledTextWithBackground = nomaiTextCompositor.overlayNomaiTextWithBackground(resizedStyledImage, backgroundImage);

        final File outFile = getOutFile("written_nomai_", ".png");
        ImageIO.write(styledTextWithBackground, "png", outFile);

        final String explanationText = snippetWordsTrees.values().stream()
                .map(e -> e.stream().map(l -> l.stream().map(WrittenNomaiTextLetter::getToken).collect(Collectors.joining(" "))).collect(Collectors.joining(" | ")))
                .collect(Collectors.joining(" ||| "));

        GENERATED_FILES.put(new JSONObject()
                .put("text", normalText)
                .put("style", style)
                .put("snippets", new JSONArray(snippets))
                .put("explanation", explanationText)
                .put("imageFile", outFile.getAbsolutePath())
        );
        saveGeneratedFiles();


        return new RenderResult(outFile, explanationText);
    }

    private static class RenderResult {
        public final File outFile;
        public final String explanationText;

        public RenderResult(File outFile, String explanationText) {
            this.outFile = outFile;
            this.explanationText = explanationText;

            System.out.println(explanationText + " in " + outFile.getAbsolutePath());
        }
    }

    private static File getOutFile(String prefix, String suffix) {
        try {
            return new File(GENERATED_IMAGES_DIR, prefix + System.currentTimeMillis() + suffix).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}