package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.NomaiTextCompositor;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurve;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurveCoordinateSystem;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

class WrittenNomaiTextTokenizerTest {
    private static final Logger LOG = LogManager.getLogger(WrittenNomaiTextTokenizerTest.class);

    @Test
    public void test() throws IOException {
        final WrittenNomaiTextTokenizer tokenizer = new WrittenNomaiTextTokenizer(
                new File("nomai-language-core/ src/main/resources/ow-lang/cmudict.dict"),
                new File("nomai-language-core/ src/main/resources/ow-lang/cmudict-to-ow.txt")
        );

        final List<List<String>> tokens = tokenizer.tokenizeToStringTokens("I have 3287 Apples, but I wish I had 3288!");
        final List<List<WrittenNomaiTextLetter>> words = tokenizer.convertStringTokensToLetters(tokens);
        final WrittenNomaiBranchingLetterNode tree = WrittenNomaiBranchingLetterNode.fromSentence(words);

        LOG.info("Tokenized: {}", tokens);
        LOG.info("Symbol combinations: {}", words);
        LOG.info("Converted branches:{}", tree);
    }

    private static void manuallyTest() throws IOException {
        final WrittenNomaiTextTokenizer tokenizer = new WrittenNomaiTextTokenizer(
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict.dict"),
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict-to-ow.txt")
        );

        final List<List<String>> tokens = tokenizer.tokenizeToStringTokens("I have 3287 Apples but I wish I had 3288. Do you wish that too?");
        final List<List<WrittenNomaiTextLetter>> words = tokenizer.convertStringTokensToLetters(tokens);
        final WrittenNomaiBranchingLetterNode tree = WrittenNomaiBranchingLetterNode.fromSentence(words);
        LOG.info("Tokenized: {}", tokens);
        LOG.info("Symbol combinations: {}", words);
        LOG.info("Converted branches:{}", tree);
        LOG.info("Depth: {}", tree.getDepth());

        final Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider = letterShapes -> {
            final BezierCurve curve = new BezierCurve();

            // curve.addControlPoint(new Point2D.Double(446, 709), new Point2D.Double(270, 215), new Point2D.Double(646, 1), new Point2D.Double(896, 452), new Point2D.Double(591, 482));
            curve.addControlPoint(new Point2D.Double(365, 1012), new Point2D.Double(43, 440), new Point2D.Double(369, 45), new Point2D.Double(821, 172), new Point2D.Double(961, 593), new Point2D.Double(606, 741));
            curve.setFirstControlPointAsOrigin();
            curve.getTransformation().setScale(2);

            curve.recalculateAbsoluteControlPoints();
            return curve.getCoordinateSystem();
        };

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(250, 1050));
        renderer.setSize(1000, 1100);
        renderer.setVisible(true);
        renderer.setLocationRelativeTo(null);

        final LetterToLineConverter generator = new LetterToLineConverter();

