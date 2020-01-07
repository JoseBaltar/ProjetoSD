package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Date;

public class CreateWindow {
    private static JFrame guiFrame;
    private static JTextArea display;
    private static JPanel eventPanel;
    private static JScrollPane addEventOneScroll;
    private static JScrollPane addEventTwoScroll;
    private static JScrollPane addEventThreeScroll;
    private static JTextArea displayEventThree;
    private static JTextArea displayEventTwo;
    private static JTextArea displayEventOne;

    public static void createActiveEventWindow() {
        guiFrame = new JFrame();

        guiFrame.setTitle("End-Client Display");
        guiFrame.setLocationByPlatform(true);
        guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        guiFrame.setPreferredSize(new Dimension(450, 600));

        display = new JTextArea();
        display.setEditable(false);
        display.setFont(new Font("monospaced", Font.PLAIN, 12));
        display.setPreferredSize(new Dimension(50, 100));

        eventPanel = new JPanel();
        eventPanel.setLayout(new GridLayout(3, 1));

        displayEventOne = new JTextArea(10, 30);
        displayEventOne.setEditable(false);
        displayEventOne.setLineWrap(true);
        displayEventOne.setWrapStyleWord(true);
        displayEventOne.setFont(new Font("monospaced", Font.PLAIN, 12));
        displayEventOne.setText("(Danger Level 1 event section)");
        addEventOneScroll = new JScrollPane(displayEventOne);
        addEventOneScroll.setPreferredSize(new Dimension(450, 150));

        displayEventTwo = new JTextArea(10, 30);
        displayEventTwo.setEditable(false);
        displayEventTwo.setText("(Danger Level 2 event section)");
        displayEventTwo.setLineWrap(true);
        displayEventTwo.setWrapStyleWord(true);
        displayEventTwo.setFont(new Font("monospaced", Font.PLAIN, 12));
        addEventTwoScroll = new JScrollPane(displayEventTwo);
        addEventTwoScroll.setPreferredSize(new Dimension(450, 150));

        displayEventThree = new JTextArea(10, 30);
        displayEventThree.setEditable(false);
        displayEventThree.setText("(Danger Level 3 event section)");
        displayEventThree.setLineWrap(true);
        displayEventThree.setWrapStyleWord(true);
        displayEventThree.setFont(new Font("monospaced", Font.PLAIN, 12));
        addEventThreeScroll = new JScrollPane(displayEventThree);
        addEventThreeScroll.setPreferredSize(new Dimension(450, 150));

        guiFrame.add(display, BorderLayout.NORTH);
        guiFrame.add(eventPanel, BorderLayout.SOUTH);

        guiFrame.pack();
        guiFrame.setVisible(true);
    }

    public static void setDisplayText(String text) {
        display.setText(text);
    }

    public static void showIncomingEvent(String location, String name, String description, long time, int level) {
        String text = "#####################\n       WARNING       \n\n"
                        + "EventName: " + name
                        + "\nDangerLevel: " + level
                        + "\nDescription: " + description
                        + "\nEventStartDate: " + new Date(time).toString();
        switch (level) {
            case 1:
                displayEventOne.setText(text);
                eventPanel.add(addEventOneScroll);
                break;
            case 2:
                displayEventTwo.setText(text);
                eventPanel.add(addEventTwoScroll);
                break;
            default:
                displayEventThree.setText(text);
                eventPanel.add(addEventThreeScroll);
        }
    }

    public static void dispose() {
        guiFrame.dispose();
    }
}