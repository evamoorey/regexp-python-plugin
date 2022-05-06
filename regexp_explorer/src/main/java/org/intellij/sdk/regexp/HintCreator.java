package org.intellij.sdk.regexp;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates formatted hint.
 */
public class HintCreator {

    private List<String> types;

    private JTextArea[][] regexpHint;

    public HintCreator() {
        regexpHint = new JTextArea[5][2];
        types = createTypes();
    }

    public List<String> getTypes() {
        return new ArrayList<>(types);
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

    private void initializeRegexpHint(int index) {
        switch (index) {
            case 0: {
                regexpHint = new JTextArea[6][2];
                regexpHint[0] = new JTextArea[]{new JTextArea("."), new JTextArea("any character    \nexcept newline")};
                regexpHint[1] = new JTextArea[]{new JTextArea("\\w\\d\\s"), new JTextArea("word, digit,     \nwhitespace")};
                regexpHint[2] = new JTextArea[]{new JTextArea("\\W\\D\\S"), new JTextArea("not word, digit, \nwhitespace")};
                regexpHint[3] = new JTextArea[]{new JTextArea("[abc]"), new JTextArea("any of a, b, or c")};
                regexpHint[4] = new JTextArea[]{new JTextArea("[^abc]"), new JTextArea("not a, b, or c")};
                regexpHint[5] = new JTextArea[]{new JTextArea("[a-g]"), new JTextArea("character between\na & g")};
                break;
            }
            case 1: {
                regexpHint = new JTextArea[6][2];
                regexpHint[0] = new JTextArea[]{new JTextArea("^abc$"), new JTextArea("start / end of   \nthe string")};
                regexpHint[1] = new JTextArea[]{new JTextArea("\\b\\B"), new JTextArea("word, not-word   \nboundary")};
                for (int i = 2; i < 6; i++) {
                    regexpHint[i] = new JTextArea[]{new JTextArea(""), new JTextArea("")};
                }
                break;
            }
            case 2: {
                regexpHint = new JTextArea[6][2];
                regexpHint[0] = new JTextArea[]{new JTextArea("\\.\\*\\\\"), new JTextArea("escaped special  \ncharacters")};
                regexpHint[1] = new JTextArea[]{new JTextArea("\\t\\n\\r"), new JTextArea("tab, linefeed,   \ncarriage return")};
                for (int i = 2; i < 6; i++) {
                    regexpHint[i] = new JTextArea[]{new JTextArea(""), new JTextArea("")};
                }
                break;
            }
        }
    }

    private void initializeRegexpHintSecond(int index) {
        switch (index) {
            case 3: {
                regexpHint = new JTextArea[6][2];
                regexpHint[0] = new JTextArea[]{new JTextArea("(abc)"), new JTextArea("capture group    ")};
                regexpHint[1] = new JTextArea[]{new JTextArea("\\1"), new JTextArea("backreference to \ngroup #1")};
                regexpHint[2] = new JTextArea[]{new JTextArea("(?:abc)"), new JTextArea("non-capturing    \ngroup")};
                regexpHint[3] = new JTextArea[]{new JTextArea("(?=abc)"), new JTextArea("positive\nlookahead")};
                regexpHint[4] = new JTextArea[]{new JTextArea("(?!abc)"), new JTextArea("negative\nlookahead")};
                regexpHint[5] = new JTextArea[]{new JTextArea(""), new JTextArea("")};
                break;
            }
            case 4: {
                regexpHint = new JTextArea[6][2];
                regexpHint[0] = new JTextArea[]{new JTextArea("a*a+a?"), new JTextArea("0 or more, 1 or  \nmore, 0 or 1")};
                regexpHint[1] = new JTextArea[]{new JTextArea("a{5}a{2,}"), new JTextArea("exactly five,two\nor more")};
                regexpHint[2] = new JTextArea[]{new JTextArea("a{1,3}"), new JTextArea("between one &    \nthree")};
                regexpHint[3] = new JTextArea[]{new JTextArea("a+?a{2,}"), new JTextArea("match as few as  \npossible")};
                regexpHint[4] = new JTextArea[]{new JTextArea("ab|cd"), new JTextArea("match ab or cd")};
                regexpHint[5] = new JTextArea[]{new JTextArea(""), new JTextArea("")};
                break;
            }
        }
    }

    public JTextArea[][] getRegexpHint(int index) {
        switch (index) {
            case 0: {
                initializeRegexpHint(index);
            }
            case 1: {
                initializeRegexpHint(index);
            }
            case 2: {
                initializeRegexpHint(index);
            }
            case 3: {
                initializeRegexpHintSecond(index);
            }
            case 4: {
                initializeRegexpHintSecond(index);
            }
        }
        Arrays.stream(regexpHint).forEach(jTextAreas -> Arrays.stream(jTextAreas).forEach(jTextArea -> jTextArea.setEditable(false)));
        return regexpHint;
    }
}
