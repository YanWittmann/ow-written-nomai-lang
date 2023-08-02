package de.yanwittmann.ow.lang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

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

        final List<List<String>> tokens = tokenizer.tokenizeToStringTokens("I have 3287 Apples but I wish I had 3288");
        final List<List<WrittenNomaiTextLetter>> letters = tokenizer.convertStringTokensToLetters(tokens);

        LOG.info("Tokenized: {}", tokens);
        LOG.info("Converted: {}", letters);
    }

}