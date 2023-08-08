package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.renderer.shapes.ShapeDefinitions;
import de.yanwittmann.ow.lang.renderer.shapes.TextShape;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ShapeDefinitionVisualizer {
    public static void main(String[] args) {
        final List<Object> shapes = new ArrayList<>();
        final int plusX = 150;
        final int plusY = 120;
        final int shapeOffsetX = 45;
        final int shapeOffsetY = 45;

        int offsetX = 0;
        int offsetY = 0;

        for (ShapeDefinitions definition : ShapeDefinitions.values()) {
            final LetterShape letterShape = LetterShape.fromShapeDefinition(definition);
            letterShape.getTransformation().setOffsetPosition(new Point2D.Double(offsetX + shapeOffsetX, offsetY + shapeOffsetY));
            shapes.add(letterShape);

            // shapes.add(new Circle2D(offsetX + shapeOffsetX, offsetY + shapeOffsetY, 2));

            final TextShape textShape = new TextShape(definition.name(), new Point2D.Double(offsetX, offsetY - 10));
            shapes.add(textShape);

            offsetX += plusX;
            if (offsetX > plusX * 8) {
                offsetX = 0;
                offsetY += plusY;
            }
        }

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(30, 80));
        renderer.setShapes(shapes);
        renderer.setSize(new Dimension(10 * plusX, offsetY + plusY * 2));
        renderer.setLineThickness(1);
        renderer.setDotRadius(3);
        renderer.setVisible(true);

        new Thread(() -> {
            int iteration = 0;
            while (true) {
                iteration++;
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int j = 0; j < shapes.size(); j++) {
                    if (shapes.get(j) instanceof LetterShape) {
                        final LetterShape letterShape = (LetterShape) shapes.get(j);
                        final double scale = 0.8 + Math.sin(iteration / 10.0) / 3.0;
                        letterShape.getTransformation().setScale(scale);
                        letterShape.getTransformation().setRotationAngle(letterShape.getTransformation().getRotationAngle() + scale / 40.0);
                    }
                }
                renderer.setShapes(shapes);
            }
        }).start();
    }
}
