package de.yanwittmann.ow.lang;


import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.NomaiTextCompositor;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CliMain {

    public static void main(String[] args) throws IOException {
        final String[] allPossibleArgumentNames = new String[]{
                "-text", "-t", "--text", "--t",
                "-style", "-s", "--style", "--s",
                "-output", "-o", "--output", "--o",
                "-help", "-h", "--help", "--h"
        };
        final String text = getArgumentValue(args, allPossibleArgumentNames, null, "-text", "-t", "--text", "--t");
        final String style = getArgumentValue(args, allPossibleArgumentNames, "wall", "-style", "-s", "--style", "--s");
        final String output = getArgumentValue(args, allPossibleArgumentNames, new File("generated-nomai-lang-" + System.currentTimeMillis() + ".png").getAbsolutePath(), "-output", "-o", "--output", "--o");
        final boolean help = text == null || isArgumentPresent(args, "-help", "-h", "--help", "--h");

        if (help) {
            System.out.println("Usage: java -jar nomai-language-cli.jar [options]");
            System.out.println("Options:");
            System.out.println("  -text, -t, --text, --t <text>     The text to render.");
            System.out.println("  -style, -s, --style, --s <style>  The style of the text. Either a file path or one of: wall 1, wall 2, wall 3, cliff, quantum, space, black, transparent");
            System.out.println("  -output, -o, --output, --o <path> The path to the output file. If not specified, a random file name will be generated.");
            System.out.println("  -help, -h, --help, --h            Show this help message.");

            return;
        }

        final WrittenNomaiConverter converter = new WrittenNomaiConverter();
        converter.setTokenizer(new WrittenNomaiTextTokenizer(
                WrittenNomaiTextTokenizer.class,
                "/ow-lang/cmudict.dict",
                "/ow-lang/cmudict-to-ow.txt"
        ));
        converter.setLineGenerator(new LetterToLineConverter());
        converter.setTransformAlongCurveProvider(WrittenNomaiConverter::lengthDependantUpwardsSpiralBezierCurveProvider);

        final RenderResult renderResult = renderText(converter, text, style);

        final File outputFile = new File(output).getAbsoluteFile();
        makeParentDirs(outputFile);
        ImageIO.write(renderResult.image, "png", outputFile);

        final JSONArray generatedFiles = parseGeneratedFiles(outputFile.getParentFile());
        generatedFiles.put(new JSONObject()
                .put("text", text)
                .put("style", style)
                .put("explanation", renderResult.explanationText)
                .put("imageFile", outputFile.getName())
        );
        FileUtils.writeStringToFile(new File(outputFile.getParentFile(), "generated-files.json"), generatedFiles.toString(2), StandardCharsets.UTF_8);

        System.out.println("\n\n" +
                "Wrote image to: " + outputFile.getAbsolutePath());
        System.out.println("          Text: " + text);
        System.out.println("         Style: " + style);
        System.out.println("   Explanation: " + renderResult.explanationText);
        System.exit(0);
    }

    private static void makeParentDirs(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    private static JSONArray parseGeneratedFiles(File outputDirectory) {
        try {
            final File generatedImagesJsonFile = new File(outputDirectory, "generated-files.json");
            makeParentDirs(generatedImagesJsonFile);
            return generatedImagesJsonFile.exists() ? new JSONArray(FileUtils.readFileToString(generatedImagesJsonFile, StandardCharsets.UTF_8)) : new JSONArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getArgumentValue(String[] args, String[] allPossibleArgumentNames, String defaultValue, String... argumentName) {
        for (int i = 0; i < args.length; i++) {
            for (String name : argumentName) {
                if (args[i].equalsIgnoreCase(name) && i + 1 < args.length) {
                    // collect until end or next argument
                    final StringJoiner joiner = new StringJoiner(" ");
                    for (int j = i + 1; j < args.length; j++) {
                        boolean isArgument = false;
                        for (String possibleArgumentName : allPossibleArgumentNames) {
                            if (args[j].equalsIgnoreCase(possibleArgumentName)) {
                                isArgument = true;
                                break;
                            }
                        }
                        if (isArgument) break;
                        joiner.add(args[j]);
                    }
                    return joiner.toString();
                }
            }
        }
        return defaultValue;
    }

    private static boolean isArgumentPresent(String[] args, String... argumentName) {
        for (int i = 0; i < args.length; i++) {
            for (String name : argumentName) {
                if (args[i].equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static RenderResult renderText(WrittenNomaiConverter converter, String normalText, String style) {
        final Random random = new Random();

        if (style == null) {
            throw new IllegalArgumentException("Style must not be null");
        }

        final BufferedImage backgroundImage;
        if (new File(style).getAbsoluteFile().exists()) {
            try {
                backgroundImage = ImageIO.read(new File(style).getAbsoluteFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
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
                    backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL_HANGING_CITY;
                    break;
                default:
                    System.out.println("Unknown style [" + style + "], using default [wall]");
                    backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL_HANGING_CITY;
            }
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

        final BufferedImage baseRenderedImage = renderer.renderShapes(8000, 8000, 2, new Point2D.Double(4000, 7800));
        final BufferedImage croppedRenderedImage = renderer.cropImageToTarget(baseRenderedImage, 70);

        final NomaiTextCompositor nomaiTextCompositor = new NomaiTextCompositor();

        final BufferedImage blueStyledImage = nomaiTextCompositor.styleNomaiTextLightBlue(croppedRenderedImage);

        final BufferedImage resizedStyledImage = LanguageRenderer.resizeImageMaintainAspectRatio(blueStyledImage, backgroundImage.getWidth() - backgroundImagePadding * 2, backgroundImage.getHeight() - backgroundImagePadding * 2);

        final BufferedImage styledTextWithBackground = nomaiTextCompositor.overlayNomaiTextWithBackground(resizedStyledImage, backgroundImage);

        final String explanationText = snippetWordsTrees.values().stream()
                .map(e -> e.stream().map(l -> l.stream().map(WrittenNomaiTextLetter::getToken).collect(Collectors.joining(" "))).collect(Collectors.joining(" | ")))
                .collect(Collectors.joining(" ||| "));


        return new RenderResult(styledTextWithBackground, explanationText);
    }

    private static class RenderResult {
        public final BufferedImage image;
        public final String explanationText;

        public RenderResult(BufferedImage image, String explanationText) {
            this.image = image;
            this.explanationText = explanationText;
        }
    }
}