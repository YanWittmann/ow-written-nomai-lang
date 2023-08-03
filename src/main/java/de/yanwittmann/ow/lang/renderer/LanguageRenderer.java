package de.yanwittmann.ow.lang.renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LanguageRenderer extends JFrame {

    private List<Shape> shapes;
    private Point2D offset = new Point2D.Double(0, 0);

    public LanguageRenderer(List<Shape> shapes) {
        this.shapes = shapes;
        setBounds(100, 100, 800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public LanguageRenderer() {
        this(new ArrayList<>());
    }

    public void setOffset(Point2D offset) {
        this.offset = offset;
    }

    public void setShapes(List<Shape> shapes) {
        this.shapes = shapes;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        AffineTransform transform = new AffineTransform();
        transform.translate(offset.getX(), offset.getY());
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        for (Shape shape : shapes) {
            if (shape instanceof Circle2D) {
                g2.fill(transform.createTransformedShape(shape));
            } else {
                g2.draw(transform.createTransformedShape(shape));
            }
        }
    }
}
