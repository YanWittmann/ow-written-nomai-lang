package de.yanwittmann.ow.lang.renderer.shapes;

import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class LetterShape {

    private final WrittenNomaiBranchingLetterNode sourceNode;
    private final WrittenNomaiTextLetter sourceLetter;
    private final ShapeDefinitions sourceShapeDefinition;

    private ShapeTransformation transformation = new ShapeTransformation();

    private Point2D[] linePositions;
    private Point2D[] branchPositions;

    public LetterShape(WrittenNomaiBranchingLetterNode sourceNode, WrittenNomaiTextLetter sourceLetter, ShapeDefinitions sourceShapeDefinition) {
        this.sourceNode = sourceNode;
        this.sourceLetter = sourceLetter;
        this.sourceShapeDefinition = sourceShapeDefinition;
    }

    public LetterShape(WrittenNomaiBranchingLetterNode node) {
        this.sourceNode = node;
        this.sourceLetter = node.getLetter() != null ? node.getLetter() : null;
        this.sourceShapeDefinition = node.getLetter() != null ? node.getLetter().getShapeDefinition() : null;
    }

    public LetterShape(ShapeDefinitions sourceShapeDefinition) {
        this.sourceNode = null;
        this.sourceLetter = null;
        this.sourceShapeDefinition = sourceShapeDefinition;
    }

    public LetterShape() {
        this.sourceNode = null;
        this.sourceLetter = null;
        this.sourceShapeDefinition = null;
    }

    public WrittenNomaiBranchingLetterNode getSourceNode() {
        return sourceNode;
    }

    public ShapeDefinitions getSourceShapeDefinition() {
        return sourceShapeDefinition;
    }

    public WrittenNomaiTextLetter getSourceLetter() {
        return sourceLetter;
    }

    public void setLinePositions(Point2D[] linePositions) {
        this.linePositions = linePositions;
    }

    public void setBranchPositions(Point2D[] branchPositions) {
        this.branchPositions = branchPositions;
    }

    public void setTransformation(ShapeTransformation transformation) {
        this.transformation = transformation;
    }

    public ShapeTransformation getTransformation() {
        return transformation;
    }

    public Point2D[] getAbsoluteLinePositions() {
        return transformation.absolutePositions(linePositions);
    }

    public Point2D[] getRawLinePositions() {
        return linePositions;
    }

    public Line2D[] getAbsoluteLines() {
        // connect all absolute lines, do not wrap around (connect last with first)
        final Point2D[] absoluteLinePositions = getAbsoluteLinePositions();
        if (absoluteLinePositions.length > 0) {
            final Line2D[] lines = new Line2D[absoluteLinePositions.length - 1];
            for (int i = 0; i < absoluteLinePositions.length - 1; i++) {
                lines[i] = new Line2D.Double(absoluteLinePositions[i], absoluteLinePositions[i + 1]);
            }
            return lines;

        } else {
            return new Line2D[0];
        }
    }

    public Point2D[] getAbsoluteBranchPositions() {
        return transformation.absolutePositions(branchPositions);
    }

    public boolean isLetterConsonantOrRoot() {
        return sourceLetter == null || sourceLetter.getType().isConsonant();
    }

    public boolean isLetterRoot() {
        return sourceLetter == null;
    }

    public static LetterShape fromWrittenNomaiBranchingLetterNode(WrittenNomaiBranchingLetterNode node) {
        final LetterShape letterShape = new LetterShape(node);
        letterShape.getTransformation().setOffsetPosition(new Point2D.Double(0, 0));

        if (node.isRoot()) {
            letterShape.setLinePositions(new Point2D[]{});
            letterShape.setBranchPositions(new Point2D[]{new Point2D.Double(0, 0)});

        } else {
            final WrittenNomaiTextLetter letter = node.getLetter();
            final ShapeDefinitions shapeDefinition = letter.getShapeDefinition();

            letterShape.setLinePositions(shapeDefinition.getPositions());
            letterShape.setBranchPositions(shapeDefinition.getBranchPositions());
        }

        return letterShape;
    }

    public static LetterShape fromWrittenNomaiTextLetterRoot() {
        final LetterShape letterShape = new LetterShape();
        letterShape.getTransformation().setOffsetPosition(new Point2D.Double(0, 0));

        letterShape.setLinePositions(new Point2D[]{});
        letterShape.setBranchPositions(new Point2D[]{new Point2D.Double(0, 0)});

        return letterShape;
    }

    public static LetterShape fromShapeDefinition(ShapeDefinitions shapeDefinition) {
        final LetterShape letterShape = new LetterShape(shapeDefinition);
        letterShape.getTransformation().setOffsetPosition(new Point2D.Double(0, 0));

        letterShape.setLinePositions(shapeDefinition.getPositions());
        letterShape.setBranchPositions(shapeDefinition.getBranchPositions());

        return letterShape;
    }
}
