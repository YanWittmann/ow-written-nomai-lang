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

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    private static WrittenNomaiConverter converter;

    public static void main(String[] args) throws IOException, AuthenticationException, InterruptedException {
        converter = new WrittenNomaiConverter();
        converter.setTokenizer(new WrittenNomaiTextTokenizer(
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict.dict"),
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict-to-ow.txt")
        ));
        converter.setLineGenerator(new LetterToLineConverter());
        converter.setTransformAlongCurveProvider(WrittenNomaiConverter::sizeDependantBezierCurveProvider);

        for (File file : getTempFile("nomai-language", "png").getParentFile().listFiles((dir, name) -> name.startsWith("written_nomai_") && name.endsWith(".png"))) {
            if (!file.delete()) {
                System.err.println("Could not delete file: " + file.getAbsolutePath());
            } else {
                System.out.println("Deleted file: " + file.getAbsolutePath());
            }
        }

        runBot();

        // renderText("This is a text in a language that I cannot understand", "");
    }

    private static void runBot() throws IOException, AuthenticationException, InterruptedException {
        final String CLIENT_ID = "pCL491RYILLmhPRxe5cgrg";
        final String CLIENT_SECRET = "zGsaXWyDy-AdSFf3gtsXqjhHBVV0Zw";
        final String USERNAME = "_SKYBALL_";
        final String PASSWORD = "Yaildheihr3844u58i4htiohnl.efhnosihfihtoethp9348heoiahtp4puihtpe4uihopejfpirjti84hz8ioeh4söhte47hztoiwahrä3äwopajto3htwpath8z3owuz43w7ugquiZFDIZGuoG§T/GHOUGou3gtubthowugtouehtbsjgubh8H";

        Reddit4J client = Reddit4J.rateLimited().setUsername(USERNAME).setPassword(PASSWORD)
                .setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET)
                .setUserAgent(new UserAgentBuilder().appname("ow-nomai-lang").author("_SKYBALL_").version("0.1"));
        client.connect();

        final RedditApiAccess customClient = new RedditApiAccess(client.getToken()) {
            @Override
            protected String getAccessToken() {
                return client.getToken();
            }
        };


        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Fetching comments");
                    for (RedditComment comment : client.getCommentsForPost("skyball_personal", "15jkyrq").submit()) {
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
                    throw new RuntimeException(e);
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
                if (reply.getData().getBody().contains("as written nomai")) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            return;
        }

        // comment body may look like:
        //   some text
        //   text: <text>
        //   style: <style>
        //   some more text
        final String commentBody = comment.getBody();

        String text = "";
        String style = "";

        final String[] lines = commentBody.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("text: ")) {
                text = line.substring(6);
            } else if (line.startsWith("style: ")) {
                style = line.substring(7);
            }
        }

        if (text.isEmpty()) {
            return;
        }

        System.out.println("Processing comment with [" + style + "]: " + text);

        final RenderResult renderResult = renderText(text, style);

        customClient.commentWithImageAsset(comment.getName(), "\"" + text + "\" as written nomai:",
                "It says: " + renderResult.explanationText,
                null,
                renderResult.outFile.getAbsolutePath(),
                "image/png"
        );
    }

    private static RenderResult renderText(String normalText, String style) throws IOException {
        final Random random = new Random();
        final BufferedImage backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL;
        final int backgroundImagePadding = 50;

        if (backgroundImage == null) {
            throw new IllegalStateException("Background image is null");
        }


        // final WrittenNomaiBranchingLetterNode tree = converter.convertTextToNodeTree(normalText);
        // the above line is equivalent to the following lines:
        final List<List<String>> tokens = converter.getTokenizer().tokenizeToStringTokens(normalText);
        final List<List<WrittenNomaiTextLetter>> words = converter.getTokenizer().convertStringTokensToLetters(tokens);
        final WrittenNomaiBranchingLetterNode tree = WrittenNomaiBranchingLetterNode.fromSentence(words);

        final List<Object> shapes = converter.convertNodeTreeToDrawables(random, 10, tree).getDrawables();

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(0, 0));
        renderer.setLineThickness(9);
        renderer.setDotRadius(12);

        renderer.setShapes(shapes);

        final BufferedImage baseRenderedImage = renderer.renderShapes(8000, 8000, 2, new Point2D.Double(4000, 4000));
        final BufferedImage croppedRenderedImage = renderer.cropImageToTarget(baseRenderedImage, 70);

        final NomaiTextCompositor nomaiTextCompositor = new NomaiTextCompositor();

        final BufferedImage blueStyledImage = nomaiTextCompositor.styleNomaiTextLightBlue(croppedRenderedImage);

        final BufferedImage resizedStyledImage = LanguageRenderer.resizeImageMaintainAspectRatio(blueStyledImage, backgroundImage.getWidth() - backgroundImagePadding * 2, backgroundImage.getHeight() - backgroundImagePadding * 2);

        final BufferedImage styledTextWithBackground = nomaiTextCompositor.overlayNomaiTextWithBackground(resizedStyledImage, backgroundImage);

        final File outFile = getTempFile("written_nomai_", ".png");
        ImageIO.write(styledTextWithBackground, "png", outFile);


        final String explanationText = words.stream().map(l -> l.stream().map(WrittenNomaiTextLetter::getToken).collect(Collectors.joining(" "))).collect(Collectors.joining(" | "));

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

    private static File getTempFile(String prefix, String suffix) {
        try {
            final File tempFile = File.createTempFile(prefix, suffix);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void commentsTest(Reddit4J client, RedditApiAccess customClient) throws IOException, AuthenticationException, InterruptedException {
        for (RedditComment comment : client.getCommentsForPost("skyball_personal", "15jkyrq").submit()) {
            System.out.println(comment.getBody());
            RedditData<RedditListing<RedditData<RedditComment>>> replies = comment.getReplies();
            // check if reply with body "test" exists

            boolean found = false;
            if (replies != null && replies.getData() != null && replies.getData().getChildren() != null) {
                for (RedditData<RedditComment> reply : replies.getData().getChildren()) {
                    if (reply.getData().getBody().contains("test")) {
                        System.out.println("Found reply with body \"test\": " + reply.getData().getBody());
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                System.out.println("No reply with body \"test\" found. Replying...");
                customClient.commentWithImageAsset(comment.getName(), "Image below:",
                        "After image text",
                        "Caption",
                        "C:/Users/yan20/Downloads/nomai_1_small.png",
                        "image/png"
                );

                break;
            }
        }
    }

    private static void customClientTest() throws IOException {
        final String CLIENT_ID = "pCL491RYILLmhPRxe5cgrg";
        final String CLIENT_SECRET = "zGsaXWyDy-AdSFf3gtsXqjhHBVV0Zw";
        final String USERNAME = "_SKYBALL_";
        final String PASSWORD = "Yaildheihr3844u58i4htiohnl.efhnosihfihtoethp9348heoiahtp4puihtpe4uihopejfpirjti84hz8ioeh4söhte47hztoiwahrä3äwopajto3htwpath8z3owuz43w7ugquiZFDIZGuoG§T/GHOUGou3gtubthowugtouehtbsjgubh8H";

        final RedditApiAccess api = new RedditApiAccess(CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD);

        final String postId = api.extractPostId("https://www.reddit.com/user/_SKYBALL_/comments/15jjekb/api_playground_post/");
        System.out.println(postId);
        final String subreddit = api.findSubredditForPost(postId);
        //final String subreddit = "u__SKYBALL_";
        System.out.println(subreddit);

        api.postComment(postId, "Test comment");
    }
}