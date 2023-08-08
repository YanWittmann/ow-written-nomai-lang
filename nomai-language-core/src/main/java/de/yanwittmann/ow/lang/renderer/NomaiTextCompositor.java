package de.yanwittmann.ow.lang.renderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NomaiTextCompositor {

    private static final Logger LOG = LogManager.getLogger(NomaiTextCompositor.class);

    private final static Color NOMAI_TEXT_LIGHT_BLUE_PRIMARY = new Color(0xEFECFB);
    private final static Color NOMAI_TEXT_LIGHT_BLUE_SECONDARY = new Color(0xA29EBB);
    private final static Color NOMAI_TEXT_LIGHT_BLUE_TERNARY = new Color(0x87839F);

    public final static BufferedImage BACKGROUND_NOMAI_WALL = tryToReadBufferedImageFromResources("ow-lang-renderer/nomai_wall_2_background_texture.png");
    public final static BufferedImage BACKGROUND_NOMAI_WALL_HANGING_CITY = tryToReadBufferedImageFromResources("ow-lang-renderer/nomai_wall_3_background_texture.png");
    public final static BufferedImage BACKGROUND_NOMAI_WALL_2_LAMP = tryToReadBufferedImageFromResources("ow-lang-renderer/nomai_wall_lamp_background_texture.png");
    public final static BufferedImage BACKGROUND_SPACE = tryToReadBufferedImageFromResources("ow-lang-renderer/blue_background_texture.png");
    public final static BufferedImage BACKGROUND_BLACK = tryToReadBufferedImageFromResources("ow-lang-renderer/black_background_texture.png");
    public final static BufferedImage BACKGROUND_TRANSPARENT = tryToReadBufferedImageFromResources("ow-lang-renderer/transparent_background_texture.png");
    public final static BufferedImage BACKGROUND_SOLANUM = tryToReadBufferedImageFromResources("ow-lang-renderer/solanum_background_texture.png");
    public final static BufferedImage BACKGROUND_STONE_WALL = tryToReadBufferedImageFromResources("ow-lang-renderer/stone_wall_background_texture.png");
    public final static BufferedImage BACKGROUND_TESTING = tryToReadBufferedImageFromResources("ow-lang-renderer/testing_background_texture.png");

    private static BufferedImage readBufferedImageFromResources(String path) throws IOException {
        final InputStream resourceAsStream = NomaiTextCompositor.class.getClassLoader().getResourceAsStream(path);
        if (resourceAsStream == null) {
            throw new RuntimeException("Could not find resource: " + path);
        }
        return ImageIO.read(resourceAsStream);
    }

    private static BufferedImage tryToReadBufferedImageFromResources(String path) {
        try {
            return readBufferedImageFromResources(path);
        } catch (IOException e) {
            LOG.error("Could not load image from resources: " + path, e);
            return null;
        }
    }

    public BufferedImage overlayNomaiTextWithBackground(BufferedImage image, BufferedImage background) {
        LOG.info("Overlaying nomai text with background");
        BufferedImage result = new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(background, 0, 0, null);
        g2d.drawImage(image, (background.getWidth() - image.getWidth()) / 2, (background.getHeight() - image.getHeight()) / 2, null);
        g2d.dispose();
        return result;
    }

    public BufferedImage styleNomaiTextLightBlue(BufferedImage image) {
        return styleNomaiText(image, NOMAI_TEXT_LIGHT_BLUE_PRIMARY, NOMAI_TEXT_LIGHT_BLUE_SECONDARY, NOMAI_TEXT_LIGHT_BLUE_TERNARY);
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public static BufferedImage styleNomaiText(BufferedImage image, Color primary, Color secondary, Color ternary) {
        LOG.info("Styling nomai text with colors: " + primary + ", " + secondary + ", " + ternary);

        final Future<BufferedImage> primaryFuture = executor.submit(() -> ImageStyler.recolorImage(image, primary));
        final Future<BufferedImage> secondaryFuture = executor.submit(() -> ImageStyler.createBlurredMorphologizedImage(ImageStyler.recolorImage(image, secondary), secondary, 5, 5));
        final Future<BufferedImage> ternaryFuture = executor.submit(() -> ImageStyler.createBlurredMorphologizedImage(ImageStyler.recolorImage(image, ternary), ternary, 10, 10));

        final BufferedImage primaryImage;
        final BufferedImage secondaryImage;
        final BufferedImage ternaryImage;

        try {
            primaryImage = primaryFuture.get();
            secondaryImage = secondaryFuture.get();
            ternaryImage = ternaryFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to style nomai text", e);
        }

        final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = result.createGraphics();
        g2d.drawImage(ternaryImage, 0, 0, null);
        g2d.drawImage(secondaryImage, 0, 0, null);
        g2d.drawImage(primaryImage, 0, 0, null);
        g2d.dispose();

        return result;
    }
}
