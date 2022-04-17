package org.intellij.sdk.regexp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;

public class CheckRegexp extends AnAction {

    private JFrame frame;
    private JPanel panel;
    private TextField regexp;
    private Label textRegExp;
    private TextField test;
    private Label textTest;


    @Override
    public void actionPerformed(AnActionEvent e) {
        initialize(e);
    }

    private void initialize(AnActionEvent e){
        frame = new JFrame("RegExp helper");

        regexp = new TextField();
        regexp.setPreferredSize(new Dimension(200, 30));
        regexp.setBackground(frame.getBackground());
        textRegExp = new Label("Your RegExp: ");
        textRegExp.setSize(100, 30);
        textRegExp.setAlignment(Label.LEFT);

        test = new TextField();
        test.setPreferredSize(new Dimension(200, 30));
        test.setBackground(frame.getBackground());
        textTest = new Label("Test: ");
        textTest.setSize(100, 30);
        textTest.setAlignment(Label.LEFT);

        panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 150));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(textRegExp);
        panel.add(regexp);
        panel.add(textTest);
        panel.add(test);


        frame.add(panel);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
    }
}