        if (true) {
            new Thread(() -> {
                while (true) {
                    int seed = (int) (Math.random() * 1000000);
                    System.out.println("Seed: " + seed);
                    final List<Object> shapes = generator.generateShapes(new Random(seed), tree, transformAlongCurveProvider);
                    renderer.setShapes(shapes);
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            final List<Object> shapes = generator.generateShapes(new Random(952764), tree, transformAlongCurveProvider);
            renderer.setShapes(shapes);
        }

    }

    private static void uiTest() throws IOException {
        final WrittenNomaiConverter converter = new WrittenNomaiConverter();
        converter.setTokenizer(new WrittenNomaiTextTokenizer(
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict.dict"),
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict-to-ow.txt")
        ));
        converter.setLineGenerator(new LetterToLineConverter());

        converter.setTransformAlongCurveProvider(WrittenNomaiConverter::sizeDependantBezierCurveProvider);


        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(250, 1000));
        renderer.setSize(1000, 1050);
        renderer.setVisible(true);
        renderer.setLocationRelativeTo(null);
        renderer.setCropImage(true);


        final JFrame textInputFrame = new JFrame("Text input");
        textInputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textInputFrame.setSize(500, 100);
        textInputFrame.setLocationRelativeTo(null);
        textInputFrame.setLayout(new BorderLayout());

        final JTextField textInput = new JTextField();
        textInputFrame.add(textInput, BorderLayout.CENTER);
        textInput.setText("I have 3287 Apples, but I wish I had 3288!");
        AtomicReference<String> lastText = new AtomicReference<>("empty");

        // add multiple checkboxes to the south contained in a JPanel
        final JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout());
        textInputFrame.add(southPanel, BorderLayout.SOUTH);


        final JCheckBox randomizeSeed = new JCheckBox("Randomize seed");
        southPanel.add(randomizeSeed);

        final JCheckBox continuouslyRegenerate = new JCheckBox("Continuously regenerate");
        southPanel.add(continuouslyRegenerate);

        textInputFrame.setVisible(true);


        new Thread(() -> {
            while (true) {
                if (!lastText.get().equals(textInput.getText()) || continuouslyRegenerate.isSelected()) {
                    try {
                        final Random random;
                        if (randomizeSeed.isSelected()) {
                            int seed = (int) (Math.random() * 1000000);
                            random = new Random(seed);
                            System.out.println("Seed: " + seed);
                        } else {
                            random = new Random(143089);
                        }

                        lastText.set(textInput.getText());

                        final WrittenNomaiBranchingLetterNode tree = converter.convertTextToNodeTree(lastText.get());
                        final List<Object> shapes = converter.convertNodeTreeToDrawables(random, 10, tree);

                        {
                            final List<List<String>> tokens = converter.getTokenizer().tokenizeToStringTokens(lastText.get());
                            final List<List<WrittenNomaiTextLetter>> words = converter.getTokenizer().convertStringTokensToLetters(tokens);

                            LOG.info("Words: {}", words.stream().map(l -> l.stream().map(WrittenNomaiTextLetter::getToken).collect(Collectors.joining(" "))).collect(Collectors.joining(" . ")));
                        }

                        renderer.setShapes(shapes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void toFileTest() throws IOException {
        final String normalText = "I have 3287 Apples but I wish I had 3288.";
        final Random random = new Random();
        final File baseSaveDirectory = new File("");
        final BufferedImage backgroundImage = NomaiTextCompositor.BACKGROUND_NOMAI_WALL;
        final int backgroundImagePadding = 50;

        if (!baseSaveDirectory.exists()) {
            baseSaveDirectory.mkdirs();
        }

        if (backgroundImage == null) {
            throw new IllegalStateException("Background image is null");
        }

        final WrittenNomaiConverter converter = new WrittenNomaiConverter();
        converter.setTokenizer(new WrittenNomaiTextTokenizer(
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict.dict"),
                new File("nomai-language-core/src/main/resources/ow-lang/cmudict-to-ow.txt")
        ));
        converter.setLineGenerator(new LetterToLineConverter());

        converter.setTransformAlongCurveProvider(WrittenNomaiConverter::sizeDependantBezierCurveProvider);

        final WrittenNomaiBranchingLetterNode tree = converter.convertTextToNodeTree(normalText);
        final List<Object> shapes = converter.convertNodeTreeToDrawables(random, 10, tree);

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(0, 0));
        renderer.setLineThickness(9);
        renderer.setDotRadius(12);

        renderer.setShapes(shapes);

        final BufferedImage baseRenderedImage = renderer.renderShapes(8000, 8000, 2, new Point2D.Double(4000, 4000));
        final BufferedImage croppedRenderedImage = renderer.cropImageToTarget(baseRenderedImage, 70);

        writeToFile(croppedRenderedImage, new File(baseSaveDirectory, "nomai-result.png"));

        if (true) {
            final NomaiTextCompositor nomaiTextCompositor = new NomaiTextCompositor();

            final BufferedImage blueStyledImage = nomaiTextCompositor.styleNomaiTextLightBlue(croppedRenderedImage);
            writeToFile(blueStyledImage, new File(baseSaveDirectory, "nomai-result-blue.png"));

            final BufferedImage resizedStyledImage = LanguageRenderer.resizeImageMaintainAspectRatio(blueStyledImage, backgroundImage.getWidth() - backgroundImagePadding * 2, backgroundImage.getHeight() - backgroundImagePadding * 2);

            final BufferedImage styledTextWithBackground = nomaiTextCompositor.overlayNomaiTextWithBackground(resizedStyledImage, backgroundImage);
            writeToFile(styledTextWithBackground, new File(baseSaveDirectory, "nomai-result-blue-background.png"));
        }
    }

    private static void writeToFile(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // manuallyTest();
        // uiTest();
        toFileTest();
    }

}