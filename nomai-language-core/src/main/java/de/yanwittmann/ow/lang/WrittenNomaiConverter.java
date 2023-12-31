package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.shapes.*;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;
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

    public DrawablesResult convertNodeTreeToDrawables(Random random, int regenerateAttempts, WrittenNomaiBranchingLetterNode rootNode) {
        final DrawablesResult initialShapes = lineGenerator.generateShapes(random, rootNode, transformAlongCurveProvider);
        if (regenerateAttempts <= 0) {
            return initialShapes;
        }

        int bestIntersectionCount = lineGenerator.intersectionCount(initialShapes.getDrawables());
        if (bestIntersectionCount == 0) {
            return initialShapes;
        }

        DrawablesResult bestShapes = initialShapes;

        for (int i = 0; i < regenerateAttempts; i++) {
            final DrawablesResult shapes = lineGenerator.generateShapes(new Random(), rootNode, transformAlongCurveProvider);
            final int intersectionCount = lineGenerator.intersectionCount(shapes.getDrawables());
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

    public static BezierCurve overrideBezierCurve = null;

    public static BezierCurveCoordinateSystem lengthDependantUpwardsSpiralBezierCurveProvider(List<LetterShape> letterShapes) {
        if (letterShapes.isEmpty()) {
            return new BezierCurve(new Point2D.Double(0, 0), new Point2D.Double(1, 1)).getCoordinateSystem();
        }
        final double largestX = letterShapes.stream().mapToDouble(shape -> {
            if (shape.isLetterConsonantOrRoot()) {
                return shape.getTransformation().getOffsetPosition().getX();
            } else {
                return shape.getTransformation().getOffsetPosition().getX() + 300;
            }
        }).max().orElse(0);
        LOG.info("Largest x is [{}]", largestX);

        final BezierCurve curve;

        if (overrideBezierCurve != null) {
            curve = overrideBezierCurve.clone();
        } else {
            curve = new BezierCurve();


            // the concept behind the different curves is that they become longer and more detailed the more letters there are
            if (largestX < 500) {
                // {{161, 505}, {137, 356}, {211, 174}, {338, 112}}
                curve.addControlPoint(new Point2D.Double(161, 505), new Point2D.Double(30, 335), new Point2D.Double(198, 125), new Point2D.Double(337, 235));
            } else if (largestX >= 500 && largestX < 950) {
                // {{212, 495}, {26, 224}, {317, 47}, {434, 228}}
                curve.addControlPoint(new Point2D.Double(284, 493), new Point2D.Double(78, 230), new Point2D.Double(389, 38), new Point2D.Double(404, 263));
            } else if (largestX >= 950 && largestX < 1300) {
                // {{199, 391}, {56, 121}, {359, 194}, {251, 298}}
                curve.addControlPoint(new Point2D.Double(199, 391), new Point2D.Double(28, 177), new Point2D.Double(351, 210), new Point2D.Double(237, 318));
            } else if (largestX >= 1300 && largestX < 1700) {
                // {{365, 1012}, {43, 440}, {369, 45}, {821, 172}, {961, 593}, {606, 741}}
                curve.addControlPoint(new Point2D.Double(402, 782), new Point2D.Double(41, 316), new Point2D.Double(542, 100), new Point2D.Double(800, 260), new Point2D.Double(729, 633), new Point2D.Double(385, 455));
            } else if (largestX >= 1700 && largestX < 2300) {
                // {{774, 950}, {545, 126}, {1257, 330}, {1267, 711}, {884, 693}}
                curve.addControlPoint(new Point2D.Double(432, 690), new Point2D.Double(146, 343), new Point2D.Double(507, 128), new Point2D.Double(870, 496), new Point2D.Double(450, 695), new Point2D.Double(462, 478));
            } else if (largestX >= 2300 && largestX < 3000) {
                // {{943, 934}, {458, 274}, {1417, 95}, {1460, 788}, {1070, 802}, {950, 592}}
                curve.addControlPoint(new Point2D.Double(432, 690), new Point2D.Double(144, 538), new Point2D.Double(294, 214), new Point2D.Double(737, 370), new Point2D.Double(450, 695), new Point2D.Double(340, 499));
            } else if (largestX >= 3000 && largestX < 4000) {
                // {{970, 892}, {449, 241}, {1452, 116}, {1397, 761}, {880, 953}, {826, 534}, {1019, 509}}
                curve.addControlPoint(new Point2D.Double(286, 508), new Point2D.Double(105, 324), new Point2D.Double(390, 173), new Point2D.Double(473, 479), new Point2D.Double(196, 493), new Point2D.Double(228, 341), new Point2D.Double(299, 367));
            } else if (largestX >= 4000 && largestX < 5000) {
                // {{934, 927}, {269, 106}, {1578, 54}, {1508, 887}, {818, 993}, {460, 545}, {1059, 276}, {1026, 552}}
                curve.addControlPoint(new Point2D.Double(318, 558), new Point2D.Double(116, 300), new Point2D.Double(420, 129), new Point2D.Double(692, 554), new Point2D.Double(124, 640), new Point2D.Double(223, 293), new Point2D.Double(387, 350), new Point2D.Double(343, 413));
            } else if (largestX >= 5000 && largestX < 6000) {
                // {{834, 921}, {358, 294}, {1639, 113}, {1136, 1149}, {740, 1070}, {451, 356}, {1143, 453}, {935, 673}}
                curve.addControlPoint(new Point2D.Double(322, 530), new Point2D.Double(108, 291), new Point2D.Double(427, 108), new Point2D.Double(717, 603), new Point2D.Double(111, 680), new Point2D.Double(64, 223), new Point2D.Double(481, 255), new Point2D.Double(368, 472), new Point2D.Double(315, 400));
            } else {
                // {{882, 913}, {239, 133}, {1698, 53}, {1233, 1137}, {611, 1146}, {363, 254}, {1112, 168}, {1054, 734}, {894, 593}}
                curve.addControlPoint(new Point2D.Double(322, 530), new Point2D.Double(104, 284), new Point2D.Double(427, 108), new Point2D.Double(699, 595), new Point2D.Double(111, 680), new Point2D.Double(44, 204), new Point2D.Double(475, 244), new Point2D.Double(375, 483), new Point2D.Double(302, 389));
            }
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

    public List<Object> combineMultipleDrawableBranches(Collection<DrawablesResult> drawableBranches) {
        final List<Object> combinedDrawableBranches = new ArrayList<>();

        final ArrayList<DrawablesResult> drawablesResults = new ArrayList<>(drawableBranches);
        final int firstDrawablesAmount = drawablesResults.get(0).getDrawables().size();

        LOG.info("Combining [{}] drawable branches, where the first one has [{}] drawables", drawablesResults.size(), firstDrawablesAmount);

        for (int i = 0; i < drawablesResults.size(); i++) {
            final DrawablesResult drawableBranch = drawablesResults.get(i);

            final List<Object> drawables = drawableBranch.getDrawables();
            // final BezierCurveCoordinateSystem coordinateSystem = drawableBranch.getCoordinateSystems();
            // final BezierCurve curve = coordinateSystem.getCurve();

            if (i == 0) {
                combinedDrawableBranches.addAll(drawables);
                continue;
            }

            final BezierCurveCoordinateSystem firstCoordinateSystem = drawablesResults.get(0).getCoordinateSystems();
            final BezierCurve firstCurve = firstCoordinateSystem.getCurve();


            final double curveOffsetFactor;
            if (firstDrawablesAmount < 20) {
                curveOffsetFactor = 1;
            } else if (firstDrawablesAmount < 50) {
                curveOffsetFactor = 1.2;
            } else if (firstDrawablesAmount < 80) {
                curveOffsetFactor = 1.5;
            } else if (firstDrawablesAmount < 100) {
                curveOffsetFactor = 2;
            } else {
                curveOffsetFactor = 3;
            }

            final double pointOnCurveOffset;
            final int angle;
            final int curveOffset;
            if (i == 1) {
                pointOnCurveOffset = 0.25;
                if (firstDrawablesAmount < 80) {
                    angle = -60;
                    curveOffset = (int) (50 * curveOffsetFactor);
                } else {
                    angle = 20;
                    curveOffset = (int) (30 * curveOffsetFactor);
                }
            } else if (i == 2) {
                pointOnCurveOffset = 0.50;
                angle = 60;
                curveOffset = (int) (60 * curveOffsetFactor);
            } else {
                pointOnCurveOffset = 0.70;
                angle = 30;
                curveOffset = (int) (100 * curveOffsetFactor);
            }

            LOG.info("Offset [{}] and angle [{}] with curve offset [{}]", pointOnCurveOffset, angle, curveOffset);


            final Point2D toPointBase = firstCurve.getPointOnCurve(pointOnCurveOffset);
            final Point2D normalAtToPoint = firstCurve.getNormalAt(pointOnCurveOffset);
            final Point2D toPoint = new Point2D.Double(toPointBase.getX() + normalAtToPoint.getX() * -curveOffset, toPointBase.getY() + normalAtToPoint.getY() * -curveOffset);

            final List<Object> translated = transformShapes(transformShapes(drawables, 0, 0, Math.toRadians(angle)), toPoint.getX(), toPoint.getY(), Math.toRadians(0));
            // final List<Object> translated = transformShapes(drawables, toPoint.getX(), toPoint.getY(), Math.toRadians(0));


            // now connect the first root with the closest point on the previous curve
            final LetterShape firstRoot = (LetterShape) drawables.stream().filter(drawable -> (drawable instanceof LetterShape) && ((LetterShape) drawable).isLetterRoot()).findFirst().orElse(null);
            if (firstRoot != null && firstRoot.getAbsoluteBranchPositions().length > 0) {
                final Point2D firstRootPoint = firstRoot.getAbsoluteBranchPositions()[0];
                // combinedDrawableBranches.add(new Circle2D(firstRootPoint, 15));

                combinedDrawableBranches.stream()
                        .filter(drawable -> (drawable instanceof LetterShape))
                        .map(drawable -> (LetterShape) drawable)
                        .min(Comparator.comparingDouble(p -> Arrays.stream(p.getAbsoluteBranchPositions())
                                .mapToDouble(point -> point.distance(firstRootPoint))
                                .min().orElse(0)))
                        .flatMap(letterShape -> Arrays.stream(letterShape.getAbsoluteBranchPositions())
                                .min(Comparator.comparingDouble(point -> point.distance(firstRootPoint))))
                        .ifPresent(closestPointOnPreviousCurve -> combinedDrawableBranches.add(new Line2D.Double(firstRootPoint, closestPointOnPreviousCurve)));
            }

            combinedDrawableBranches.addAll(translated);

        }

        return combinedDrawableBranches;
    }

    private List<Object> transformShapes(List<Object> drawables, double x, double y, double angle) {
        final AffineTransform transform = new AffineTransform();
        transform.rotate(angle);
        transform.translate(x, y);

        final List<Object> translatedDrawables = new ArrayList<>();

        for (Object drawable : drawables) {
            if (drawable instanceof Point2D) {
                final Point2D point = (Point2D) drawable;
                translatedDrawables.add(transform.transform(point, null));
            } else if (drawable instanceof LetterShape) {
                final LetterShape letterShape = (LetterShape) drawable;
                // letterShape.getTransformation().setOffsetPosition(new Point2D.Double(letterShape.getTransformation().getOffsetPosition().getX() + x, letterShape.getTransformation().getOffsetPosition().getY() + y));

                final Point2D[] linePositions = letterShape.getAbsoluteLinePositions();
                for (int i = 0; i < linePositions.length; i++) {
                    final Point2D linePosition = linePositions[i];
                    linePositions[i] = transform.transform(linePosition, null);
                }

                final Point2D[] branchPositions = letterShape.getAbsoluteBranchPositions();
                for (int i = 0; i < branchPositions.length; i++) {
                    final Point2D branchPosition = branchPositions[i];
                    branchPositions[i] = transform.transform(branchPosition, null);
                }

                letterShape.setTransformation(new ShapeTransformation());
                letterShape.setLinePositions(linePositions);
                letterShape.setBranchPositions(branchPositions);

                translatedDrawables.add(letterShape);
            } else if (drawable instanceof Shape) {
                final Shape shape = (Shape) drawable;
                translatedDrawables.add(transform.createTransformedShape(shape));
            } else {
                throw new IllegalArgumentException("Unknown drawable type: " + drawable.getClass());
            }
        }

        return translatedDrawables;
    }

    public static class DrawablesResult {

        private final List<Object> drawables;
        private final BezierCurveCoordinateSystem coordinateSystems;

        public DrawablesResult(List<Object> drawables, BezierCurveCoordinateSystem coordinateSystems) {
            this.drawables = drawables;
            this.coordinateSystems = coordinateSystems;
        }

        public List<Object> getDrawables() {
            return drawables;
        }

        public BezierCurveCoordinateSystem getCoordinateSystems() {
            return coordinateSystems;
        }
    }
}
