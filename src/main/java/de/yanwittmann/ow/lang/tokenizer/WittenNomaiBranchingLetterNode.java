package de.yanwittmann.ow.lang.tokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class WittenNomaiBranchingLetterNode {

    private static final Logger LOG = LogManager.getLogger(WittenNomaiBranchingLetterNode.class);

    private WrittenNomaiTextLetter letter;

    private WittenNomaiBranchingLetterNode nextWord;

    private WittenNomaiBranchingLetterNode vowel;
    private WittenNomaiBranchingLetterNode consonant;
    private WittenNomaiBranchingLetterNode number;

    public WittenNomaiBranchingLetterNode(WrittenNomaiTextLetter letter) {
        this.letter = letter;
    }

    public WittenNomaiBranchingLetterNode getDeepestConsonant() {
        if (consonant == null) return this;
        return consonant.getDeepestConsonant();
    }

    public WittenNomaiBranchingLetterNode getDeepestRoot() {
        WittenNomaiBranchingLetterNode current = this;
        WittenNomaiBranchingLetterNode currentDeepestRoot = this;
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

    private static WittenNomaiBranchingLetterNode fromSentenceInternal(List<WrittenNomaiTextLetter> sentence) {
        final WittenNomaiBranchingLetterNode root = new WittenNomaiBranchingLetterNode(null);

        WittenNomaiBranchingLetterNode currentVowelOrNumberBranch = root;
        WittenNomaiBranchingLetterNode currentConsonantBranch = root;
        WrittenNomaiLetterType lastType = null;
        // append to currentNode until token type changes

        for (WrittenNomaiTextLetter letter : sentence) {
            final WrittenNomaiLetterType currentLetterType = letter.getType();
            final WittenNomaiBranchingLetterNode letterNode = new WittenNomaiBranchingLetterNode(letter);

            if (lastType != currentLetterType) {
                lastType = currentLetterType;
                currentVowelOrNumberBranch = null;
            }

            final WittenNomaiBranchingLetterNode appendToNode;

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

    public static WittenNomaiBranchingLetterNode fromSentence(List<List<WrittenNomaiTextLetter>> sentence) {
        final List<WittenNomaiBranchingLetterNode> flatNodes = sentence.stream().map(WittenNomaiBranchingLetterNode::fromSentenceInternal).collect(Collectors.toList());
        // LOG.info("flatNodes: {}", flatNodes.stream().map(WittenNomaiBranchingLetterNode::toString).collect(Collectors.joining("\n")));

        if (flatNodes.isEmpty()) {
            return new WittenNomaiBranchingLetterNode(null);
        }

        final WittenNomaiBranchingLetterNode root = flatNodes.get(0);
        WittenNomaiBranchingLetterNode currentNode = root;

        for (int i = 1; i < flatNodes.size(); i++) {
            final WittenNomaiBranchingLetterNode node = flatNodes.get(i);

            if (node.getLetterType() == WrittenNomaiLetterType.NUMBER || node.hasNumber()) {
                currentNode.setNumber(node.getNumber());
            } else {
                final WittenNomaiBranchingLetterNode deepestConsonant = currentNode.getDeepestConsonant();
                deepestConsonant.setNextWord(node);

                if (currentNode.hasNumber()) {
                    node.setNumber(currentNode.getNumber());
                    currentNode.setNumber(null);
                }

                currentNode = node.getDeepestConsonant();
            }
        }

        if (currentNode.hasNumber()) {
            WittenNomaiBranchingLetterNode deepestRoot = root.getDeepestRoot();
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

    public WittenNomaiBranchingLetterNode getVowel() {
        return vowel;
    }

    public void setVowel(WittenNomaiBranchingLetterNode vowel) {
        this.vowel = vowel;
    }

    public WittenNomaiBranchingLetterNode getConsonant() {
        return consonant;
    }

    public void setConsonant(WittenNomaiBranchingLetterNode consonant) {
        this.consonant = consonant;
    }

    public WittenNomaiBranchingLetterNode getNumber() {
        return number;
    }

    public void setNumber(WittenNomaiBranchingLetterNode number) {
        this.number = number;
    }

    public WittenNomaiBranchingLetterNode getNextWord() {
        return nextWord;
    }

    public void setNextWord(WittenNomaiBranchingLetterNode nextWord) {
        this.nextWord = nextWord;
    }
}
