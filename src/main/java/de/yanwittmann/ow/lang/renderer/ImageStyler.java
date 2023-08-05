package de.yanwittmann.ow.lang.renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

public class ImageStyler {

    public static BufferedImage copyImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public static BufferedImage recolorImage(BufferedImage image, Color color) {
        BufferedImage recoloredImage = copyImage(image);
        for (int x = 0; x < recoloredImage.getWidth(); x++) {
            for (int y = 0; y < recoloredImage.getHeight(); y++) {
                int pixel = recoloredImage.getRGB(x, y);
                if ((pixel >> 24) != 0x00) {
                    recoloredImage.setRGB(x, y, color.getRGB());
                }
            }
        }
        return recoloredImage;
    }

    public static BufferedImage createBlurredMorphologizedImage(BufferedImage image, Color color, int morphologyStrength, int blurStrength) {
        // 1. apply morphology to the source image
        // 2. create a black and white image from the morholigized image, white where the color is found, black where not
        // 3. apply blur to the black and white image
        // 4. create an image of pure color and the same size as the source image
        // 5. apply the black and white image as alpha mask to the pure color image

        BufferedImage morphologizedImage = applyMorphology(image, color, morphologyStrength);
        BufferedImage blackAndWhiteImage = applyPureBlackAndWhiteFilterBasedOnColor(morphologizedImage, color);
        BufferedImage blurredImage = applyBlur(blackAndWhiteImage, blurStrength);
        BufferedImage pureColorImage = createPureColorImage(image.getWidth(), image.getHeight(), color);
        BufferedImage alphaMaskApplied = applyAlphaMask(pureColorImage, blurredImage);

        return alphaMaskApplied;
    }

    private static BufferedImage applyAlphaMask(BufferedImage image, BufferedImage blurredImage) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                int blurredPixel = blurredImage.getRGB(x, y);
                int alpha = (blurredPixel & 0xFF) | ((blurredPixel >> 8) & 0xFF) | ((blurredPixel >> 16) & 0xFF);
                alpha /= 3;
                output.setRGB(x, y, (alpha << 24) | (pixel & 0x00FFFFFF));
            }
        }
        return output;
    }

    private static BufferedImage createPureColorImage(int width, int height, Color color) {
        BufferedImage pureColorImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = pureColorImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return pureColorImage;
    }

    private static BufferedImage applyPureBlackAndWhiteFilterBasedOnColor(BufferedImage image, Color findColor) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int findColorRGB = findColor.getRGB();
        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                if (pixel == findColorRGB) {
                    output.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    output.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return output;
    }

    public static BufferedImage applyMorphology(BufferedImage image, Color color, int morphologyStrength) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final int dilationRadius = morphologyStrength;
        final int colorRGB = color.getRGB();

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                boolean colorFound = false;
                for (int dx = -dilationRadius; dx <= dilationRadius; ++dx) {
                    for (int dy = -dilationRadius; dy <= dilationRadius; ++dy) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            int pixel = image.getRGB(nx, ny);
                            if (pixel == colorRGB) {
                                colorFound = true;
                                break;
                            }
                        }
                    }
                    if (colorFound) {
                        break;
                    }
                }
                if (colorFound) {
                    output.setRGB(x, y, colorRGB);
                } else {
                    output.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }

        return output;
    }


    public static BufferedImage applyBlur(BufferedImage image, int blurStrength) {
        if (blurStrength == 0) {
            return image;
        }
        return applyBoxBlur(image, blurStrength);
    }

    private static BufferedImage applyBoxBlur(BufferedImage image, int blurStrength) {
        float weight = 1.0f / (blurStrength * blurStrength);
        float[] elements = new float[blurStrength * blurStrength];
        Arrays.fill(elements, weight);
        final Kernel kernel = new Kernel(blurStrength, blurStrength, elements);
        final ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }
}

