package de.yanwittmann.ow.lang.tokenizer;

import de.yanwittmann.ow.lang.renderer.shapes.ShapeDefinitions;

public class WrittenNomaiTextLetter {
    private final WrittenNomaiTextSymbolType a;
    private final WrittenNomaiTextSymbolType b;

    private final String token;

    public WrittenNomaiTextLetter(String token, WrittenNomaiTextSymbolType a, WrittenNomaiTextSymbolType b) {
        this.a = a;
        this.b = b;
        this.token = token;
    }

    public WrittenNomaiTextSymbolType getA() {
        return a;
    }

    public WrittenNomaiTextSymbolType getB() {
        return b;
    }

    public ShapeDefinitions getShapeDefinition() {
        return ShapeDefinitions.fromCombinationOfTypes(getFirst(), getSecond());
    }

    public WrittenNomaiTextSymbolType getFirst() {
        if (getType().isConsonant()) {
            return b == null ? a : b;
        } else {
            return a == null ? b : a;
        }
    }

    public WrittenNomaiTextSymbolType getSecond() {
        if (getType().isConsonant()) {
            return a;
        } else {
            return b;
        }
    }

    public String getToken() {
        return token;
    }

    public boolean isSpecialCharacter() {
        return a == null && b == null;
    }

    @Override
    public String toString() {
        return isSpecialCharacter() ?
                "[" + token + "]" :
                "[" + token + " => " + (getFirst() == null ? "_" : getFirst()) + " " + (getSecond() == null ? "_" : getSecond()) + " " + getShapeDefinition() + "]";
    }

    public WrittenNomaiLetterType getType() {
        switch (token) {
            case "aah":
            case "iy":
            case "ay":
            case "ah":
            case "oh":
            case "eee":
            case "oo":
            case "eh":
            case "uh":
            case "oy":
                return WrittenNomaiLetterType.VOWEL;

            case "p":
            case "b":
            case "m":
            case "w":
            case "v":
            case "f":
            case "th":
            case "l":
            case "ch":
            case "sh":
            case "ih":
            case "z":
            case "s":
            case "j":
            case "t":
            case "n":
            case "d":
            case "r":
            case "y":
            case "k":
            case "g":
            case "ng":
            case "h":
                return WrittenNomaiLetterType.CONSONANT;

            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "10":
                return WrittenNomaiLetterType.NUMBER;

            default:
                return WrittenNomaiLetterType.OTHER;
        }
    }

    public static WrittenNomaiTextLetter fromToken(String token, boolean throwException) {
        switch (token) {
            case "p":
                return new WrittenNomaiTextLetter(token, null, WrittenNomaiTextSymbolType.LINE);
            case "b":
                return new WrittenNomaiTextLetter(token, null, WrittenNomaiTextSymbolType.BEND);
            case "m":
                return new WrittenNomaiTextLetter(token, null, WrittenNomaiTextSymbolType.SQUARE);
            case "w":
                return new WrittenNomaiTextLetter(token, null, WrittenNomaiTextSymbolType.PENTAGON);
            case "v":
                return new WrittenNomaiTextLetter(token, null, WrittenNomaiTextSymbolType.HEXAGON);
            case "f":
                return new WrittenNomaiTextLetter(token, null, WrittenNomaiTextSymbolType.OCTAGON);
            case "th":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.SQUARE);
            case "l":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.PENTAGON);
            case "ch":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.HEXAGON);
            case "sh":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.LINE, WrittenNomaiTextSymbolType.OCTAGON);
            case "ih":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.BEND, null);
            case "z":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.SQUARE);
            case "s":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.PENTAGON);
            case "j":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.HEXAGON);
            case "t":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.BEND, WrittenNomaiTextSymbolType.OCTAGON);
            case "aah":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.LINE);
            case "iy":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.BEND);
            case "ay":
            case "n":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.SQUARE);
            case "d":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.PENTAGON);
            case "r":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.HEXAGON);
            case "y":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, WrittenNomaiTextSymbolType.OCTAGON);
            case "ah":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.LINE);
            case "oh":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.BEND);
            case "k":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.PENTAGON);
            case "g":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.HEXAGON);
            case "ng":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.OCTAGON);
            case "eee":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.LINE);
            case "oo":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.BEND);
            case "h":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.HEXAGON);
            case "eh":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.OCTAGON, WrittenNomaiTextSymbolType.LINE);
            case "uh":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.OCTAGON, WrittenNomaiTextSymbolType.BEND);
            case "oy":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.OCTAGON);

            case "0":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.SQUARE, null);
            case "1":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, null);
            case "2":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, null);
            case "3":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.OCTAGON, null);
            case "4":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.SQUARE);
            case "5":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.SQUARE);
            case "6":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.OCTAGON, WrittenNomaiTextSymbolType.SQUARE);
            case "7":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.PENTAGON, WrittenNomaiTextSymbolType.PENTAGON);
            case "8":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.PENTAGON);
            case "9":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.OCTAGON, WrittenNomaiTextSymbolType.PENTAGON);
            case "10":
                return new WrittenNomaiTextLetter(token, WrittenNomaiTextSymbolType.HEXAGON, WrittenNomaiTextSymbolType.HEXAGON);

            default:
                if (throwException) {
                    throw new IllegalArgumentException("Invalid token: " + token);
                } else {
                    return new WrittenNomaiTextLetter(token, null, null);
                }
        }
    }

}
