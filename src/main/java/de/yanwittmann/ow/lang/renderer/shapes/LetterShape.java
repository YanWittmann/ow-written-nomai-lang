package de.yanwittmann.ow.lang.renderer.shapes;

import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;

import java.awt.geom.Point2D;

public class LetterShape {

    private final WrittenNomaiTextLetter sourceLetter;
    private final ShapeDefinitions sourceShapeDefinition;

    private Point2D basePosition = new Point2D.Double(0, 0);
    private double rotationAngle = 0;
    private double scale = 1;

    private Point2D[] linePositions;
    private Point2D[] branchPositions;

    public LetterShape(WrittenNomaiTextLetter sourceLetter, ShapeDefinitions sourceShapeDefinition) {
        this.sourceLetter = sourceLetter;
        this.sourceShapeDefinition = sourceShapeDefinition;
    }

    public LetterShape(ShapeDefinitions sourceShapeDefinition) {
        this.sourceLetter = null;
        this.sourceShapeDefinition = sourceShapeDefinition;
    }

    public LetterShape() {
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

    public static LetterShape fromWrittenNomaiTextLetter(WrittenNomaiTextLetter letter) {
        final ShapeDefinitions shapeDefinition = letter.getShapeDefinition();

        final LetterShape letterShape = new LetterShape(letter, shapeDefinition);
        letterShape.setBasePosition(new Point2D.Double(0, 0));

        letterShape.setLinePositions(shapeDefinition.getPositions());
        letterShape.setBranchPositions(shapeDefinition.getBranchPositions());

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
