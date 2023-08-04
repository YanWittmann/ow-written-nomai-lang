package de.yanwittmann.ow.lang.tokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class WrittenNomaiBranchingLetterNode {

    private static final Logger LOG = LogManager.getLogger(WrittenNomaiBranchingLetterNode.class);

    private WrittenNomaiTextLetter letter;

    private WrittenNomaiBranchingLetterNode nextWord;

    private WrittenNomaiBranchingLetterNode vowel;
    private WrittenNomaiBranchingLetterNode consonant;
    private WrittenNomaiBranchingLetterNode number;

    public WrittenNomaiBranchingLetterNode(WrittenNomaiTextLetter letter) {
        this.letter = letter;
    }

    public WrittenNomaiBranchingLetterNode getDeepestConsonant() {
        if (consonant == null) return this;
        return consonant.getDeepestConsonant();
    }

    public WrittenNomaiBranchingLetterNode getDeepestRoot() {
        WrittenNomaiBranchingLetterNode current = this;
        WrittenNomaiBranchingLetterNode currentDeepestRoot = this;
        while (current.hasNextWord() || current.hasConsonant()) {
            if (current.hasNextWord()) current = current.getNextWord();
            else current = current.getConsonant();
            if (!current.hasLetter()) currentDeepestRoot = current;
        }
        return currentDeepestRoot;
    }

    public WrittenNomaiLetterType getLetterType() {
        if (letter == null) return WrittenNomaiLetterType.OTHER;
        return letter.getType();
    }

    public boolean hasLetter() {
        return letter != null;
    }

    public boolean hasVowel() {
        return vowel != null;
    }

    public boolean hasConsonant() {
        return consonant != null;
    }

    public boolean hasNumber() {
        return number != null;
    }

    public boolean hasNextWord() {
        return nextWord != null;
    }

    private static WrittenNomaiBranchingLetterNode fromSentenceInternal(List<WrittenNomaiTextLetter> sentence) {
        final WrittenNomaiBranchingLetterNode root = new WrittenNomaiBranchingLetterNode(null);

        WrittenNomaiBranchingLetterNode currentVowelOrNumberBranch = root;
        WrittenNomaiBranchingLetterNode currentConsonantBranch = root;
        WrittenNomaiLetterType lastType = null;
        // append to currentNode until token type changes

        for (WrittenNomaiTextLetter letter : sentence) {
            final WrittenNomaiLetterType currentLetterType = letter.getType();
            final WrittenNomaiBranchingLetterNode letterNode = new WrittenNomaiBranchingLetterNode(letter);

            if (lastType != currentLetterType) {
                lastType = currentLetterType;
                currentVowelOrNumberBranch = null;
            }

            final WrittenNomaiBranchingLetterNode appendToNode;

            switch (currentLetterType) {
                case VOWEL:
                case NUMBER:
                    appendToNode = currentVowelOrNumberBranch == null ? currentConsonantBranch : currentVowelOrNumberBranch;
                    break;
                case CONSONANT:
                    appendToNode = currentConsonantBranch;
                    break;
                case OTHER:
                default:
                    continue;
            }

            switch (currentLetterType) {
                case VOWEL:
                    appendToNode.setVowel(letterNode);
                    currentVowelOrNumberBranch = letterNode;
                    break;
                case NUMBER:
                    appendToNode.setNumber(letterNode);
                    currentVowelOrNumberBranch = letterNode;
                    break;
                case CONSONANT:
                    appendToNode.setConsonant(letterNode);
                    currentConsonantBranch = letterNode;
                    currentVowelOrNumberBranch = null;
                    break;
            }
        }

        return root;
    }

    public static WrittenNomaiBranchingLetterNode fromSentence(List<List<WrittenNomaiTextLetter>> sentence) {
        final List<WrittenNomaiBranchingLetterNode> flatNodes = sentence.stream().map(WrittenNomaiBranchingLetterNode::fromSentenceInternal).collect(Collectors.toList());
        // LOG.info("flatNodes: {}", flatNodes.stream().map(WittenNomaiBranchingLetterNode::toString).collect(Collectors.joining("\n")));

        if (flatNodes.isEmpty()) {
            return new WrittenNomaiBranchingLetterNode(null);
        }

        final WrittenNomaiBranchingLetterNode root = flatNodes.get(0);
        WrittenNomaiBranchingLetterNode currentNode = root;

        for (int i = 1; i < flatNodes.size(); i++) {
            final WrittenNomaiBranchingLetterNode node = flatNodes.get(i);

            if (node.getLetterType() == WrittenNomaiLetterType.NUMBER || node.hasNumber()) {
                currentNode.setNumber(node.getNumber());
            } else {
                final WrittenNomaiBranchingLetterNode deepestConsonant = currentNode.getDeepestConsonant();
                deepestConsonant.setNextWord(node);

                if (currentNode.hasNumber()) {
                    node.setNumber(currentNode.getNumber());
                    currentNode.setNumber(null);
                }

                currentNode = node.getDeepestConsonant();
            }
        }

        if (currentNode.hasNumber()) {
            WrittenNomaiBranchingLetterNode deepestRoot = root.getDeepestRoot();
            deepestRoot.setNumber(currentNode.getNumber());
            currentNode.setNumber(null);
        }

        return root;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        return toString(stringBuilder, 0).toString();
    }

    private StringBuilder toString(StringBuilder stringBuilder, int depth) {
        if (letter != null) {
            stringBuilder.append("\n");
            stringBuilder.append("  ".repeat(Math.max(0, depth)));
            stringBuilder.append(letter.getType().toString().charAt(0)).append(" ").append(letter);
        } else {
            stringBuilder.append("\n");
            stringBuilder.append("  ".repeat(Math.max(0, depth)));
            stringBuilder.append("ROOT [")
                    .append(((hasVowel() ? "V " : "") +
                            (hasConsonant() ? "C " : "") +
                            (hasNumber() ? "N " : "") +
                            (hasNextWord() ? "W " : "")).trim())
                    .append("]");
        }
        if (vowel != null) {
            vowel.toString(stringBuilder, depth + 1);
        }
        if (number != null) {
            number.toString(stringBuilder, depth + 1);
        }
        if (consonant != null) {
            consonant.toString(stringBuilder, depth + 1);
        }
        if (nextWord != null) {
            stringBuilder.append("\n");
            nextWord.toString(stringBuilder, depth + 1);
        }
        return stringBuilder;
    }

    public boolean isRoot() {
        return letter == null;
    }

    public WrittenNomaiTextLetter getLetter() {
        return letter;
    }

    public void setLetter(WrittenNomaiTextLetter letter) {
        this.letter = letter;
    }

    public WrittenNomaiBranchingLetterNode getVowel() {
        return vowel;
    }

    public void setVowel(WrittenNomaiBranchingLetterNode vowel) {
        this.vowel = vowel;
    }

    public WrittenNomaiBranchingLetterNode getConsonant() {
        return consonant;
    }

    public void setConsonant(WrittenNomaiBranchingLetterNode consonant) {
        this.consonant = consonant;
    }

    public WrittenNomaiBranchingLetterNode getNumber() {
        return number;
    }

    public void setNumber(WrittenNomaiBranchingLetterNode number) {
        this.number = number;
    }

    public WrittenNomaiBranchingLetterNode getNextWord() {
        return nextWord;
    }

    public void setNextWord(WrittenNomaiBranchingLetterNode nextWord) {
        this.nextWord = nextWord;
    }
}
