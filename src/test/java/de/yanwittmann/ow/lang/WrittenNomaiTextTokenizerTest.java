package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurve;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurveCoordinateSystem;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

class WrittenNomaiTextTokenizerTest {
    private static final Logger LOG = LogManager.getLogger(WrittenNomaiTextTokenizerTest.class);

    @Test
    public void test() throws IOException {
        final WrittenNomaiTextTokenizer tokenizer = new WrittenNomaiTextTokenizer(
                new File("src/main/resources/ow-lang/cmudict.dict"),
                new File("src/main/resources/ow-lang/cmudict-to-ow.txt")
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
                new File("src/main/resources/ow-lang/cmudict.dict"),
                new File("src/main/resources/ow-lang/cmudict-to-ow.txt")
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

    private static void converterTest() throws IOException {
        final WrittenNomaiConverter converter = new WrittenNomaiConverter();
        converter.setTokenizer(new WrittenNomaiTextTokenizer(
                new File("src/main/resources/ow-lang/cmudict.dict"),
                new File("src/main/resources/ow-lang/cmudict-to-ow.txt")
        ));
        converter.setLineGenerator(new LetterToLineConverter());

        final Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider = letterShapes -> {
            final double largestX = letterShapes.stream().mapToDouble(shape -> shape.getTransformation().getOffsetPosition().getX()).max().orElse(0);

            final BezierCurve curve = new BezierCurve();
            // curve.addControlPoint(new Point2D.Double(446, 709), new Point2D.Double(270, 215), new Point2D.Double(646, 1), new Point2D.Double(896, 452), new Point2D.Double(591, 482));
            curve.addControlPoint(new Point2D.Double(365, 1012), new Point2D.Double(43, 440), new Point2D.Double(369, 45), new Point2D.Double(821, 172), new Point2D.Double(961, 593), new Point2D.Double(606, 741));
            curve.setFirstControlPointAsOrigin();

            curve.getTransformation().setScale(0.5);
            curve.recalculateAbsoluteControlPoints();

            final BezierCurveCoordinateSystem coordinateSystem = curve.getCoordinateSystem();

            // while the largestX does not fit, increase the scale by 0.1
            while (curve.calculateLengthOfCurveAt(1) > largestX) {
                curve.getTransformation().setScale(curve.getTransformation().getScale() - 0.1);
                curve.recalculateAbsoluteControlPoints();
            }
            while (curve.calculateLengthOfCurveAt(1) < largestX) {
                curve.getTransformation().setScale(curve.getTransformation().getScale() + 0.1);
                curve.recalculateAbsoluteControlPoints();
            }

            LOG.info("Picked scale [{}] for x [{}]", curve.getTransformation().getScale(), largestX);

            return coordinateSystem;
        };
        converter.setTransformAlongCurveProvider(transformAlongCurveProvider);

        final WrittenNomaiBranchingLetterNode tree = converter.convertTextToNodeTree("I have 3287 Apples, but I wish I had 3288!");
        final List<Object> shapes = converter.convertNodeTreeToDrawables(tree);

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(250, 1000));
        renderer.setSize(1000, 1050);
        renderer.setShapes(shapes);
        renderer.setVisible(true);
        renderer.setLocationRelativeTo(null);
    }

    public static void main(String[] args) throws IOException {
        // manuallyTest();
        converterTest();
    }

}