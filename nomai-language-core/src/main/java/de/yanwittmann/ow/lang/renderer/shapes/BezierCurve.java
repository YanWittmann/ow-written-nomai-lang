package de.yanwittmann.ow.lang.renderer.shapes;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BezierCurve {

    private List<Point2D> controlPoints;
    private List<Point2D> absoluteControlPoints;

    private ShapeTransformation transformation = new ShapeTransformation();

    public BezierCurve() {
        this.controlPoints = new ArrayList<>();
        recalculateAbsoluteControlPoints();
    }

    public BezierCurve(Point2D... controlPoints) {
        this.controlPoints = new ArrayList<>(Arrays.asList(controlPoints));
        recalculateAbsoluteControlPoints();
    }

    public BezierCurve clone() {
        final BezierCurve clone = new BezierCurve();
        clone.setTransformation(new ShapeTransformation(transformation));
        clone.controlPoints = new ArrayList<>(controlPoints);
        clone.recalculateAbsoluteControlPoints();
        return clone;
    }

    public void setTransformation(ShapeTransformation transformation) {
        this.transformation = transformation;
        recalculateAbsoluteControlPoints();
    }

    public ShapeTransformation getTransformation() {
        return transformation;
    }

    public void recalculateAbsoluteControlPoints() {
        absoluteControlPoints = transformation.absolutePositions(controlPoints);
    }

    public void addControlPoint(Point2D... point) {
        this.controlPoints.addAll(Arrays.asList(point));
        recalculateAbsoluteControlPoints();
    }

    public void setControlPoint(int index, Point2D point) {
        this.controlPoints.set(index, point);
        recalculateAbsoluteControlPoints();
    }

    public List<Point2D> getControlPoints() {
        return absoluteControlPoints;
    }

    public List<Point2D> getUnTransformedControlPoints() {
        return controlPoints;
    }

    public Point2D getPointOnCurve(double t) {
        return calculateBezierPoint(t, absoluteControlPoints);
    }

    private Point2D calculateBezierPoint(double t, List<Point2D> controlPoints) {
        final int n = controlPoints.size() - 1;
        if (n == 0) {
            return controlPoints.get(0);
        } else {
            final List<Point2D> newControlPoints = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                final double x = (1 - t) * controlPoints.get(i).getX() + t * controlPoints.get(i + 1).getX();
                final double y = (1 - t) * controlPoints.get(i).getY() + t * controlPoints.get(i + 1).getY();
                newControlPoints.add(new Point2D.Double(x, y));
            }
            return calculateBezierPoint(t, newControlPoints);
        }
    }

    public double calculateLengthOfCurveAt(double t) {
        return calculateLengthOfCurveAt(t, 1000);
    }

    public double calculateLengthOfCurveAt(double t, int detail) {
        final int steps = Math.max(1, (int) (detail * t));
        double length = 0;
        Point2D previousPoint = getPointOnCurve(0);

        for (int i = 1; i <= steps * t; i++) {
            double currentT = (double) i / steps;
            Point2D currentPoint = getPointOnCurve(currentT);
            length += previousPoint.distance(currentPoint);
            previousPoint = currentPoint;
        }

        return length;
    }

    public double findTForX(double x) {
        return findTForX(x, 1000);
    }

    public double findTForX(double x, int detail) {
        if (x <= 0) return 0;

        final int steps = Math.max(1, (int) (detail * x));
        double length = 0;
        Point2D previousPoint = getPointOnCurve(0);

        for (int i = 1; i <= steps; i++) {
            double currentT = (double) i / steps;
            Point2D currentPoint = getPointOnCurve(currentT);
            length += previousPoint.distance(currentPoint);
            previousPoint = currentPoint;
            if (length >= x) {
                return currentT;
            }
        }

        return 1;
    }

    public Point2D getTangentAt(double t) {
        Point2D pointBefore = getPointOnCurve(Math.max(0, t - 0.01));
        Point2D pointAfter = getPointOnCurve(Math.min(1, t + 0.01));
        double dx = pointAfter.getX() - pointBefore.getX();
        double dy = pointAfter.getY() - pointBefore.getY();
        double length = Math.sqrt(dx * dx + dy * dy);
        return new Point2D.Double(dx / length, dy / length);
    }

    public Point2D getNormalAt(double t) {
        Point2D tangent = getTangentAt(t);
        return new Point2D.Double(-tangent.getY(), tangent.getX());
    }

    public void setFirstControlPointAsOrigin() {
        Point2D firstControlPoint = controlPoints.get(0);
        for (int i = 0; i < controlPoints.size(); i++) {
            Point2D point = controlPoints.get(i);
            controlPoints.set(i, new Point2D.Double(point.getX() - firstControlPoint.getX(), point.getY() - firstControlPoint.getY()));
        }
        recalculateAbsoluteControlPoints();
    }

    public BezierCurveCoordinateSystem getCoordinateSystem() {
        return new BezierCurveCoordinateSystem(this);
    }
}
