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
import java.util.function.Consumer;

public class BezierEditor {

    private static final int SIZE = 600;
    private static final double STEP = 0.01;

    private final static long START_TIME = System.currentTimeMillis();

    private static final LanguageRenderer renderer = new LanguageRenderer();
    private static final BezierCurve curve = new BezierCurve();
    private static final BezierCurveCoordinateSystem coordinateSystem = new BezierCurveCoordinateSystem(curve);
    private static Optional<Integer> selectedPoint = Optional.empty();

    private List<Consumer<BezierCurve>> onCurveChangeListeners = new ArrayList<>();

    public static void main(String[] args) {
        new BezierEditor();
    }

    public BezierEditor() {
        setupRenderer();
        setupCurve();
        drawCurve();
        setupMouseListeners();
        curveChanged();

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

    public void addOnCurveChangeListener(Consumer<BezierCurve> onCurveChangeListener) {
        onCurveChangeListeners.add(onCurveChangeListener);
    }

    private void curveChanged() {
        onCurveChangeListeners.forEach(onCurveChangeListener -> onCurveChangeListener.accept(curve));
    }

    private void setupRenderer() {
        renderer.setOffset(new Point2D.Double(0, 0));
        renderer.setSize(SIZE, SIZE);
        renderer.setVisible(true);
    }

    private void setupCurve() {
        curve.addControlPoint(new Point2D.Double(432, 690), new Point2D.Double(146, 343), new Point2D.Double(426, 321), new Point2D.Double(756, 430), new Point2D.Double(450, 695), new Point2D.Double(428, 494));
        // curve.addControlPoint(new Point2D.Double(300, 300));
        // curve.addControlPoint(new Point2D.Double(400, 300));
        // curve.addControlPoint(new Point2D.Double(400, 400));
        // curve.addControlPoint(new Point2D.Double(300, 400));
        // curve.getTransformation().setOffsetPosition(new Point2D.Double(300, 300));
        // curve.getTransformation().setRotationAngle(Math.PI / 4);
        // curve.setFirstControlPointAsOrigin();
    }

    private void drawCurve() {
        final List<Object> points = new ArrayList<>();

        for (double t = 0; t <= 1; t += STEP) {
            Point2D point = curve.getPointOnCurve(t);
            points.add(new Circle2D(point, 2));
        }
        curve.getControlPoints().forEach(point -> points.add(new Circle2D(point, 4)));
        points.add(
                new Circle2D(coordinateSystem.worldToBezier(
                        new Point2D.Double(
                                (System.currentTimeMillis() - START_TIME) / 1000.0 * 100 % curve.calculateLengthOfCurveAt(1),
                                // sin wave depending on time
                                Math.sin((System.currentTimeMillis() - START_TIME) / 1000.0 * 2 * Math.PI) * 15
                        ), 100
                ), 6));

        renderer.setShapes(points);
    }

    private void setupMouseListeners() {
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

    private Optional<Integer> findClosestControlPoint(Point2D mousePosition) {
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

    private void printControlPoints() {
        final String transformed = curveToPrintable(curve.getControlPoints());
        final String unTransformed = curveToPrintable(curve.getUnTransformedControlPoints());
        System.out.println("Transf: " + transformed);
        if (!transformed.equals(unTransformed)) {
            System.out.println("Untran: " + unTransformed);
        }
        System.out.println("Length: " + curve.calculateLengthOfCurveAt(1));
    }

    private String curveToPrintable(List<Point2D> curve) {
        final int variant = 1;

        if (variant == 0) {
            // {{161, 505}, {137, 356}, {211, 174}, {338, 112}}
            final StringJoiner text = new StringJoiner(", ", "{", "}");
            for (Point2D controlPoint : curve) {
                text.add("{" + (int) controlPoint.getX() + ", " + (int) controlPoint.getY() + "}");
            }
            return text.toString();
        } else if (variant == 1) {
            // curve.addControlPoint(new Point2D.Double(161, 505), new Point2D.Double(137, 356), new Point2D.Double(211, 174), new Point2D.Double(338, 112));
            final StringJoiner text = new StringJoiner(", ", "curve.addControlPoint(", ");");
            for (Point2D controlPoint : curve) {
                text.add("new Point2D.Double(" + (int) controlPoint.getX() + ", " + (int) controlPoint.getY() + ")");
            }
            return text.toString();
        }

        return "";
    }

    private void handleMouseClick(MouseEvent e) {
        final Point2D mousePosition = renderer.getMousePosition();
        if (mousePosition != null) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                curve.addControlPoint(mousePosition);
                curveChanged();
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                Optional<Integer> closestControlPoint = findClosestControlPoint(mousePosition);
                if (closestControlPoint.isPresent()) {
                    curve.getUnTransformedControlPoints().remove((int) closestControlPoint.get());
                    curveChanged();
                }
            }
            drawCurve();
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        final Point2D mousePosition = renderer.getMousePosition();
        if (mousePosition != null && selectedPoint.isPresent()) {
            curve.setControlPoint(selectedPoint.get(), mousePosition);
            curveChanged();
            drawCurve();
        }
    }
}

