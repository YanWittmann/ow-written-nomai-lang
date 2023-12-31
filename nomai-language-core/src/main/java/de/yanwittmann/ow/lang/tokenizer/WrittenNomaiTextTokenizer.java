package de.yanwittmann.ow.lang.tokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WrittenNomaiTextTokenizer {

    private static final Logger LOG = LogManager.getLogger(WrittenNomaiTextTokenizer.class);

    private final Map<String, List<List<String>>> dictionary;
    private final Map<String, String> conversionTable;

    public WrittenNomaiTextTokenizer(File dictionaryFile, File conversionTableFile) throws IOException {
        Objects.requireNonNull(dictionaryFile, "Dictionary file cannot be null!");
        Objects.requireNonNull(conversionTableFile, "Conversion table file cannot be null!");

        this.dictionary = loadDictionary(dictionaryFile);
        LOG.info("Loaded dictionary with [{}] entries from: {}", dictionary.size(), dictionaryFile.getAbsolutePath());

        this.conversionTable = loadConversionTable(conversionTableFile);
        LOG.info("Loaded conversion table with [{}] entries from: {}", conversionTable.size(), conversionTableFile.getAbsolutePath());

        assertConversionTableParsable();
    }

    public WrittenNomaiTextTokenizer(Class<?> clazz, String resourcePathDictionaryFile, String resourcePathConversionTableFile) throws IOException {
        Objects.requireNonNull(clazz, "Class cannot be null!");
        Objects.requireNonNull(resourcePathDictionaryFile, "Dictionary file cannot be null!");
        Objects.requireNonNull(resourcePathConversionTableFile, "Conversion table file cannot be null!");

        this.dictionary = loadDictionary(clazz, resourcePathDictionaryFile);
        LOG.info("Loaded dictionary with [{}] entries from: {}", dictionary.size(), resourcePathDictionaryFile);

        this.conversionTable = loadConversionTable(clazz, resourcePathConversionTableFile);
        LOG.info("Loaded conversion table with [{}] entries from: {}", conversionTable.size(), resourcePathConversionTableFile);

        assertConversionTableParsable();
    }

    private void assertConversionTableParsable() {
        // assert parseable
        final Set<String> unparsableConversionTableEntries = new HashSet<>();
        for (String key : conversionTable.values()) {
            try {
                WrittenNomaiTextLetter.fromToken(key, true);
            } catch (Exception e) {
                unparsableConversionTableEntries.add(key);
            }
        }
        if (!unparsableConversionTableEntries.isEmpty()) {
            throw new RuntimeException("The following conversion table entries are not parseable: " + unparsableConversionTableEntries);
        }
    }

    public List<List<WrittenNomaiTextLetter>> convertStringTokensToLetters(List<List<String>> tokens) {
        final List<List<WrittenNomaiTextLetter>> result = new ArrayList<>();

        for (List<String> token : tokens) {
            final List<WrittenNomaiTextLetter> tokenResult = new ArrayList<>();
            for (String word : token) {
                final WrittenNomaiTextLetter letter = WrittenNomaiTextLetter.fromToken(word, false);
                if (letter.isSpecialCharacter()) {
                    continue;
                }
                tokenResult.add(letter);
            }
            if (!tokenResult.isEmpty()) {
                result.add(tokenResult);
            }
        }

        return result;
    }

    public List<String> convertTextToBranchSnippets(String text, boolean splitSentences) {
        final List<String> snippets = new ArrayList<>();

        for (String sentence : text.split("[.!?:]")) {
            sentence = sentence.trim();
            if (splitSentences) {
                while (sentence.length() > 50) {
                    final int lastSpace = sentence.substring(0, 50).lastIndexOf(" ");
                    snippets.add(sentence.substring(0, lastSpace));
                    sentence = sentence.substring(lastSpace + 1).trim();
                }
            }
            if (!sentence.isEmpty()) {
                snippets.add(sentence);
            }
        }

        LOG.info("Transformed text into [{}] branch snippets", snippets.size());

        return snippets;
    }

    public List<List<String>> tokenizeToStringTokens(String text) {
        LOG.info("Tokenizing text [{}]", text);
        final List<List<String>> tokens = new ArrayList<>();

        final String[] words = text.toLowerCase().split("(?<=\\b|[^a-zA-Z0-9])");

        for (final String word : words) {
            if (isPunctuation(word)) {
                tokens.add(Collections.singletonList(word));
            } else if (word.matches("[0-9]+")) {
                tokens.add(tokenizeNumber(word));
            } else {
                tokens.add(tokenizeWord(word));
            }
        }

        return tokens;
    }

    private boolean isPunctuation(String word) {
        return word.length() == 1 && "!?,.;: ".contains(word);
    }

    private List<String> tokenizeWord(String word) {
        final List<String> tokens = new ArrayList<>();
        final List<List<String>> phoneticTranscriptions = dictionary.get(word);

        if (phoneticTranscriptions == null) {
            // find the largest substring of the word that is in the dictionary and repeat until the word is fully tokenized
            LOG.warn("Word not found in dictionary, parsing as subtokens: {}", word);
            String remainingWord = word;
            while (!remainingWord.isEmpty()) {
                boolean found = false;
                for (int i = remainingWord.length(); i > 0; i--) {
                    final String substring = remainingWord.substring(0, i);
                    if (dictionary.containsKey(substring)) {
                        final List<String> subtokens = tokenizeWord(substring);
                        tokens.addAll(subtokens);
                        remainingWord = remainingWord.substring(i);
                        LOG.info("Found substring in dictionary: [{}] -> {}", substring, subtokens);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LOG.warn("Could not tokenize word: {}", word);
                    tokens.add(word);
                    remainingWord = "";
                }
            }

            return tokens;
        }

        final List<String> phoneticTranscription = phoneticTranscriptions.get(0);

        for (final String phoneticSymbol : phoneticTranscription) {
            final String customLanguageSymbol = conversionTable.get(phoneticSymbol);

            if (customLanguageSymbol == null) {
                LOG.warn("Phonetic symbol not found in conversion table: {}", phoneticSymbol);
                continue;
            }

            tokens.add(customLanguageSymbol);
        }

        return tokens;
    }

    /**
     * Converts a string of digit characters into a list of strings, where each string is one of: 1-9 and 10.
     *
     * @param number The number to tokenize.
     * @return The tokenized number.
     */
    private List<String> tokenizeNumber(String number) {
        final List<String> tokens = new ArrayList<>();

        for (int i = 0; i < number.length(); i++) {
            final char digit = number.charAt(i);
            final char nextDigit = i + 1 < number.length() ? number.charAt(i + 1) : ' ';

            if (digit == '1' && nextDigit == '0') {
                tokens.add("10");
                i++;
            } else {
                tokens.add(String.valueOf(digit));
            }
        }

        return tokens;
    }

    /**
     * Example:
     * <pre>
     * aalborg AO1 L B AO0 R G # place, danish
     * aalborg(2) AA1 L B AO0 R G
     * aalburg AE1 L B ER0 G
     * </pre>
     * <p>
     * Should be converted to:
     * <pre>
     * aalborg: [[AO1 L B AO0 R G], [AA1 L B AO0 R G]]
     * aalburg: [[AE1 L B ER0 G]]
     * </pre>
     *
     * @param file The dictionary file to load.
     * @return The loaded dictionary.
     * @throws IOException If the dictionary file could not be read.
     */
    private static Map<String, List<List<String>>> loadDictionary(File file) throws IOException {
        final List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);

        return loadDictionary(lines);
    }

    private static Map<String, String> loadConversionTable(File conversionTableFile) throws IOException {
        final List<String> lines = FileUtils.readLines(conversionTableFile, StandardCharsets.UTF_8);

        return loadConversionTable(lines);
    }

    private static Map<String, List<List<String>>> loadDictionary(Class<?> clazz, String resource) throws IOException {
        try (InputStream inputStream = clazz.getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Dictionary resource not found: " + resource);
            }
            final List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);

            return loadDictionary(lines);
        }
    }

    private static Map<String, String> loadConversionTable(Class<?> clazz, String resource) throws IOException {
        try (InputStream inputStream = clazz.getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Conversion table resource not found: " + resource);
            }
            final List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);

            return loadConversionTable(lines);
        }
    }

    private static Map<String, List<List<String>>> loadDictionary(List<String> lines) {
        final Map<String, List<List<String>>> dictionary = new HashMap<>();

        for (String line : lines) {
            line = line.split("#", 2)[0].trim();

            if (!line.isEmpty()) {
                final String[] parts = line.split(" ", 2);
                final String word = parts[0].replaceAll("\\(\\d+\\)$", "");
                final List<String> phonetic = Arrays.asList(parts[1].split(" "));

                dictionary.computeIfAbsent(word, k -> new ArrayList<>()).add(phonetic);
            }
        }

        return dictionary;
    }

    private static Map<String, String> loadConversionTable(List<String> lines) {
        final Map<String, String> conversionTable = new HashMap<>();

        for (String line : lines) {
            final String[] parts = line.split(" ", 2);
            final String phoneticSymbol = parts[0];
            final String customLanguageSymbol = parts[1];

            conversionTable.put(phoneticSymbol, customLanguageSymbol);
        }

        return conversionTable;
    }

}
