package de.yanwittmann.ow.lang.renderer.shapes;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextSymbolType;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public enum ShapeDefinitions {
    LINE(WrittenNomaiTextSymbolType.LINE, null,
            new int[][]{{10, 50}, {90, 50}},
            new int[][]{{10, 50}, {90, 50}}),
    BEND(WrittenNomaiTextSymbolType.BEND, null,
            new int[][]{{22, 50}, {63, 25}, {85, 50}},
            new int[][]{{22, 50}, {85, 50}}),
    SQUARE(WrittenNomaiTextSymbolType.SQUARE, null,
            new int[][]{{57, 15}, {21, 40}, {47, 78}, {84, 53}, {57, 15}},
            new int[][]{{57, 15}, {21, 40}, {47, 78}, {84, 53}}),
    PENTAGON(WrittenNomaiTextSymbolType.PENTAGON, null,
            new int[][]{{48, 18}, {78, 39}, {74, 72}, {34, 80}, {21, 43}, {48, 18}},
            new int[][]{{48, 18}, {78, 39}, {74, 72}, {34, 80}, {21, 43}}),
    HEXAGON(WrittenNomaiTextSymbolType.HEXAGON, null,
            new int[][]{{25, 36}, {50, 17}, {75, 36}, {75, 63}, {50, 83}, {25, 63}, {25, 36}},
            new int[][]{{25, 36}, {50, 17}, {75, 36}, {75, 63}, {50, 83}, {25, 63}}),
    OCTAGON(WrittenNomaiTextSymbolType.OCTAGON, null,
            new int[][]{{35, 21}, {63, 21}, {80, 39}, {80, 67}, {64, 81}, {34, 81}, {18, 64}, {18, 39}, {35, 21}},
            new int[][]{{35, 21}, {63, 21}, {80, 39}, {80, 67}, {64, 81}, {34, 81}, {18, 64}, {18, 39}}),

    LINE_SQUARE(WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.SQUARE,
            new int[][]{{47, 23}, {18, 43}, {38, 71}, {68, 52}, {47, 23}, {17, 15}, {47, 23}},
            new int[][]{{18, 43}, {38, 71}, {68, 52}}),
    LINE_PENTAGON(WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.PENTAGON,
            new int[][]{{52, 21}, {27, 43}, {42, 75}, {77, 75}, {82, 42}, {52, 21}, {13, 22}},
            new int[][]{{27, 43}, {42, 75}, {77, 75}, {82, 42}}),
    LINE_HEXAGON(WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.HEXAGON,
            new int[][]{{55, 20}, {31, 37}, {31, 65}, {58, 83}, {78, 67}, {78, 39}, {55, 20}, {11, 13}},
            new int[][]{{31, 37}, {31, 65}, {58, 83}, {78, 67}, {78, 39}}),
    LINE_OCTAGON(WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.OCTAGON,
            new int[][]{{44, 24}, {70, 23}, {85, 38}, {85, 58}, {70, 72}, {46, 73}, {30, 59}, {29, 37}, {44, 24}, {2, 35}},
            new int[][]{{70, 23}, {85, 38}, {85, 58}, {70, 72}, {46, 73}, {30, 59}}),

    BEND_SQUARE(WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.SQUARE,
            new int[][]{},
            new int[][]{}),
    BEND_PENTAGON(WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.PENTAGON,
            new int[][]{},
            new int[][]{}),
    BEND_HEXAGON(WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.HEXAGON,
            new int[][]{},
            new int[][]{}),
    BEND_OCTAGON(WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.OCTAGON,
            new int[][]{},
            new int[][]{}),

    SQUARE_SQUARE(WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.SQUARE,
            new int[][]{{63, 40}, {39, 15}, {16, 36}, {39, 61}, {61, 85}, {86, 64}, {63, 40}, {39, 61}},
            new int[][]{{63, 40}, {39, 15}, {16, 36}, {39, 61}, {61, 85}, {86, 64}, {63, 40}}),
    SQUARE_PENTAGON(WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.PENTAGON,
            new int[][]{{58, 34}, {79, 47}, {71, 76}, {39, 76}, {32, 52}, {58, 34}, {32, 52}, {16, 26}, {42, 9}, {58, 34}},
            new int[][]{{79, 47}, {71, 76}, {39, 76}, {16, 26}, {42, 9}}),
    SQUARE_HEXAGON(WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.HEXAGON,
            new int[][]{{56, 36}, {38, 11}, {12, 29}, {33, 53}, {56, 36}, {83, 34}, {87, 60}, {59, 83}, {28, 80}, {33, 53}},
            new int[][]{{38, 11}, {12, 29}, {83, 34}, {87, 60}, {59, 83}, {28, 80}}),
    SQUARE_OCTAGON(WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.OCTAGON,
            new int[][]{{48, 29}, {25, 9}, {7, 29}, {28, 46}, {48, 29}, {71, 29}, {87, 42}, {90, 63}, {73, 82}, {47, 82}, {28, 64}, {28, 46}},
            new int[][]{{25, 9}, {7, 29}, {71, 29}, {87, 42}, {90, 63}, {73, 82}, {47, 82}, {28, 64}}),

    PENTAGON_PENTAGON(WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.PENTAGON,
            new int[][]{},
            new int[][]{}),
    PENTAGON_HEXAGON(WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.HEXAGON,
            new int[][]{},
            new int[][]{}),
    PENTAGON_OCTAGON(WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.OCTAGON,
            new int[][]{},
            new int[][]{}),

    HEXAGON_HEXAGON(WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.HEXAGON,
            new int[][]{},
            new int[][]{}),
    HEXAGON_OCTAGON(WrittenNomaiTextSymbolType.OCTAGON, WrittenNomaiTextSymbolType.OCTAGON,
            new int[][]{},
            new int[][]{}),
    ;

    private final Point2D[] positions;
    private final Point2D[] branchPositions;

    private final WrittenNomaiTextSymbolType typeA;
    private final WrittenNomaiTextSymbolType typeB;

    ShapeDefinitions(WrittenNomaiTextSymbolType typeA, WrittenNomaiTextSymbolType typeB, int[][] positions, int[][] branchPositions) {
        this.typeA = typeA;
        this.typeB = typeB;

        this.positions = convertToPoints(positions);
        this.branchPositions = convertToPoints(branchPositions);
    }

    public Point2D[] getPositions() {
        return positions;
    }

    public Point2D[] getBranchPositions() {
        return branchPositions;
    }

    public Rectangle2D getBoundingBox() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point2D position : positions) {
            if (position.getX() < minX) minX = position.getX();
            if (position.getY() < minY) minY = position.getY();
            if (position.getX() > maxX) maxX = position.getX();
            if (position.getY() > maxY) maxY = position.getY();
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    private static Point2D[] convertToPoints(int[][] positions) {
        Point2D[] points = new Point2D[positions.length];
        for (int i = 0; i < positions.length; i++) {
            points[i] = new Point2D.Double(positions[i][0], positions[i][1]);
        }
        return points;
    }

    public static ShapeDefinitions fromCombinationOfTypes(WrittenNomaiTextSymbolType a, WrittenNomaiTextSymbolType b) {
        for (ShapeDefinitions shapeDefinition : values()) {
            if (shapeDefinition.typeA == a && shapeDefinition.typeB == b) return shapeDefinition;
            else if (shapeDefinition.typeA == b && shapeDefinition.typeB == a) return shapeDefinition;
        }
        return null;
    }

    public static void main(String[] args) {
        final List<Object> shapes = new ArrayList<>();
        int offsetX = 0;
        int offsetY = 0;

        final int plusX = 150;
        final int plusY = 120;

        for (ShapeDefinitions definition : ShapeDefinitions.values()) {
            final Point2D[] positions = definition.getPositions();
            final Point2D[] branchPositions = definition.getBranchPositions();

            if (positions.length > 0) {
                GeneralPath path = new GeneralPath();
                path.moveTo(positions[0].getX() + offsetX, positions[0].getY() + offsetY);
                for (int i = 1; i < positions.length; i++) {
                    path.lineTo(positions[i].getX() + offsetX, positions[i].getY() + offsetY);
                }
                shapes.add(path);
            }

            for (Point2D branchPosition : branchPositions) {
                shapes.add(new Circle2D(branchPosition.getX() + offsetX, branchPosition.getY() + offsetY, 3));
            }

            TextShape textShape = new TextShape(definition.name(), new Point2D.Double(offsetX, offsetY - 10));
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
        renderer.setVisible(true);
    }
}
