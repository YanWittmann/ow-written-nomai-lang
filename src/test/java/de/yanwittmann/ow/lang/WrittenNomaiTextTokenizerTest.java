package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LanguageRenderer;
import de.yanwittmann.ow.lang.renderer.LineGenerator;
import de.yanwittmann.ow.lang.tokenizer.WittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
        final WittenNomaiBranchingLetterNode tree = WittenNomaiBranchingLetterNode.fromSentence(words);

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
        final WittenNomaiBranchingLetterNode tree = WittenNomaiBranchingLetterNode.fromSentence(words);
        LOG.info("Tokenized: {}", tokens);
        LOG.info("Symbol combinations: {}", words);
        LOG.info("Converted branches:{}", tree);

        final LineGenerator generator = new LineGenerator();
        final List<Shape> shapes = generator.generateLines(tree);

        final LanguageRenderer renderer = new LanguageRenderer();
        renderer.setOffset(new Point2D.Double(100, 300));
        renderer.setShapes(shapes);
        renderer.setVisible(true);
    }

}