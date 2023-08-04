package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

class WrittenNomaiTextTokenizerTest {
    private static final Logger LOG = LogManager.getLogger(WrittenNomaiTextTokenizerTest.class);

    @Test
    public void test() throws IOException {
        final WrittenNomaiTextTokenizer tokenizer = new WrittenNomaiTextTokenizer(
                new File("src/main/resources/ow-lang/cmudict.dict"),
                new File("src/main/resources/ow-lang/cmudict-to-ow.txt")
        );

        final List<List<String>> tokens = tokenizer.tokenizeToStringTokens("I have 3287 Apples, but I wish I had 3288!");
        final List<List<WrittenNomaiTextLetter>> words = tokenizer.convertStringTokensToLetters(tokens);
        final WrittenNomaiBranchingLetterNode tree = WrittenNomaiBranchingLetterNode.fromSentence(words);

        LOG.info("Tokenized: {}", tokens);
        LOG.info("Symbol combinations: {}", words);
        LOG.info("Converted branches:{}", tree);
    }

    public static void main(String[] args) throws IOException {
        final WrittenNomaiTextTokenizer tokenizer = new WrittenNomaiTextTokenizer(
                new File("src/main/resources/ow-lang/cmudict.dict"),
                new File("src/main/resources/ow-lang/cmudict-to-ow.txt")
        );

        final List<List<String>> tokens = tokenizer.tokenizeToStringTokens("I have 3287 Apples but I wish I had 3288");
        final List<List<WrittenNomaiTextLetter>> words = tokenizer.convertStringTokensToLetters(tokens);
        final WrittenNomaiBranchingLetterNode tree = WrittenNomaiBranchingLetterNode.fromSentence(words);
        LOG.info("Tokenized: {}", tokens);
        LOG.info("Symbol combinations: {}", words);
        LOG.info("Converted branches:{}", tree);

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(40, 300));
        renderer.setSize(2400, 600);
        renderer.setVisible(true);

        final LetterToLineConverter generator = new LetterToLineConverter();

        if (true) {
            new Thread(() -> {
                while (true) {
                    int seed = (int) (Math.random() * 1000000);
                    System.out.println("Seed: " + seed);
                    final List<Object> shapes = generator.generateShapes(new Random(seed), tree);
                    renderer.setShapes(shapes);
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            final List<Object> shapes = generator.generateShapes(new Random(952764), tree);
            renderer.setShapes(shapes);
        }
    }

}