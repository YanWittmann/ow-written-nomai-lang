package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.WrittenNomaiConverter;
import de.yanwittmann.ow.lang.other.RandomBetweenDouble;
import de.yanwittmann.ow.lang.other.RandomBetweenInteger;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurveCoordinateSystem;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class LetterToLineConverter {

    private static final Logger LOG = LogManager.getLogger(LetterToLineConverter.class);

    private int gapBetweenCenterLineAndOuterLinesSingle = 70;
    private int gapBetweenCenterLineAndOuterLinesMultiple = 80;

    private int outsideStartGapWidthSingle = 10;
    private int outsideStartGapWidthMultiple = 70;

    private int wordLineWidth = 50;
    private int generalLetterWidth = 50;

    private RandomBetweenInteger randomVerticalOffsetCenter = new RandomBetweenInteger(-10, 10);
    private RandomBetweenInteger randomVerticalOffsetOuter = new RandomBetweenInteger(-10, 30);
    private RandomBetweenDouble randomScale = new RandomBetweenDouble(0.7, 0.9);
    private RandomBetweenDouble randomRotation = new RandomBetweenDouble(0, Math.PI * 2);
    private RandomBetweenInteger randomOuterElementsStartPointHorizontalOffset = new RandomBetweenInteger(-10, 20);


    public WrittenNomaiConverter.DrawablesResult generateShapes(Random random, WrittenNomaiBranchingLetterNode nodeTree, Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider) {
        LOG.info("Generating shapes for node tree with depth [{}]", nodeTree.getDepth());

        final List<LetterShape> letterShapes = distributeLetterShapes(random, nodeTree);
        LOG.info("Distributed [{}] letter shapes", letterShapes.size());

        final BezierCurveCoordinateSystem curve;

        if (transformAlongCurveProvider != null) {
            curve = transformAlongCurveProvider.apply(letterShapes);
        } else {
            curve = null;
            LOG.warn("No transform along curve provider provided, skipping transform.");
        }

        if (curve != null) {
            transformLetterShapesAlongCurve(letterShapes, curve);
            LOG.info("Transformed letter shapes along curve [{}]", curve.getCurve().getControlPoints().stream().map(p -> "{" + p.getX() + "," + p.getY() + "}").reduce((a, b) -> a + "," + b).orElse("null"));
        } else {
            LOG.warn("Transform along curve provider returned null, skipping transform.");
        }

        final List<Line2D> connectingLines = generateConnectingLines(random, letterShapes);
        LOG.info("Generated [{}] connecting lines", connectingLines.size());

        final ArrayList<Object> combinedShapes = new ArrayList<>();
        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        combinedShapes.addAll(letterShapes);
        combinedShapes.addAll(connectingLines);
        return new WrittenNomaiConverter.DrawablesResult(combinedShapes, curve);
    }

    private void transformLetterShapesAlongCurve(List<LetterShape> letterShapes, BezierCurveCoordinateSystem coordinateSystem) {
        if (coordinateSystem == null) {
            LOG.warn("No transform along curve provided, skipping transform.");
            return;
        }

        for (LetterShape letterShape : letterShapes) {
            final Point2D letterShapePosition = letterShape.getTransformation().getOffsetPosition();
            final Point2D transformedPosition = coordinateSystem.worldToBezier(letterShapePosition, 50);
            letterShape.getTransformation().setOffsetPosition(transformedPosition);
        }
    }

    private List<LetterShape> distributeLetterShapes(Random random, WrittenNomaiBranchingLetterNode nodeTree) {
        final List<LetterShape> letterShapes = new ArrayList<>();

        int centerConsonantBranchX = 0;
        int upperBranchX = 0;
        int lowerBranchX = 0;

        WrittenNomaiBranchingLetterNode lastCenterBranchNode = nodeTree;
        boolean previousWasRoot = false;

        while (lastCenterBranchNode != null) {
            if (lastCenterBranchNode.isRoot()) {
                if (previousWasRoot) {
                    centerConsonantBranchX += wordLineWidth;
                } else {
                    centerConsonantBranchX += (int) (wordLineWidth * 1.5);
                }

                final LetterShape rootShape = LetterShape.fromWrittenNomaiBranchingLetterNode(lastCenterBranchNode);
                rootShape.getTransformation().setOffsetPosition(new Point2D.Double(centerConsonantBranchX, 0));
                letterShapes.add(rootShape);
                previousWasRoot = true;

            } else { // else if (lastCenterBranchNode.hasLetter())
                if (previousWasRoot) {
                    centerConsonantBranchX += (int) (wordLineWidth * 1.5);
                } else {
                    centerConsonantBranchX += generalLetterWidth + wordLineWidth;
                }

                final LetterShape consonantShape = LetterShape.fromWrittenNomaiBranchingLetterNode(lastCenterBranchNode);
                consonantShape.getTransformation().setOffsetPosition(new Point2D.Double(centerConsonantBranchX, 0));
                letterShapes.add(consonantShape);
                previousWasRoot = false;
            }


            if (lastCenterBranchNode.hasNumber()) {
                WrittenNomaiBranchingLetterNode numberNode = null;
                final boolean hasMultipleNumbers = lastCenterBranchNode.getNumber().hasNumber();

                final boolean appendToUpperBranch = upperBranchX < lowerBranchX;
                if (appendToUpperBranch) {
                    upperBranchX = centerConsonantBranchX + (hasMultipleNumbers ? outsideStartGapWidthMultiple : outsideStartGapWidthSingle) + randomOuterElementsStartPointHorizontalOffset.next(random);
                } else {
                    lowerBranchX = centerConsonantBranchX + (hasMultipleNumbers ? outsideStartGapWidthMultiple : outsideStartGapWidthSingle) + randomOuterElementsStartPointHorizontalOffset.next(random);
                }

                do {
                    numberNode = numberNode == null ? lastCenterBranchNode.getNumber() : numberNode.getNumber();

                    final LetterShape numberShape = LetterShape.fromWrittenNomaiBranchingLetterNode(numberNode);
                    numberShape.getTransformation().setOffsetPosition(new Point2D.Double(
                            appendToUpperBranch ? upperBranchX : lowerBranchX,
                            (appendToUpperBranch ? -1 : 1) * (hasMultipleNumbers ? gapBetweenCenterLineAndOuterLinesMultiple : gapBetweenCenterLineAndOuterLinesSingle)
                    ));
                    letterShapes.add(numberShape);

                    if (appendToUpperBranch) {
                        upperBranchX += generalLetterWidth + wordLineWidth;
                    } else {
                        lowerBranchX += generalLetterWidth + wordLineWidth;
                    }
                } while (numberNode.hasNumber());
            }

            if (lastCenterBranchNode.hasVowel()) {
                WrittenNomaiBranchingLetterNode vowelNode = null;
                final boolean hasMultipleVowels = lastCenterBranchNode.getVowel().hasVowel();

                final boolean appendToUpperBranch = upperBranchX < lowerBranchX;
                if (appendToUpperBranch) {
                    upperBranchX = centerConsonantBranchX + (hasMultipleVowels ? outsideStartGapWidthMultiple : outsideStartGapWidthSingle) + randomOuterElementsStartPointHorizontalOffset.next(random);
                } else {
                    lowerBranchX = centerConsonantBranchX + (hasMultipleVowels ? outsideStartGapWidthMultiple : outsideStartGapWidthSingle) + randomOuterElementsStartPointHorizontalOffset.next(random);
                }

                do {
                    vowelNode = vowelNode == null ? lastCenterBranchNode.getVowel() : vowelNode.getVowel();

                    final LetterShape vowelShape = LetterShape.fromWrittenNomaiBranchingLetterNode(vowelNode);
                    vowelShape.getTransformation().setOffsetPosition(new Point2D.Double(
                            appendToUpperBranch ? upperBranchX : lowerBranchX,
                            (appendToUpperBranch ? -1 : 1) * (hasMultipleVowels ? gapBetweenCenterLineAndOuterLinesMultiple : gapBetweenCenterLineAndOuterLinesSingle)
                    ));
                    letterShapes.add(vowelShape);

                    if (appendToUpperBranch) {
                        upperBranchX += generalLetterWidth + wordLineWidth;
                    } else {
                        lowerBranchX += generalLetterWidth + wordLineWidth;
                    }
                } while (vowelNode.hasVowel());
            }

            if (lastCenterBranchNode.hasConsonant()) {
                if (lastCenterBranchNode.hasNextWord()) {
                    throw new RuntimeException("Consonant has next word:" + lastCenterBranchNode);
                }
                lastCenterBranchNode = lastCenterBranchNode.getConsonant();

            } else if (lastCenterBranchNode.hasNextWord()) {
                lastCenterBranchNode = lastCenterBranchNode.getNextWord();

            } else {
                lastCenterBranchNode = null;
            }
        }

        boolean isFirstRoot = true;

        for (LetterShape letterShape : letterShapes) {
            letterShape.getTransformation().setScale(randomScale.next(random) * (random.nextBoolean() ? 1 : -1));
            letterShape.getTransformation().setRotationAngle(randomRotation.next(random));

            final int offset;
            if (letterShape.isLetterConsonantOrRoot()) {
                if (isFirstRoot) {
                    isFirstRoot = false;
                    continue;
                }
                offset = randomVerticalOffsetCenter.next(random);
            } else {
                // check if above or below center line by checking y position >/<= 0
                offset = letterShape.getTransformation().getOffsetPosition().getY() > 0 ? randomVerticalOffsetOuter.next(random) : -randomVerticalOffsetOuter.next(random);
            }
            letterShape.getTransformation().setOffsetPosition(new Point2D.Double(letterShape.getTransformation().getOffsetPosition().getX(), letterShape.getTransformation().getOffsetPosition().getY() + offset));
        }

        return letterShapes;
    }

    private LetterShape findLetterShapeFromNode(List<LetterShape> letterShapes, WrittenNomaiBranchingLetterNode node) {
        for (LetterShape letterShape : letterShapes) {
            if (letterShape.getSourceNode() == node) {
                return letterShape;
            }
        }
        return null;
    }

    private boolean doesLineIntersectWithAnyOtherLine(Line2D line, double buffer, Line2D... otherLines) {
        Line2D bufferedLine = new Line2D.Double(
                new Point2D.Double(line.getX1() + buffer, line.getY1() + buffer),
                new Point2D.Double(line.getX2() - buffer, line.getY2() - buffer));

        for (Line2D otherLine : otherLines) {
            if (bufferedLine.intersectsLine(otherLine)) {
                return true;
            }
        }
        return false;
    }

    private Line2D findClosestBranchingPointsConnectingLine(LetterShape a, LetterShape b) {
        final Point2D[] posA = a.getAbsoluteBranchPositions();
        final Point2D[] posB = b.getAbsoluteBranchPositions();

        final Line2D[] linesA = a.getAbsoluteLines();
        final Line2D[] linesB = b.getAbsoluteLines();

        double bestDistance = Double.MAX_VALUE;
        final Line2D bestLine = findClosestBranchingPointsConnectingLine(posA, posB, linesA, linesB, bestDistance, true);

        if (bestLine == null) {
            final Line2D bestLineWithoutIntersectionCheck = findClosestBranchingPointsConnectingLine(posA, posB, linesA, linesB, bestDistance, false);
            if (bestLineWithoutIntersectionCheck == null) {
                throw new RuntimeException("Could not find a connecting line between " + a + " and " + b);
            }
            return bestLineWithoutIntersectionCheck;
        }

        return bestLine;
    }

    private Line2D findClosestBranchingPointsConnectingLine(Point2D[] posA, Point2D[] posB, Line2D[] linesA, Line2D[] linesB, double bestDistance, boolean checkForIntersection) {
        Line2D bestLine = null;

        for (int i = 0; i < posA.length; i++) {
            for (int j = 0; j < posB.length; j++) {
                final Line2D line = new Line2D.Double(posA[i], posB[j]);

                if (checkForIntersection) {
                    final boolean intersects = doesLineIntersectWithAnyOtherLine(line, 0.01, linesA) || doesLineIntersectWithAnyOtherLine(line, 0.01, linesB);
                    if (intersects) {
                        continue;
                    }
                }

                final double distance = line.getP1().distance(line.getP2());
                if (distance < bestDistance) {
                    bestLine = line;
                    bestDistance = distance;
                }
            }
        }
        return bestLine;
    }

    private List<Line2D> generateConnectingLines(Random random, List<LetterShape> letterShapes) {
        final List<Line2D> connectingLines = new ArrayList<>();

        for (LetterShape letterShape : letterShapes) {
            final WrittenNomaiBranchingLetterNode sourceNode = letterShape.getSourceNode();

            final LetterShape consonantNode = findLetterShapeFromNode(letterShapes, sourceNode.getConsonant());
            final LetterShape vowelNode = findLetterShapeFromNode(letterShapes, sourceNode.getVowel());
            final LetterShape numberNode = findLetterShapeFromNode(letterShapes, sourceNode.getNumber());
            final LetterShape nextWordNode = findLetterShapeFromNode(letterShapes, sourceNode.getNextWord());

            if (consonantNode != null) {
                connectingLines.add(findClosestBranchingPointsConnectingLine(letterShape, consonantNode));
            }
            if (vowelNode != null) {
                connectingLines.add(findClosestBranchingPointsConnectingLine(letterShape, vowelNode));
            }
            if (numberNode != null) {
                connectingLines.add(findClosestBranchingPointsConnectingLine(letterShape, numberNode));
            }
            if (nextWordNode != null) {
                connectingLines.add(findClosestBranchingPointsConnectingLine(letterShape, nextWordNode));
            }
        }

        return connectingLines;
    }

    private Line2D shortenLineFromBothSides(Line2D line, double amount) {
        if (line == null) {
            return null;
        }
        final double angle = Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1());
        final double x1 = line.getX1() + Math.cos(angle) * amount;
        final double y1 = line.getY1() + Math.sin(angle) * amount;
        final double x2 = line.getX2() - Math.cos(angle) * amount;
        final double y2 = line.getY2() - Math.sin(angle) * amount;
        return new Line2D.Double(x1, y1, x2, y2);
    }

    private int intersectionCount(Object shapeA, Object shapeB) {
        final LetterShape letterShapeA = shapeA instanceof LetterShape ? (LetterShape) shapeA : null;
        final LetterShape letterShapeB = shapeB instanceof LetterShape ? (LetterShape) shapeB : null;
        final Line2D lineA = shapeA instanceof Line2D ? (Line2D) shapeA : null;
        final Line2D lineB = shapeB instanceof Line2D ? (Line2D) shapeB : null;

        final Line2D[] linesA;
        final Line2D[] linesB;

        if (letterShapeA != null && letterShapeB != null) {
            linesA = letterShapeA.getAbsoluteLines();
            linesB = letterShapeB.getAbsoluteLines();
        } else if (letterShapeA != null) {
            linesA = letterShapeA.getAbsoluteLines();
            linesB = new Line2D[]{shortenLineFromBothSides(lineB, 2)};
        } else if (letterShapeB != null) {
            linesA = new Line2D[]{lineA};
            linesB = letterShapeB.getAbsoluteLines();
        } else {
            linesA = new Line2D[]{shortenLineFromBothSides(lineA, 2)};
            linesB = new Line2D[]{shortenLineFromBothSides(lineB, 2)};
        }

        int count = 0;

        for (Line2D line1 : linesA) {
            for (Line2D line2 : linesB) {
                if (line1.intersectsLine(line2)) {
                    count++;
                }
            }
        }

        return count;
    }

    public int intersectionCount(List<Object> shapes) {
        int count = 0;

        for (int i = 0; i < shapes.size(); i++) {
            final Object shapeA = shapes.get(i);
            if (!(shapeA instanceof LetterShape || shapeA instanceof Line2D)) {
                continue;
            }

            for (int j = i + 1; j < shapes.size(); j++) {
                final Object shapeB = shapes.get(j);
                if (!(shapeB instanceof LetterShape || shapeB instanceof Line2D)) {
                    continue;
                }

                count += intersectionCount(shapeA, shapeB);
            }
        }

        return count;
    }
}
