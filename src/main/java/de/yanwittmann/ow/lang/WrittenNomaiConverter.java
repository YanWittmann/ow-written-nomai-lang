package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurve;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurveCoordinateSystem;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class WrittenNomaiConverter {

    private static final Logger LOG = LogManager.getLogger(WrittenNomaiConverter.class);

    private WrittenNomaiTextTokenizer tokenizer;
    private LetterToLineConverter lineGenerator;
    private Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider;

    public void setTokenizer(WrittenNomaiTextTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void setLineGenerator(LetterToLineConverter lineGenerator) {
        this.lineGenerator = lineGenerator;
    }

    public void setTransformAlongCurveProvider(Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider) {
        this.transformAlongCurveProvider = transformAlongCurveProvider;
    }

    public WrittenNomaiTextTokenizer getTokenizer() {
        return tokenizer;
    }

    public LetterToLineConverter getLineGenerator() {
        return lineGenerator;
    }

    public Function<List<LetterShape>, BezierCurveCoordinateSystem> getTransformAlongCurveProvider() {
        return transformAlongCurveProvider;
    }

    public WrittenNomaiBranchingLetterNode convertTextToNodeTree(String normalText) {
        final List<List<String>> tokens = this.tokenizer.tokenizeToStringTokens(normalText);
        final List<List<WrittenNomaiTextLetter>> words = this.tokenizer.convertStringTokensToLetters(tokens);
        return WrittenNomaiBranchingLetterNode.fromSentence(words);
    }

    public List<Object> convertNodeTreeToDrawables(Random random, int regenerateAttempts, WrittenNomaiBranchingLetterNode rootNode) {
        final List<Object> initialShapes = lineGenerator.generateShapes(random, rootNode, transformAlongCurveProvider);
        if (regenerateAttempts <= 0) {
            return initialShapes;
        }

        int bestIntersectionCount = lineGenerator.intersectionCount(initialShapes);
        if (bestIntersectionCount == 0) {
            return initialShapes;
        }

        List<Object> bestShapes = initialShapes;

        for (int i = 0; i < regenerateAttempts; i++) {
            final List<Object> shapes = lineGenerator.generateShapes(new Random(), rootNode, transformAlongCurveProvider);
            final int intersectionCount = lineGenerator.intersectionCount(shapes);
            if (intersectionCount == 0) {
                LOG.info("Found valid shape after [{}] attempts", i);
                return shapes;
            } else if (intersectionCount < bestIntersectionCount) {
                LOG.info("Found better shape after [{}] attempts: [{}]", i, intersectionCount);
                bestIntersectionCount = intersectionCount;
                bestShapes = shapes;
            }
        }

        LOG.warn("Could not find valid shape after [{}] attempts, using best shape with [{}] intersections", regenerateAttempts, bestIntersectionCount);
        return bestShapes;
    }

    public static BezierCurveCoordinateSystem sizeDependantBezierCurveProvider(List<LetterShape> letterShapes) {
        if (letterShapes.isEmpty()) {
            return new BezierCurve(new Point2D.Double(0, 0), new Point2D.Double(1, 1)).getCoordinateSystem();
        }
        final double largestX = letterShapes.stream().mapToDouble(shape -> shape.getTransformation().getOffsetPosition().getX()).max().orElse(0);
        LOG.info("Largest x is [{}]", largestX);

        final BezierCurve curve = new BezierCurve();

        // the concept behind the different curves is that they become longer and more detailed the more letters there are
        if (largestX < 500) {
            // {{161, 505}, {137, 356}, {211, 174}, {338, 112}}
            curve.addControlPoint(new Point2D.Double(161, 505), new Point2D.Double(137, 356), new Point2D.Double(211, 174), new Point2D.Double(338, 112));
        } else if (largestX >= 500 && largestX < 1300) {
            // {{212, 495}, {26, 224}, {317, 47}, {434, 228}}
            curve.addControlPoint(new Point2D.Double(212, 495), new Point2D.Double(26, 224), new Point2D.Double(317, 47), new Point2D.Double(434, 228));
        } else if (largestX >= 1300 && largestX < 1700) {
            // {{365, 1012}, {43, 440}, {369, 45}, {821, 172}, {961, 593}, {606, 741}}
            curve.addControlPoint(new Point2D.Double(365, 1012), new Point2D.Double(43, 440), new Point2D.Double(369, 45), new Point2D.Double(821, 172), new Point2D.Double(961, 593), new Point2D.Double(606, 741));
        } else if (largestX >= 1700 && largestX < 2300) {
            // {{774, 950}, {545, 126}, {1257, 330}, {1267, 711}, {884, 693}}
            curve.addControlPoint(new Point2D.Double(774, 950), new Point2D.Double(545, 126), new Point2D.Double(1257, 330), new Point2D.Double(1267, 711), new Point2D.Double(884, 693));
        } else if (largestX >= 2300 && largestX < 3000) {
            // {{943, 934}, {458, 274}, {1417, 95}, {1460, 788}, {1070, 802}, {950, 592}}
            curve.addControlPoint(new Point2D.Double(943, 934), new Point2D.Double(458, 274), new Point2D.Double(1417, 95), new Point2D.Double(1460, 788), new Point2D.Double(1070, 802), new Point2D.Double(950, 592));
        } else if (largestX >= 3000 && largestX < 4000) {
            // {{970, 892}, {449, 241}, {1452, 116}, {1397, 761}, {880, 953}, {826, 534}, {1019, 509}}
            curve.addControlPoint(new Point2D.Double(970, 892), new Point2D.Double(449, 241), new Point2D.Double(1452, 116), new Point2D.Double(1397, 761), new Point2D.Double(880, 953), new Point2D.Double(826, 534), new Point2D.Double(1019, 509));
        } else if (largestX >= 4000 && largestX < 5000) {
            // {{934, 927}, {269, 106}, {1578, 54}, {1508, 887}, {818, 993}, {460, 545}, {1059, 276}, {1026, 552}}
            curve.addControlPoint(new Point2D.Double(934, 927), new Point2D.Double(269, 106), new Point2D.Double(1578, 54), new Point2D.Double(1508, 887), new Point2D.Double(818, 993), new Point2D.Double(460, 545), new Point2D.Double(1059, 276), new Point2D.Double(1026, 552));
        } else if (largestX >= 5000 && largestX < 6000) {
            // {{834, 921}, {358, 294}, {1639, 113}, {1136, 1149}, {740, 1070}, {451, 356}, {1143, 453}, {935, 673}}
            curve.addControlPoint(new Point2D.Double(834, 921), new Point2D.Double(358, 294), new Point2D.Double(1639, 113), new Point2D.Double(1136, 1149), new Point2D.Double(740, 1070), new Point2D.Double(451, 356), new Point2D.Double(1143, 453), new Point2D.Double(935, 673));
        } else {
            // {{882, 913}, {239, 133}, {1698, 53}, {1233, 1137}, {611, 1146}, {363, 254}, {1112, 168}, {1054, 734}, {894, 593}}
            curve.addControlPoint(new Point2D.Double(882, 913), new Point2D.Double(239, 133), new Point2D.Double(1698, 53), new Point2D.Double(1233, 1137), new Point2D.Double(611, 1146), new Point2D.Double(363, 254), new Point2D.Double(1112, 168), new Point2D.Double(1054, 734), new Point2D.Double(894, 593));
        }

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
    }
}
