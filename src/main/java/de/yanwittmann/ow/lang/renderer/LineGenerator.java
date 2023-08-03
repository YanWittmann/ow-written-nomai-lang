package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.tokenizer.WittenNomaiBranchingLetterNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LineGenerator {

    private int gapBetweenCenterLineAndOuterLines = 100;
    private int wordLineLength = 50;

    public LineGenerator() {
    }

    public List<Shape> generateLines(WittenNomaiBranchingLetterNode nodeTree) {
        final List<Shape> shapes = new ArrayList<>();



        return shapes;
    }
}


/*
shapes.add(new Line2D.Double(x, y));
shapes.add(new Circle2D(new Line2D.Double(x, y), r));
 */