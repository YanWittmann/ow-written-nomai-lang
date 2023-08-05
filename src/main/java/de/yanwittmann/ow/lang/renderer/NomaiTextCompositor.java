package de.yanwittmann.ow.lang.renderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class NomaiTextCompositor {

    private static final Logger LOG = LogManager.getLogger(NomaiTextCompositor.class);

    private final static Color NOMAI_TEXT_LIGHT_BLUE_PRIMARY = new Color(0xEFECFB);
    private final static Color NOMAI_TEXT_LIGHT_BLUE_SECONDARY = new Color(0xA29EBB);
    private final static Color NOMAI_TEXT_LIGHT_BLUE_TERNARY = new Color(0x87839F);

    public final static BufferedImage BACKGROUND_NOMAI_WALL = tryToReadBufferedImageFromResources("ow-lang-renderer/nomai_wall_background_texture.png");

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

    public static BufferedImage styleNomaiText(BufferedImage image, Color primary, Color secondary, Color ternary) {
        LOG.info("Styling nomai text with colors: " + primary + ", " + secondary + ", " + ternary);

        final BufferedImage primaryImage = ImageStyler.recolorImage(image, primary);
        final BufferedImage secondaryImage = ImageStyler.createBlurredMorphologizedImage(ImageStyler.recolorImage(image, secondary), secondary, 5, 5);
        final BufferedImage ternaryImage = ImageStyler.createBlurredMorphologizedImage(ImageStyler.recolorImage(image, ternary), ternary, 10, 10);

        final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = result.createGraphics();
        g2d.drawImage(ternaryImage, 0, 0, null);
        g2d.drawImage(secondaryImage, 0, 0, null);
        g2d.drawImage(primaryImage, 0, 0, null);
        g2d.dispose();

        return result;
    }
}
