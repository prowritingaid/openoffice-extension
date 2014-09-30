package com.prowritingaid.openoffice;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A dialog with version and copyright information.
 * 
 */
public class AboutDialog {

  protected final ResourceBundle messages;

  private final Component parent;

  public AboutDialog(final ResourceBundle messages, Component parent) {
    this.messages = messages;
    this.parent = parent;
  }

  public void show() {
    final String aboutText = "Hello";

    JTextPane aboutPane = new JTextPane();
    aboutPane.setBackground(new Color(0, 0, 0, 0));
    aboutPane.setBorder(BorderFactory.createEmptyBorder());
    aboutPane.setContentType("text/html");
    aboutPane.setEditable(false);
    aboutPane.setOpaque(false);

    aboutPane.setText(String.format("<html>"
            + "<p>LanguageTool %s (%s)<br>"
            + "Copyright (C) 2005-2014 the LanguageTool community and Daniel Naber<br>"
            + "This software is licensed under the GNU Lesser General Public License.<br>"
            + "<a href=\"http://www.languagetool.org\">http://www.languagetool.org</a></p>"
            + "<p>Maintainers of the language modules:</p><br>"
            + "</html>", "1.0", "01-Jan-2104"));

    aboutPane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
              //Tools.showError(ex);
            }
          }
        }
      }
    });

    JTextPane maintainersPane = new JTextPane();
    maintainersPane.setBackground(new Color(0, 0, 0, 0));
    maintainersPane.setBorder(BorderFactory.createEmptyBorder());
    maintainersPane.setContentType("text/html");
    maintainersPane.setEditable(false);
    maintainersPane.setOpaque(false);

    maintainersPane.setText(getMaintainers());

    int maxHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height / 2;
    if(maintainersPane.getPreferredSize().height > maxHeight) {
      maintainersPane.setPreferredSize(
                new Dimension(maintainersPane.getPreferredSize().width, maxHeight));
    }

    JScrollPane scrollPane = new JScrollPane(maintainersPane);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(aboutPane);
    panel.add(scrollPane);

    JOptionPane.showMessageDialog(parent, panel,
        aboutText, JOptionPane.INFORMATION_MESSAGE);
  }

  private String getMaintainers() {
    final StringBuilder maintainersInfo = new StringBuilder();
    maintainersInfo.append("<table border=0 cellspacing=0 cellpadding=0>");
    maintainersInfo.append("</table>");
    return maintainersInfo.toString();
  }

}
