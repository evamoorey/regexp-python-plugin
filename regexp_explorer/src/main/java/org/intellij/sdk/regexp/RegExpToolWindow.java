package org.intellij.sdk.regexp;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.util.ui.JBUI;
import org.intellij.lang.regexp.RegExpHighlighter;
import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.intention.CheckRegExpForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpToolWindow {

    private JLabel explanationLabel;
    private JLabel usersRegExpLabel;
    private JLabel testLabel;
    private JPanel myToolWindowContent;
    private JComboBox hintComboBox;
    private JPanel hintTable;

    private final Project myProject;

    private LanguageTextField myRegExpTextField;
    private EditorTextField myTestsTextField;

    private final JBLabel myRegExpIcon;
    private final JBLabel myTestsIcon;

    private final List<RangeHighlighter> myTestsHighlights;

    public RegExpToolWindow(ToolWindow toolWindow, Project project) {
        this.myProject = project;
        this.myTestsHighlights = new ArrayList<>();

        this.myRegExpIcon = new JBLabel();
        this.myTestsIcon = new JBLabel();

        explanationLabel.setText("Regular expression explanation");

        usersRegExpLabel.setText("Your RegExp");
        usersRegExpLabel.setLabelFor(myRegExpTextField);

        testLabel.setText("Test strings");
        testLabel.setLabelFor(myTestsTextField);

        myRegExpTextField.setFontInheritedFromLAF(true);
        addIcon(myRegExpTextField, myRegExpIcon);

        myTestsTextField.setFontInheritedFromLAF(true);
        myTestsTextField.setOneLineMode(false);
        myTestsTextField.setAutoscrolls(true);
        addIcon(myTestsTextField, myTestsIcon);

        myToolWindowContent.setBackground(toolWindow.getComponent().getBackground());
        initializeHintTable();

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                DocumentListener.super.documentChanged(event);
                updateTestsHighlights();
            }
        };
        myRegExpTextField.addDocumentListener(documentListener);
        myTestsTextField.addDocumentListener(documentListener);

        updateTestsHighlights();
    }

    private void initializeHintTable() {
        HintCreator creator = new HintCreator();
        hintComboBox.setModel(new DefaultComboBoxModel(creator.getTypes().toArray()));

        hintTable.setLayout(new GridLayout(creator.getRegexpHint(0).length, creator.getRegexpHint(0)[0].length, 5, 5));
        for (int i = 0; i < creator.getRegexpHint(0).length; i++) {
            for (int j = 0; j < creator.getRegexpHint(0)[0].length; j++) {
                hintTable.add(creator.getRegexpHint(0)[i][j]);
            }
        }
        hintTable.updateUI();

        hintComboBox.addActionListener(e -> {
            hintTable.removeAll();
            hintTable.setLayout(new GridLayout(creator.getRegexpHint(hintComboBox.getSelectedIndex()).length,
                    creator.getRegexpHint(hintComboBox.getSelectedIndex())[0].length, 5, 5));
            for (int i = 0; i < creator.getRegexpHint(hintComboBox.getSelectedIndex()).length; i++) {
                for (int j = 0; j < creator.getRegexpHint(hintComboBox.getSelectedIndex())[0].length; j++) {
                    hintTable.add(creator.getRegexpHint(hintComboBox.getSelectedIndex())[i][j]);
                }
            }
            hintTable.updateUI();
        });
    }

    private void updateTestsHighlights() {
        HighlightManager highlightManager = HighlightManager.getInstance(myProject);

        removeTestsHighlights(highlightManager);

        highlightMatches(highlightManager);
    }

    private void highlightMatches(HighlightManager highlightManager) {
        Editor testsEditor = myTestsTextField.getEditor();
        if (testsEditor == null) {
            return;
        }

        myRegExpIcon.setIcon(null);
        myRegExpIcon.setToolTipText(null);
        myTestsIcon.setIcon(null);
        myTestsIcon.setToolTipText(null);

        Pattern pattern;
        try {
            pattern = Pattern.compile(myRegExpTextField.getText());
        } catch (PatternSyntaxException ex) {
            myRegExpIcon.setIcon(AllIcons.General.BalloonError);
            myRegExpIcon.setToolTipText(ex.getDescription());
            return;
        }

        Matcher matcher = pattern.matcher(myTestsTextField.getText());
        if (matcher.find()) {
            myRegExpIcon.setIcon(AllIcons.General.InspectionsOK);
            myTestsIcon.setIcon(AllIcons.General.InspectionsOK);
        }
        if (matcher.hitEnd()) {
            myTestsIcon.setIcon(AllIcons.General.BalloonWarning);
            myTestsIcon.setToolTipText("Incomplete");
        }
        matcher.reset();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            highlightManager.addRangeHighlight(testsEditor, start, end, RegExpHighlighter.MATCHED_GROUPS, true, myTestsHighlights);
        }
    }

    private void removeTestsHighlights(HighlightManager highlightManager) {
        Editor testsEditor = myTestsTextField.getEditor();
        if (testsEditor != null) {
            for (RangeHighlighter rangeHighlighter : myTestsHighlights) {
                highlightManager.removeSegmentHighlighter(testsEditor, rangeHighlighter);
            }
            myTestsHighlights.clear();
        }
    }

    private void addIcon(EditorTextField textField, JBLabel icon) {
        textField.addSettingsProvider(editor -> {
            icon.setBorder(JBUI.Borders.emptyLeft(2));
            final JScrollPane scrollPane = editor.getScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setBackground(editor.getBackgroundColor());
            verticalScrollBar.add(JBScrollBar.LEADING, icon);
            verticalScrollBar.setOpaque(true);
        });
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void createUIComponents() {
        myRegExpTextField = new LanguageTextField(RegExpLanguage.INSTANCE, this.myProject, "[22]TEXT(", false) {
            @Override
            public @NotNull EditorEx createEditor() {
                EditorEx editor = super.createEditor();
                editor.setHorizontalScrollbarVisible(true);
                editor.setHorizontalScrollbarVisible(false);
                editor.getSettings().setLineNumbersShown(false);
                editor.getSettings().setAutoCodeFoldingEnabled(false);
                editor.getSettings().setFoldingOutlineShown(false);
                editor.getSettings().setAllowSingleLogicalLineFolding(false);
                editor.putUserData(CheckRegExpForm.CHECK_REG_EXP_EDITOR, Boolean.TRUE);
                return editor;
            }
        };
        myTestsTextField = new EditorTextField("sampleText", myProject, PlainTextFileType.INSTANCE);
    }
}
