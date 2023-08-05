package de.yanwittmann.ow.lang;

import de.yanwittmann.ow.lang.renderer.LetterToLineConverter;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurve;
import de.yanwittmann.ow.lang.renderer.shapes.BezierCurveCoordinateSystem;
import de.yanwittmann.ow.lang.renderer.shapes.LetterShape;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiBranchingLetterNode;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextLetter;
import de.yanwittmann.ow.lang.tokenizer.WrittenNomaiTextTokenizer;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class WrittenNomaiConverter {

    private WrittenNomaiTextTokenizer tokenizer;
    private LetterToLineConverter lineGenerator;
    private Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider;

    public void setTokenizer(WrittenNomaiTextTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void setLineGenerator(LetterToLineConverter lineGenerator) {
        this.lineGenerator = lineGenerator;
    }

    public void setTransformAlongCurveProvider(Function<List<LetterShape>, BezierCurveCoordinateSystem> transformAlongCurveProvider) {
        this.transformAlongCurveProvider = transformAlongCurveProvider;
    }

    public WrittenNomaiBranchingLetterNode convertTextToNodeTree(String normalText) {
        final List<List<String>> tokens = this.tokenizer.tokenizeToStringTokens(normalText);
        final List<List<WrittenNomaiTextLetter>> words = this.tokenizer.convertStringTokensToLetters(tokens);
        return WrittenNomaiBranchingLetterNode.fromSentence(words);
    }

    public List<Object> convertNodeTreeToDrawables(WrittenNomaiBranchingLetterNode rootNode) {
        return lineGenerator.generateShapes(new Random(0), rootNode, transformAlongCurveProvider);
    }
}
