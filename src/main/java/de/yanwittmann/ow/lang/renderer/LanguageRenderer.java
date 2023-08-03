package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.renderer.shapes.Circle2D;
import de.yanwittmann.ow.lang.renderer.shapes.TextShape;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LanguageRenderer extends JFrame {

    private List<Object> shapes;
    private Point2D offset = new Point2D.Double(0, 0);

    public LanguageRenderer(List<?> shapes) {
        this.shapes = (List<Object>) shapes;
        setBounds(100, 100, 800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public LanguageRenderer() {
        this(new ArrayList<>());
    }

    public void setOffset(Point2D offset) {
        this.offset = offset;
    }

    public void setShapes(List<?> shapes) {
        this.shapes = (List<Object>) shapes;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        AffineTransform transform = new AffineTransform();
        transform.translate(offset.getX(), offset.getY());
        Graphics2D g2 = (Graphics2D) g;

        for (Object shape : shapes) {
            g2.setColor(Color.BLACK);
            if (shape instanceof Circle2D) {
                g2.fill(transform.createTransformedShape((Circle2D) shape));
            } else if (shape instanceof TextShape) {
                TextShape textShape = (TextShape) shape;
                Point2D transformedPoint = transform.transform(textShape.getPosition(), null);
                g2.drawString(textShape.getText(), (float) transformedPoint.getX(), (float) transformedPoint.getY());
            } else if (shape instanceof Shape) {
                g2.draw(transform.createTransformedShape((Shape) shape));
            } else {
                throw new RuntimeException("Unknown shape type: " + shape.getClass().getSimpleName());
            }
        }
    }
}
