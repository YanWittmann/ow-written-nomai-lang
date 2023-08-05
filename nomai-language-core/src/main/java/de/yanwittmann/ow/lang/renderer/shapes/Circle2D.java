package de.yanwittmann.ow.lang.renderer.shapes;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class Circle2D extends Ellipse2D.Double {

    public Circle2D(Point2D point, double radius) {
        super(point.getX() - radius, point.getY() - radius, 2.0 * radius, 2.0 * radius);
    }

    public Circle2D(double x, double y, double radius) {
        super(x - radius, y - radius, 2.0 * radius, 2.0 * radius);
    }
}
