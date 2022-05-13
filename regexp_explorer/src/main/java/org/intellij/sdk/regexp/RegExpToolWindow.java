/*
 * Copyright 2022 Eva Galyuta and Sergey Nesterenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.sdk.regexp;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.internal.psiView.ViewerNodeDescriptor;
import com.intellij.internal.psiView.ViewerTreeBuilder;
import com.intellij.internal.psiView.ViewerTreeStructure;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.intellij.lang.regexp.RegExpHighlighter;
import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.intention.CheckRegExpForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.metal.MetalSplitPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpToolWindow {

    private JPanel myToolWindowContent;

    private TitledSeparator usersRegExpLabel;
    private JCheckBox multilineCheckBox;
    private LanguageTextField myRegExpTextField;
    private EditorTextField myTestsTextField;
    private final JBLabel myRegExpIcon;
    private final JBLabel myTestsIcon;

    private JSplitPane myExplanationSplit;
    private TitledSeparator explanationLabel;
    private Tree myPsiTree;
    private TitledSeparator testLabel;
    private JComboBox hintComboBox;
    private JPanel hintTable;
    private TitledSeparator quickReferenceLabel;
    private JSplitPane verticalSplit;
    private JSplitPane horizontalSplit;

    private final Project myProject;
    private final Disposable myDisposable;

    private final Alarm myAlarm;
    private final List<RangeHighlighter> myTestsHighlights;

    private final ViewerTreeBuilder myPsiTreeBuilder;

    private void createUIComponents() {
        myPsiTree = new Tree(new DefaultTreeModel(new DefaultMutableTreeNode()));

        myRegExpTextField = new LanguageTextField(RegExpLanguage.INSTANCE, this.myProject, "", false) {
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

    public RegExpToolWindow(ToolWindow toolWindow, Project project) {
        this.myProject = project;
        this.myTestsHighlights = new ArrayList<>();

        this.myRegExpIcon = new JBLabel();
        this.myTestsIcon = new JBLabel();

        myDisposable = Disposer.newDisposable();

        initializeTree(myPsiTree);

        myPsiTreeBuilder = new ViewerTreeBuilder(myProject, myPsiTree);
        Disposer.register(myDisposable, myPsiTreeBuilder);

        usersRegExpLabel.setLabelFor(myRegExpTextField);
        explanationLabel.setLabelFor(myPsiTree);

        testLabel.setText("Test Strings");
        testLabel.setLabelFor(myTestsTextField);

        myRegExpTextField.setFontInheritedFromLAF(true);
        addIcon(myRegExpTextField, myRegExpIcon);

        myTestsTextField.setFontInheritedFromLAF(true);
        myTestsTextField.setOneLineMode(false);
        myTestsTextField.setAutoscrolls(true);
        addIcon(myTestsTextField, myTestsIcon);

        registerFocusShortcut(myRegExpTextField, "shift TAB", myTestsTextField);
        registerFocusShortcut(myTestsTextField, "shift TAB", myRegExpTextField);

        myToolWindowContent.setBackground(toolWindow.getComponent().getBackground());
        initializeHintTable();

        myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, myDisposable);
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                scheduleAllFieldsUpdate();
            }
        };
        myRegExpTextField.addDocumentListener(documentListener);
        myTestsTextField.addDocumentListener(documentListener);

        verticalSplit.setDividerSize(4);
        verticalSplit.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    public void setBorder(Border b) {
                    }

                    @Override
                    public void paint(Graphics g) {
                        g.setColor(myRegExpTextField.getBackground());
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }
                };
            }
        });
        verticalSplit.setBorder(null);

        horizontalSplit.setDividerSize(4);
        horizontalSplit.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    public void setBorder(Border b) {
                    }

                    @Override
                    public void paint(Graphics g) {
                        g.setColor(myRegExpTextField.getBackground());
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }
                };
            }
        });
        horizontalSplit.setBorder(null);

        multilineCheckBox.addChangeListener(e -> scheduleAllFieldsUpdate());

        scheduleAllFieldsUpdate();
    }

    private void registerFocusShortcut(JComponent source, String shortcut, EditorTextField target) {
        final AnAction action = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                IdeFocusManager.findInstance().requestFocus(target.getFocusTarget(), true);
            }
        };
        action.registerCustomShortcutSet(CustomShortcutSet.fromString(shortcut), source);
    }

    public static void initializeTree(JTree tree) {
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.updateUI();
        ToolTipManager.sharedInstance().registerComponent(tree);
        TreeUtil.installActions(tree);
        new TreeSpeedSearch(tree);

        final TreeCellRenderer renderer = tree.getCellRenderer();
        tree.setCellRenderer(new TreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                final Component c = renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObject instanceof ViewerNodeDescriptor) {
                        final Object element = ((ViewerNodeDescriptor) userObject).getElement();
                        if (c instanceof NodeRenderer) {
                            ((NodeRenderer) c).setToolTipText(element == null ? null : element.getClass().getName());
                        }
                        if (element instanceof PsiElement && FileContextUtil.getFileContext(((PsiElement) element).getContainingFile()) != null) {
                            final TextAttributes attr =
                                    EditorColorsManager.getInstance().getGlobalScheme().getAttributes(EditorColors.INJECTED_LANGUAGE_FRAGMENT);
                            c.setBackground(attr.getBackgroundColor());
                        }
                    }
                }
                return c;
            }
        });
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

    private void scheduleAllFieldsUpdate() {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(() -> ApplicationManager.getApplication().invokeLater(this::allFieldsUpdate, ModalityState.any(), __ -> myAlarm.isDisposed()), 0);
    }

    private void allFieldsUpdate() {
        HighlightManager highlightManager = HighlightManager.getInstance(myProject);

        removeTestsHighlights(highlightManager);

        updateMatchesAndTree(highlightManager);
    }

    private void updateMatchesAndTree(HighlightManager highlightManager) {
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
            pattern = Pattern.compile(myRegExpTextField.getText(), multilineCheckBox.isSelected() ? Pattern.MULTILINE : 0);
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

        EditorEx regexEditor = (EditorEx) myRegExpTextField.getEditor();
        if (regexEditor == null) {
            return;
        }
        FileType regexFileType = RegExpLanguage.INSTANCE.getAssociatedFileType();
        String ext = "regex";
        final PsiFile psiFile = PsiFileFactory.getInstance(myProject).createFileFromText("Dummy." + ext, regexFileType, myRegExpTextField.getText());
        //noinspection UnstableApiUsage
        ((ViewerTreeStructure) myPsiTreeBuilder.getTreeStructure()).setRootPsiElement((PsiElement) Arrays.stream(psiFile.getChildren()).toArray()[0]);

        //noinspection UnstableApiUsage
        myPsiTreeBuilder.queueUpdate().doWhenDone(() -> {
            for (int i = 0; i < myPsiTree.getRowCount(); i++) {
                myPsiTree.expandRow(i);
            }
        });

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
}
