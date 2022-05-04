package org.intellij.sdk.regexp;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates formatted hint.
 */
public class HintCreator {

    private List<String> types;

    private List<String> regexpHint;

    public HintCreator() {
        regexpHint = new ArrayList<>(5);
        types = createTypes();
        initializeRegexpHint();
        initializeRegexpHintSecond();
    }

    public List<String> getTypes() {
        return types;
    }

    private List<String> createTypes() {
        List<String> types = new ArrayList<>(5);
        types.add("Character classes");
        types.add("Anchors");
        types.add("Escaped characters");
        types.add("Groups and lookaround");
        types.add("Quantifiers and alternation");
        return types;
    }

    private void initializeRegexpHint() {
        StringBuilder build = new StringBuilder();
        build.append(".        any character except newline\n");
        build.append("\\w\\d\\s        word, digit, whitespace\n");
        build.append("\\W\\D\\S    not word, digit, whitespace\n");
        build.append("[abc]               any of a, b, or c\n");
        build.append("[^abc]                 not a, b, or c\n");
        build.append("[a-g]       character between a & g\n\n");
        regexpHint.add(build.toString());
        build = new StringBuilder();
        build.append("^abc$       start / end of the string\n");
        build.append("\\b\\B          word, not-word boundary\n");
        regexpHint.add(build.toString());
        build = new StringBuilder();
        build.append("\\.\\*\\\\     escaped special characters\n");
        build.append("\\t\\n\\r    tab,linefeed,carriage return\n");
        regexpHint.add(build.toString());
    }

    private void initializeRegexpHintSecond() {
        StringBuilder build = new StringBuilder();
        build.append("(abc)                   capture group\n");
        build.append("\\1          backreference to group #1\n");
        build.append("(?:abc)           non-capturing group\n");
        build.append("(?=abc)            positive lookahead\n");
        build.append("(?!abc)            negative lookahead\n");
        regexpHint.add(build.toString());
        build = new StringBuilder();
        build.append("a*a+a?   0 or more, 1 or more, 0 or 1\n");
        build.append("a{5}a{2,}    exactly five, two or more\n");
        build.append("a{1,3}              between one & three\n");
        build.append("a+?a{2,}?    match as few as possible\n");
        build.append("ab|cd                      match ab or cd\n");
        regexpHint.add(build.toString());
    }


    public List<String> getRegexpHint() {
        return regexpHint;
    }
}
