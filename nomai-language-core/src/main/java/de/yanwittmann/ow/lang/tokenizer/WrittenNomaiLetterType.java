package de.yanwittmann.ow.lang.tokenizer;

public enum WrittenNomaiLetterType {
    CONSONANT(false, true),
    VOWEL(true, false),
    NUMBER(true, false),
    OTHER(false, false);

    private final boolean vowel;
    private final boolean consonant;

    WrittenNomaiLetterType(boolean vowel, boolean consonant) {
        this.vowel = vowel;
        this.consonant = consonant;
    }

    public boolean isVowel() {
        return vowel;
    }

    public boolean isConsonant() {
        return consonant;
    }
}
