package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurve;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurveCoordinateSystem;
import de.yanwittmann.ow.lang.renderer.shapes.Circle2D;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class BezierEditor {

    private static final int SIZE = 600;
    private static final double STEP = 0.01;

    private final static long START_TIME = System.currentTimeMillis();

    private static final LanguageRenderer renderer = new LanguageRenderer();
    private static final BezierCurve curve = new BezierCurve();
    private static final BezierCurveCoordinateSystem coordinateSystem = new BezierCurveCoordinateSystem(curve);
    private static Optional<Integer> selectedPoint = Optional.empty();

    public static void main(String[] args) {
        setupRenderer();
        setupCurve();
        drawCurve();
        setupMouseListeners();

        new Thread(() -> {
            while (true) {
                drawCurve();
                try {
                    Thread.sleep(1000 / 30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void setupRenderer() {
        renderer.setOffset(new Point2D.Double(0, 0));
        renderer.setSize(SIZE, SIZE);
        renderer.setVisible(true);
    }

    private static void setupCurve() {
        curve.addControlPoint(new Point2D.Double(300, 300));
        curve.addControlPoint(new Point2D.Double(400, 300));
        curve.addControlPoint(new Point2D.Double(400, 400));
        curve.addControlPoint(new Point2D.Double(300, 400));
        // curve.getTransformation().setOffsetPosition(new Point2D.Double(300, 300));
        // curve.getTransformation().setRotationAngle(Math.PI / 4);
        // curve.setFirstControlPointAsOrigin();
    }

    private static void drawCurve() {
        final List<Object> points = new ArrayList<>();

        for (double t = 0; t <= 1; t += STEP) {
            Point2D point = curve.getPointOnCurve(t);
            points.add(new Circle2D(point, 2));
        }
        curve.getControlPoints().forEach(point -> points.add(new Circle2D(point, 4)));
        points.add(new Circle2D(coordinateSystem.worldToBezier(new Point2D.Double(
                (System.currentTimeMillis() - START_TIME) / 1000.0 * 100 % curve.calculateLengthOfCurveAt(1),
                // sin wave depending on time
                Math.sin((System.currentTimeMillis() - START_TIME) / 1000.0 * 2 * Math.PI) * 15
        )), 6));

        renderer.setShapes(points);
    }

    private static void setupMouseListeners() {
        renderer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedPoint = findClosestControlPoint(renderer.getMousePosition());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedPoint = Optional.empty();
                printControlPoints();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });

        renderer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }

    private static Optional<Integer> findClosestControlPoint(Point2D mousePosition) {
        int closestPoint = -1;
        double closestPointDistance = Integer.MAX_VALUE;
        for (int i = 0; i < curve.getControlPoints().size(); i++) {
            final double distance = curve.getControlPoints().get(i).distance(mousePosition);
            if (distance < closestPointDistance) {
                closestPoint = i;
                closestPointDistance = distance;
            }
        }
        return closestPoint == -1 ? Optional.empty() : Optional.of(closestPoint);
    }

    private static void printControlPoints() {
        final StringJoiner transformed = new StringJoiner(", ", "{", "}");
        for (Point2D controlPoint : curve.getControlPoints()) {
            transformed.add("{" + (int) controlPoint.getX() + ", " + (int) controlPoint.getY() + "}");
        }
        final StringJoiner unTransformed = new StringJoiner(", ", "{", "}");
        for (Point2D controlPoint : curve.getUnTransformedControlPoints()) {
            unTransformed.add("{" + (int) controlPoint.getX() + ", " + (int) controlPoint.getY() + "}");
        }
        System.out.println("Transf: " + transformed);
        System.out.println("Untran: " + unTransformed);
        System.out.println("Length: " + curve.calculateLengthOfCurveAt(1));
    }

    private static void handleMouseClick(MouseEvent e) {
        final Point2D mousePosition = renderer.getMousePosition();
        if (mousePosition != null) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                curve.addControlPoint(mousePosition);
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                findClosestControlPoint(mousePosition).ifPresent(curve.getControlPoints()::remove);
            }
            drawCurve();
        }
    }

    private static void handleMouseDrag(MouseEvent e) {
        final Point2D mousePosition = renderer.getMousePosition();
        if (mousePosition != null && selectedPoint.isPresent()) {
            curve.setControlPoint(selectedPoint.get(), mousePosition);
            drawCurve();
        }
    }
}

