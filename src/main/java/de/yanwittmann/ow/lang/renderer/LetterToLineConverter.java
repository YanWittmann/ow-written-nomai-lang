package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.other.RandomBetweenDouble;
import de.yanwittmann.ow.lang.other.RandomBetweenInteger;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LetterToLineConverter {

    private int verticalGapBetweenCenterLineAndOuterLines = 90;
    private int wordLineWidth = 50;
    private int generalLetterWidth = 50;

    private RandomBetweenInteger randomVerticalOffsetCenter = new RandomBetweenInteger(-10, 10);
    private RandomBetweenInteger randomVerticalOffsetOuter = new RandomBetweenInteger(-10, 30);
    private RandomBetweenDouble randomScale = new RandomBetweenDouble(0.7, 1.0);
    private RandomBetweenDouble randomRotation = new RandomBetweenDouble(0, Math.PI * 2);
    private RandomBetweenInteger randomOuterElementsStartPointHorizontalOffset = new RandomBetweenInteger(-10, 20);

    public List<Object> generateShapes(Random random, WrittenNomaiBranchingLetterNode nodeTree) {
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

                final boolean appendToUpperBranch = upperBranchX < lowerBranchX;
                if (appendToUpperBranch) {
                    upperBranchX = centerConsonantBranchX + generalLetterWidth + randomOuterElementsStartPointHorizontalOffset.next(random);
                } else {
                    lowerBranchX = centerConsonantBranchX + generalLetterWidth + randomOuterElementsStartPointHorizontalOffset.next(random);
                }

                do {
                    numberNode = numberNode == null ? lastCenterBranchNode.getNumber() : numberNode.getNumber();

                    final LetterShape numberShape = LetterShape.fromWrittenNomaiBranchingLetterNode(numberNode);
                    numberShape.setBasePosition(new Point2D.Double(appendToUpperBranch ? upperBranchX : lowerBranchX, (appendToUpperBranch ? -1 : 1) * verticalGapBetweenCenterLineAndOuterLines));
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

                final boolean appendToUpperBranch = upperBranchX < lowerBranchX;
                if (appendToUpperBranch) {
                    upperBranchX = centerConsonantBranchX + generalLetterWidth + randomOuterElementsStartPointHorizontalOffset.next(random);
                } else {
                    lowerBranchX = centerConsonantBranchX + generalLetterWidth + randomOuterElementsStartPointHorizontalOffset.next(random);
                }

                do {
                    vowelNode = vowelNode == null ? lastCenterBranchNode.getVowel() : vowelNode.getVowel();

                    final LetterShape vowelShape = LetterShape.fromWrittenNomaiBranchingLetterNode(vowelNode);
                    vowelShape.setBasePosition(new Point2D.Double(appendToUpperBranch ? upperBranchX : lowerBranchX, (appendToUpperBranch ? -1 : 1) * verticalGapBetweenCenterLineAndOuterLines));
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
            letterShape.setScale(randomScale.next(random));
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

        final ArrayList<Object> combinedShapes = new ArrayList<>();
        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        combinedShapes.addAll(letterShapes);
        return combinedShapes;
    }
}


/*
shapes.add(new Line2D.Double(x, y));
shapes.add(new Circle2D(new Line2D.Double(x, y), r));
 */