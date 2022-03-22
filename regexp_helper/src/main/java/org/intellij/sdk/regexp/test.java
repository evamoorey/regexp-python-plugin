package org.intellij.sdk.regexp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class test extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Project project = e.getProject();
        Messages.showMessageDialog(project,
                "Popup dialog action",
                "Hiiii!",
                Messages.getInformationIcon());
    }
}
