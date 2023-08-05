package de.yanwittmann.ow.lang.renderer.shapes;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ShapeTransformation {

    private Point2D offsetPosition = new Point2D.Double(0, 0);
    private double rotationAngle = 0;
    private double scale = 1;

    public Point2D absolutePosition(Point2D relativePosition) {
        // rotate
        final double transformedX = relativePosition.getX() * Math.cos(rotationAngle) - relativePosition.getY() * Math.sin(rotationAngle);
        final double transformedY = relativePosition.getX() * Math.sin(rotationAngle) + relativePosition.getY() * Math.cos(rotationAngle);
        // scale
        final double scaledX = transformedX * scale;
        final double scaledY = transformedY * scale;
        // translate
        return new Point2D.Double(scaledX + offsetPosition.getX(), scaledY + offsetPosition.getY());
    }

    public Point2D[] absolutePositions(Point2D... relativePositions) {
        if (relativePositions == null) {
            return new Point2D[0];
        }

        final Point2D[] absolutePositions = new Point2D[relativePositions.length];
        for (int i = 0; i < relativePositions.length; i++) {
            absolutePositions[i] = absolutePosition(relativePositions[i]);
        }

        return absolutePositions;
    }

    public List<Point2D> absolutePositions(List<Point2D> relativePositions) {
        if (relativePositions == null) {
            return new ArrayList<>();
        }

        final List<Point2D> absolutePositions = new ArrayList<>();
        for (Point2D relativePosition : relativePositions) {
            absolutePositions.add(absolutePosition(relativePosition));
        }

        return absolutePositions;
    }

    public void setOffsetPosition(Point2D offsetPosition) {
        this.offsetPosition = offsetPosition;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Point2D getOffsetPosition() {
        return offsetPosition;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public double getScale() {
        return scale;
    }
}
