package de.yanwittmann.ow.lang.renderer.shapes;

import java.awt.geom.Point2D;

public class BezierCurveCoordinateSystem {

    private BezierCurve curve;

    public BezierCurveCoordinateSystem(BezierCurve curve) {
        this.curve = curve;
    }

    public Point2D worldToBezier(Point2D point) {
        final double t = curve.findTForX(point.getX());
        final Point2D pointOnCurve = curve.getPointOnCurve(t);
        final Point2D normal = curve.getNormalAt(t);
        return new Point2D.Double(pointOnCurve.getX() + normal.getX() * point.getY(), pointOnCurve.getY() + normal.getY() * point.getY());
    }

    public BezierCurve getCurve() {
        return curve;
    }
}
