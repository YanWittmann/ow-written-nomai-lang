package de.yanwittmann.ow.lang.renderer.shapes;

import org.apache.commons.io.FileUtils;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SvgPathDToArrayConverter {
    public static void main(String[] args) throws IOException {
        File dir = new File("");
        extractPathLine(findAndParseLatestSvgFileInDir(dir)).forEach(SvgPathDToArrayConverter::extractCoordinates);
    }

    private static List<String> findAndParseLatestSvgFileInDir(File basedir) throws IOException {
        File[] files = basedir.listFiles((dir, name) -> name.toLowerCase().endsWith(".svg"));

        if (files != null && files.length > 0) {
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            System.out.println("Latest .svg file is: " + files[0].getAbsolutePath());
        } else {
            System.out.println("No .svg files found in the directory.");
            return Collections.emptyList();
        }

        return FileUtils.readLines(files[0], StandardCharsets.UTF_8);
    }

    private static List<String> extractPathLine(List<String> lines) {
        return lines.stream().filter(line -> line.contains("d=\"m")).collect(Collectors.toList());
    }

    private static List<Point2D> convergeSimilarPoints(List<Point2D> points) {
        List<Point2D> convergedPoints = new ArrayList<>();
        for (Point2D point : points) {
            boolean foundSimilar = false;
            for (Point2D existingPoint : convergedPoints) {
                if (existingPoint.distance(point) < 5) {
                    convergedPoints.add(new Point2D.Double(existingPoint.getX(), existingPoint.getY()));
                    System.out.println("Converged point: " + point + " -> " + existingPoint);
                    foundSimilar = true;
                    break;
                }
            }
            if (!foundSimilar) {
                convergedPoints.add(new Point2D.Double(point.getX(), point.getY()));
            }
        }
        return convergedPoints;
    }

    private static void extractCoordinates(String input) {
        Pattern pattern = Pattern.compile("d=\"m(.*?)z?\"");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            final List<Point2D> points = new ArrayList<>();
            final String pathData = matcher.group(1);
            final String[] commands = pathData.split("l");
            int x = 0, y = 0;
            for (String command : commands) {
                String[] parts = command.split(",");
                x += (int) Double.parseDouble(parts[0]);
                y += (int) Double.parseDouble(parts[1]);
                points.add(new Point2D.Double(x, y));
            }

            final List<Point2D> convergedPoints = convergeSimilarPoints(points);

            System.out.println("points: {" + convergedPoints.stream().map(point -> "{" + (int) point.getX() + ", " + (int) point.getY() + "}").collect(Collectors.joining(", ")) + "}");
            System.out.println("unique: {" + convergedPoints.stream().map(point -> "{" + (int) point.getX() + ", " + (int) point.getY() + "}").distinct().collect(Collectors.joining(", ")) + "}");
        } else {
            System.out.println("No match found in: " + input);
        }
    }
}
