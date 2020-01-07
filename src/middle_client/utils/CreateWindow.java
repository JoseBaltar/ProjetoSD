package middle_client.utils;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import middle_client.models.EventModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

public class CreateWindow {
    private static JFrame guiFrame;
    private static JTextField displayServerConnection;
    private static JTextArea display;
    private static JPanel eventPanel;
    private static JPanel eventOnePanel;
    private static JPanel eventTwoPanel;
    private static JPanel eventThreePanel;
    private static JScrollPane addEventOneScroll;
    private static JScrollPane addEventTwoScroll;
    private static JScrollPane addEventThreeScroll;
    private static JButton eventOneStop;
    private static JButton eventTwoStop;
    private static JButton eventThreeStop;
    private static JTextArea displayEventThree;
    private static JTextArea displayEventTwo;
    private static JTextArea displayEventOne;

    public static void createActiveEventListWindow() {
        guiFrame = new JFrame();

        guiFrame.setTitle("Middle-Client Display");
        guiFrame.setLocationByPlatform(true);
        guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        guiFrame.setPreferredSize(new Dimension(450, 600));

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(1, 2));
        displayServerConnection = new JTextField();
        displayServerConnection.setEditable(false);
        display = new JTextArea();
        display.setEditable(false);
        display.setFont(new Font("monospaced", Font.PLAIN, 12));
        display.setPreferredSize(new Dimension(300, 100));
        displayPanel.add(display);
        displayPanel.add(displayServerConnection);

        eventPanel = new JPanel();
        eventPanel.setLayout(new GridLayout(3, 1));

        eventOnePanel = new JPanel();
        eventOnePanel.setLayout(new GridLayout(1, 2));
        displayEventOne = new JTextArea(10, 30);
        displayEventOne.setEditable(false);
        displayEventOne.setLineWrap(true);
        displayEventOne.setWrapStyleWord(true);
        displayEventOne.setFont(new Font("monospaced", Font.PLAIN, 12));
        displayEventOne.setText("(Danger Level 1 event section)");
        addEventOneScroll = new JScrollPane(displayEventOne);
        eventOneStop = new JButton("Stop");
        eventOneStop.setToolTipText("Stop this event!");
        eventOnePanel.add(displayEventOne);
        eventOnePanel.add(eventOneStop);
        eventOnePanel.setPreferredSize(new Dimension(450, 150));

        eventTwoPanel = new JPanel();
        eventTwoPanel.setLayout(new GridLayout(1, 2));
        displayEventTwo = new JTextArea(10, 30);
        displayEventTwo.setEditable(false);
        displayEventTwo.setText("(Danger Level 2 event section)");
        displayEventTwo.setLineWrap(true);
        displayEventTwo.setWrapStyleWord(true);
        displayEventTwo.setFont(new Font("monospaced", Font.PLAIN, 12));
        addEventTwoScroll = new JScrollPane(displayEventTwo);
        eventTwoStop = new JButton("Stop");
        eventTwoStop.setToolTipText("Stop this event!");
        eventTwoPanel.add(displayEventTwo);
        eventTwoPanel.add(eventTwoStop);
        eventTwoPanel.setPreferredSize(new Dimension(450, 150));

        eventThreePanel = new JPanel();
        eventThreePanel.setLayout(new GridLayout(1, 2));
        displayEventThree = new JTextArea(10, 30);
        displayEventThree.setEditable(false);
        displayEventThree.setText("(Danger Level 3 event section)");
        displayEventThree.setLineWrap(true);
        displayEventThree.setWrapStyleWord(true);
        displayEventThree.setFont(new Font("monospaced", Font.PLAIN, 12));
        addEventThreeScroll = new JScrollPane(displayEventThree);
        eventThreeStop = new JButton("Stop");
        eventThreeStop.setToolTipText("Stop this event!");
        eventThreePanel.add(displayEventThree);
        eventThreePanel.add(eventThreeStop);
        eventThreePanel.setPreferredSize(new Dimension(450, 150));

        guiFrame.add(displayPanel, BorderLayout.NORTH);
        guiFrame.add(eventPanel, BorderLayout.SOUTH);

        guiFrame.pack();
        guiFrame.setVisible(true);
    }

    public static void setDisplayText(String text) {
        display.setText(text);
    }

    public static void setDisplayServerConnectionText(String text) {
        displayServerConnection.setText(text);
    }

    public static void addEventFinishWindow(EventModel event, EventTracking eventTracking, Thread stopEvent) {
        String text = "#####################\n       WARNING       \n\n"
                        + "EventName: " + event.getEventName()
                        + "\nDangerLevel: " + event.getSeverity()
                        + "\nDescription: " + event.getDescription()
                        + "\nEventStartDate: " + new Date(event.getInitime()).toString();
        switch (event.getSeverity()) {
            case 1:
                eventOneStop.addActionListener((ActionEvent e) -> {
                    eventTracking.removeActiveEvent(event);
                    eventPanel.remove(eventOnePanel);
                    stopEvent.interrupt();
                });
                displayEventOne.setText(text);
                eventOnePanel.add(addEventOneScroll);
                eventOnePanel.add(eventOneStop);
                eventPanel.add(eventOnePanel);
                break;
            case 2:
                eventTwoStop.addActionListener((ActionEvent e) -> {
                    eventTracking.removeActiveEvent(event);
                    eventPanel.remove(eventTwoPanel);
                    stopEvent.interrupt();
                });
                displayEventTwo.setText(text);
                eventTwoPanel.add(addEventTwoScroll);
                eventTwoPanel.add(eventTwoStop);
                eventPanel.add(eventTwoPanel);
                break;
            default:
                eventThreeStop.addActionListener((ActionEvent e) -> {
                    eventTracking.removeActiveEvent(event);
                    eventPanel.remove(eventThreePanel);
                    stopEvent.interrupt();
                });
                displayEventThree.setText(text);
                eventThreePanel.add(addEventThreeScroll);
                eventThreePanel.add(eventThreeStop);
                eventPanel.add(eventThreePanel);
        }
    }

    public static void dispose() {
        guiFrame.dispose();
    }
}