package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.other.RandomBetweenInteger;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WittenNomaiBranchingLetterNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LineGenerator {

    private final RandomBetweenInteger gapBetweenCenterLineAndOuterLines = new RandomBetweenInteger(80, 120);
    private final RandomBetweenInteger wordLineLength = new RandomBetweenInteger(40, 60);

    public List<Shape> generateLines(Random random, WittenNomaiBranchingLetterNode nodeTree) {
        final List<Shape> shapes = new ArrayList<>();

        LetterShape.fromWrittenNomaiTextLetter(nodeTree.getLetter());

        return shapes;
    }
}


/*
shapes.add(new Line2D.Double(x, y));
shapes.add(new Circle2D(new Line2D.Double(x, y), r));
 */