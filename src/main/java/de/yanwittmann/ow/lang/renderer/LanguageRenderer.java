package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.renderer.shapes.Circle2D;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.renderer.shapes.TextShape;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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
        BufferedImage bufferImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferImage.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        AffineTransform transform = new AffineTransform();
        transform.translate(offset.getX(), offset.getY());

        for (Object shape : shapes) {
            g2.setColor(Color.BLACK);
            if (shape instanceof Circle2D) {
                g2.fill(transform.createTransformedShape((Circle2D) shape));

            } else if (shape instanceof LetterShape) {
                final LetterShape letterShape = (LetterShape) shape;
                final Point2D[] linePositions = letterShape.getAbsoluteLinePositions();
                final Point2D[] branchPositions = letterShape.getAbsoluteBranchPositions();
                for (int i = 0; i < linePositions.length - 1; i++) {
                    final Point2D transformedPoint = transform.transform(linePositions[i], null);
                    final Point2D transformedPoint2 = transform.transform(linePositions[i + 1], null);
                    g2.drawLine((int) transformedPoint.getX(), (int) transformedPoint.getY(), (int) transformedPoint2.getX(), (int) transformedPoint2.getY());
                }
                for (int i = 0; i < branchPositions.length; i++) {
                    final Point2D transformedPoint = transform.transform(branchPositions[i], null);
                    g2.fillOval((int) transformedPoint.getX() - 3, (int) transformedPoint.getY() - 3, 6, 6);
                }

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

        g.drawImage(bufferImage, 0, 0, null);
    }
}
