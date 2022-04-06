package org.intellij.sdk.regexp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class CheckRegexp extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        JFrame frame = new JFrame("RegExp helper");

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(250, 150));
        frame.add(panel);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //frame.setSize(300, 200);
        frame.pack();
    }
}
