package org.intellij.sdk.regexp;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.util.Calendar;

public class RegExpToolWindow {

  private JLabel explanationLabel;
  private JLabel usersRegExpLabel;
  private JLabel testLabel;
  private JPanel myToolWindowContent;
  private JTextField usersRegexp;
  private JTextArea testsArea;

  public RegExpToolWindow(ToolWindow toolWindow) {
    this.currentDateTime();
  }

  public void currentDateTime() {
    explanationLabel.setText("Regular expression explanation");
    usersRegExpLabel.setText("Your RegExp");
    testLabel.setText("Test strings");
  }

  public JPanel getContent() {
    return myToolWindowContent;
  }

}
