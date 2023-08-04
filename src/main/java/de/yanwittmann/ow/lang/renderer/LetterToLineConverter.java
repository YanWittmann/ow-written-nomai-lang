package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.other.RandomBetweenDouble;
import de.yanwittmann.ow.lang.other.RandomBetweenInteger;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LetterToLineConverter {

    private int verticalGapBetweenCenterLineAndOuterLinesSingle = 50;
    private int verticalGapBetweenCenterLineAndOuterLinesMultiple = 80;
    private int outsideStartGapWidthSingle = 50;
    private int outsideStartGapWidthMultiple = 70;
    private int wordLineWidth = 50;
    private int generalLetterWidth = 50;

    private RandomBetweenInteger randomVerticalOffsetCenter = new RandomBetweenInteger(-10, 10);
    private RandomBetweenInteger randomVerticalOffsetOuter = new RandomBetweenInteger(-10, 30);
    private RandomBetweenDouble randomScale = new RandomBetweenDouble(0.7, 1.0);
    private RandomBetweenDouble randomRotation = new RandomBetweenDouble(0, Math.PI * 2);
    private RandomBetweenInteger randomOuterElementsStartPointHorizontalOffset = new RandomBetweenInteger(-10, 20);

    public List<Object> generateShapes(Random random, WrittenNomaiBranchingLetterNode nodeTree) {
        final List<LetterShape> letterShapes = distributeLetterShapes(random, nodeTree);
        final List<Line2D> connectingLines = generateConnectingLines(random, letterShapes);

        final ArrayList<Object> combinedShapes = new ArrayList<>();
        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        combinedShapes.addAll(letterShapes);
        combinedShapes.addAll(connectingLines);
        return combinedShapes;
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
                rootShape.setBasePosition(new Point2D.Double(centerConsonantBranchX, 0));
                letterShapes.add(rootShape);
                previousWasRoot = true;

            } else { // else if (lastCenterBranchNode.hasLetter())
                if (previousWasRoot) {
                    centerConsonantBranchX += (int) (wordLineWidth * 1.5);
                } else {
                    centerConsonantBranchX += generalLetterWidth + wordLineWidth;
                }

                final LetterShape consonantShape = LetterShape.fromWrittenNomaiBranchingLetterNode(lastCenterBranchNode);
                consonantShape.setBasePosition(new Point2D.Double(centerConsonantBranchX, 0));
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
                    numberShape.setBasePosition(new Point2D.Double(
                            appendToUpperBranch ? upperBranchX : lowerBranchX,
                            (appendToUpperBranch ? -1 : 1) * (hasMultipleNumbers ? verticalGapBetweenCenterLineAndOuterLinesMultiple : verticalGapBetweenCenterLineAndOuterLinesSingle)
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
                    vowelShape.setBasePosition(new Point2D.Double(
                            appendToUpperBranch ? upperBranchX : lowerBranchX,
                            (appendToUpperBranch ? -1 : 1) * (hasMultipleVowels ? verticalGapBetweenCenterLineAndOuterLinesMultiple : verticalGapBetweenCenterLineAndOuterLinesSingle)
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

        for (LetterShape letterShape : letterShapes) {
            letterShape.setScale(randomScale.next(random) * (random.nextBoolean() ? 1 : -1));
            letterShape.setRotationAngle(randomRotation.next(random));

            final int offset;
            if (letterShape.isLetterConsonantOrRoot()) {
                offset = randomVerticalOffsetCenter.next(random);
            } else {
                // check if above or below center line by checking y position >/<= 0
                offset = letterShape.getBasePosition().getY() > 0 ? randomVerticalOffsetOuter.next(random) : -randomVerticalOffsetOuter.next(random);
            }
            letterShape.setBasePosition(new Point2D.Double(letterShape.getBasePosition().getX(), letterShape.getBasePosition().getY() + offset));
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

    private Line2D findClosestBranchingPointsConnectingLine(LetterShape a, LetterShape b) {
        final Point2D[] posA = a.getAbsoluteBranchPositions();
        final Point2D[] posB = b.getAbsoluteBranchPositions();

        double bestDistance = Double.MAX_VALUE;
        Line2D bestLine = null;

        for (int i = 0; i < posA.length; i++) {
            for (int j = 0; j < posB.length; j++) {
                final Line2D line = new Line2D.Double(posA[i], posB[j]);
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
}
