package de.yanwittmann.ow.lang.renderer;

import de.yanwittmann.ow.lang.renderer.shapes.Circle2D;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.renderer.shapes.TextShape;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LanguageRenderer extends JFrame {

    private List<Object> shapes;
    private Point2D offset = new Point2D.Double(0, 0);

    private int lineThickness = 10;
    private int dotRadius = 10;

    private boolean cropImage = false;

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

    public void setCropImage(boolean cropImage) {
        this.cropImage = cropImage;
    }

    public void setLineThickness(int lineThickness) {
        this.lineThickness = lineThickness;
    }

    public void setDotRadius(int dotRadius) {
        this.dotRadius = dotRadius;
    }

    @Override
    public void paint(Graphics g) {
        if (cropImage) {
            BufferedImage bufferImage = renderShapes(
                    (int) (getWidth() + offset.getX() + 8000),
                    (int) (getHeight() + offset.getY() + 8000),
                    2,
                    new Point2D.Double(offset.getX() + 4000, offset.getY() + 4000)
            );
            bufferImage = cropImageToTarget(bufferImage, 70);
            bufferImage = resizeImageMaintainAspectRatio(bufferImage, getWidth() + 40, getHeight() + 40);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(bufferImage, 0, 0, null);

        } else {
            final BufferedImage bufferedImage = renderShapes(getWidth(), getHeight(), 1, offset);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(bufferedImage, 0, 0, null);
        }
    }

    public BufferedImage renderShapes(int width, int height, double scale, Point2D offset) {
        final BufferedImage bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bufferImage.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, width, height);

        AffineTransform transform = new AffineTransform();
        transform.translate(offset.getX(), offset.getY());
        transform.scale(scale, scale);

        for (Object shape : shapes) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1));

            if (shape instanceof Circle2D) {
                g2.fill(transform.createTransformedShape((Circle2D) shape));

            } else if (shape instanceof LetterShape) {
                g2.setStroke(new BasicStroke(lineThickness));

                final LetterShape letterShape = (LetterShape) shape;
                final Point2D[] linePositions = letterShape.getAbsoluteLinePositions();
                final Point2D[] branchPositions = letterShape.getAbsoluteBranchPositions();
                for (int i = 0; i < linePositions.length - 1; i++) {
                    final Point2D transformedPoint = transform.transform(linePositions[i], null);
                    final Point2D transformedPoint2 = transform.transform(linePositions[i + 1], null);
                    g2.drawLine((int) transformedPoint.getX(), (int) transformedPoint.getY(), (int) transformedPoint2.getX(), (int) transformedPoint2.getY());
                }

                if (letterShape.isLetterRoot() && branchPositions != null && branchPositions.length == 1) {
                    final Point2D transformedPoint = transform.transform(branchPositions[0], null);
                    g2.fillOval((int) transformedPoint.getX() - dotRadius, (int) transformedPoint.getY() - dotRadius, dotRadius * 2, dotRadius * 2);
                }
                /*for (int i = 0; i < branchPositions.length; i++) {
                    final Point2D transformedPoint = transform.transform(branchPositions[i], null);
                    g2.fillOval((int) transformedPoint.getX() - 3, (int) transformedPoint.getY() - 3, 6, 6);
                }*/

            } else if (shape instanceof TextShape) {
                TextShape textShape = (TextShape) shape;
                Point2D transformedPoint = transform.transform(textShape.getPosition(), null);
                g2.drawString(textShape.getText(), (float) transformedPoint.getX(), (float) transformedPoint.getY());

            } else if (shape instanceof Line2D) {
                g2.setStroke(new BasicStroke(lineThickness));
                g2.draw(transform.createTransformedShape((Shape) shape));

            } else if (shape instanceof Shape) {
                g2.draw(transform.createTransformedShape((Shape) shape));

            } else {
                throw new RuntimeException("Unknown shape type: " + shape.getClass().getSimpleName());
            }
        }
        return bufferImage;
    }

    public BufferedImage cropImageToTarget(BufferedImage src, double padding) {
        int minX = src.getWidth();
        int minY = src.getHeight();
        int maxX = 0;
        int maxY = 0;

        // find the bounds of the black pixels
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int color = src.getRGB(x, y);
                if (color == Color.BLACK.getRGB()) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // add padding
        minX = Math.max(0, minX - (int) padding);
        minY = Math.max(0, minY - (int) padding);
        maxX = Math.min(src.getWidth(), maxX + (int) padding);
        maxY = Math.min(src.getHeight(), maxY + (int) padding);

        try {
            return src.getSubimage(minX, minY, maxX - minX, maxY - minY);
        } catch (Exception e) {
            return src;
        }
    }

    public static BufferedImage resizeImageMaintainAspectRatio(BufferedImage image, int targetWidth, int targetHeight) {
        double originalRatio = (double) image.getWidth() / image.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        int newWidth;
        int newHeight;

        if (originalRatio > targetRatio) {
            newWidth = targetWidth;
            newHeight = (int) (newWidth / originalRatio);
        } else {
            newHeight = targetHeight;
            newWidth = (int) (newHeight * originalRatio);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2.dispose();

        return resizedImage;
    }
}
