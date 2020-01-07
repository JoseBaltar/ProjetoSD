package server.utils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class CreateWindow {
    private static JFrame guiFrame;
    private static JTextField displayServerConnection;
    private static JTextArea display;
    private static JTextArea activeConnections;

    public static void createActiveConnectionsListWindow() {
        guiFrame = new JFrame();

        guiFrame.setTitle("Main-Server Display");
        guiFrame.setLocationByPlatform(true);
        guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        guiFrame.setPreferredSize(new Dimension(450, 500));

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

        activeConnections = new JTextArea();
        activeConnections.setEditable(false);
        activeConnections.setFont(new Font("monospaced", Font.PLAIN, 12));

        guiFrame.add(displayPanel, BorderLayout.NORTH);
        guiFrame.add(activeConnections, BorderLayout.SOUTH);

        guiFrame.pack();
        guiFrame.setVisible(true);
    }

    public static void addActiveConnection(String address) {
        String text = activeConnections.getText();
        activeConnections.setText(text + "ActiveLocationAddress: " + address + "\n");
    }

    public static void removeActiveConnection(String address) {
        String text = activeConnections.getText().replaceAll("ActiveLocationAddress: " + address + "\n", "");
        activeConnections.setText(text);
    }

    public static void setDisplayText(String text) {
        display.setText(text);
    }

    public static void setDisplayServerConnectionText(String text) {
        displayServerConnection.setText(text);
    }

    public static void createActiveEventsListWindow() {

    }

    public static void addCurrentActiveEvents(EventTracking eventTracking) {

    }

    public static void addSingleActiveEvent(EventTracking eventTracking) {

    }

    public static void dispose() {
        guiFrame.dispose();
    }
}