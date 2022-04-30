package org.intellij.sdk.regexp;

import com.intellij.codeInsight.highlighting.HighlightManager;
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
import org.intellij.lang.regexp.RegExpLanguage;
import org.jetbrains.annotations.NotNull;
import org.intellij.lang.regexp.*;

import javax.swing.*;
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

  private final Project project;

  private LanguageTextField myRegExpTextField;

  private EditorTextField myTestsTextField;
  private final List<RangeHighlighter> myTestsHighlights = new ArrayList<>();

  public RegExpToolWindow(ToolWindow toolWindow, Project project) {
    this.project = project;
    explanationLabel.setText("Regular expression explanation");
    usersRegExpLabel.setText("Your RegExp");
    testLabel.setText("Test strings");
    myRegExpTextField.setFontInheritedFromLAF(true);
    usersRegExpLabel.setLabelFor(myRegExpTextField);
    testLabel.setLabelFor(myTestsTextField);
  }
  private void initializeRegExpTextField() {
    myRegExpTextField = new LanguageTextField(RegExpLanguage.INSTANCE, project, "", true) {
      @Override
      public @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setHorizontalScrollbarVisible(true);
        editor.setHorizontalScrollbarVisible(false);
        editor.getSettings().setLineNumbersShown(false);
        editor.getSettings().setAutoCodeFoldingEnabled(false);
        editor.getSettings().setFoldingOutlineShown(false);
        editor.getSettings().setAllowSingleLogicalLineFolding(false);

        return editor;
      }
    };
  }

  private void initializeTestsTextField() {
    myTestsTextField = new EditorTextField("sampleText", project, PlainTextFileType.INSTANCE);
  }

  private void createUIComponents() {
    initializeRegExpTextField();
    initializeTestsTextField();
    DocumentListener documentListener = new DocumentListener() {
      @Override
      public void documentChanged(@NotNull DocumentEvent event) {
        DocumentListener.super.documentChanged(event);
        updateTestsHighlights();
      }
    };
    myRegExpTextField.addDocumentListener(documentListener);
    myTestsTextField.addDocumentListener(documentListener);
  }

  private void updateTestsHighlights() {
    HighlightManager highlightManager = HighlightManager.getInstance(project);

    removeTestsHighlights(highlightManager);

    highlightMatches(highlightManager);
  }

  private void highlightMatches(HighlightManager highlightManager) {
    Editor testsEditor = myTestsTextField.getEditor();
    if (testsEditor == null) {
      return;
    }

    Pattern pattern;
    try {
      pattern = Pattern.compile(myRegExpTextField.getText());
    } catch(PatternSyntaxException ex) {
      // regex error
      // TODO description icon
        //  ex.getDescription()
      // ex.getIndex()
      return;
    }

    Matcher matcher = pattern.matcher(myTestsTextField.getText());
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

  public JPanel getContent() {
    return myToolWindowContent;
  }
}
