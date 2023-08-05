package de.yanwittmann.ow.lang.renderer.shapes;

import java.awt.geom.Point2D;

public class TextShape {
    private final String text;
    private final Point2D position;

    public TextShape(String text, Point2D position) {
        this.text = text;
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public Point2D getPosition() {
        return position;
    }
}
