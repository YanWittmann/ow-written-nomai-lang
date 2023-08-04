package de.yanwittmann.ow.lang.renderer.shapes;

import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;

import java.awt.geom.Point2D;

public class LetterShape {

    private final WrittenNomaiBranchingLetterNode sourceNode;
    private final WrittenNomaiTextLetter sourceLetter;
    private final ShapeDefinitions sourceShapeDefinition;

    private Point2D basePosition = new Point2D.Double(0, 0);
    private double rotationAngle = 0;
    private double scale = 1;

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

    public ShapeDefinitions getSourceShapeDefinition() {
        return sourceShapeDefinition;
    }

    public WrittenNomaiTextLetter getSourceLetter() {
        return sourceLetter;
    }

    public void setBasePosition(Point2D.Double basePosition) {
        this.basePosition = basePosition;
    }

    public void setLinePositions(Point2D[] linePositions) {
        this.linePositions = linePositions;
    }

    public void setBranchPositions(Point2D[] branchPositions) {
        this.branchPositions = branchPositions;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    private Point2D absolutePosition(Point2D relativePosition) {
        // rotate
        final double transformedX = relativePosition.getX() * Math.cos(rotationAngle) - relativePosition.getY() * Math.sin(rotationAngle);
        final double transformedY = relativePosition.getX() * Math.sin(rotationAngle) + relativePosition.getY() * Math.cos(rotationAngle);
        // scale
        final double scaledX = transformedX * scale;
        final double scaledY = transformedY * scale;
        // translate
        return new Point2D.Double(scaledX + basePosition.getX(), scaledY + basePosition.getY());
    }

    private Point2D[] absolutePositions(Point2D[] relativePositions) {
        if (relativePositions == null) {
            return new Point2D[0];
        }
        final Point2D[] absolutePositions = new Point2D[relativePositions.length];
        for (int i = 0; i < relativePositions.length; i++) {
            absolutePositions[i] = absolutePosition(relativePositions[i]);
        }
        return absolutePositions;
    }

    public Point2D[] getAbsoluteLinePositions() {
        return absolutePositions(linePositions);
    }

    public Point2D[] getAbsoluteBranchPositions() {
        return absolutePositions(branchPositions);
    }

    public Point2D getBasePosition() {
        return basePosition;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public boolean isLetterConsonantOrRoot() {
        return sourceLetter == null || sourceLetter.getType().isConsonant();
    }

    public static LetterShape fromWrittenNomaiBranchingLetterNode(WrittenNomaiBranchingLetterNode node) {
        final LetterShape letterShape = new LetterShape(node);
        letterShape.setBasePosition(new Point2D.Double(0, 0));

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
        letterShape.setBasePosition(new Point2D.Double(0, 0));

        letterShape.setLinePositions(new Point2D[]{});
        letterShape.setBranchPositions(new Point2D[]{new Point2D.Double(0, 0)});

        return letterShape;
    }

    public static LetterShape fromShapeDefinition(ShapeDefinitions shapeDefinition) {
        final LetterShape letterShape = new LetterShape(shapeDefinition);
        letterShape.setBasePosition(new Point2D.Double(0, 0));

        letterShape.setLinePositions(shapeDefinition.getPositions());
        letterShape.setBranchPositions(shapeDefinition.getBranchPositions());

        return letterShape;
    }
}
