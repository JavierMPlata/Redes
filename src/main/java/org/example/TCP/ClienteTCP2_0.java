package org.example.TCP;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClienteTCP2_0 extends JFrame {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String clientName;

    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel messageStatusIndicator;
    private JButton disconnectButton;
    private JButton reconnectButton;

    enum MessageStatus {
        SENT_AND_RECEIVED, SENDING, SERVER_CLOSED, DISCONNECTED
    }

    public ClienteTCP2_0() {
        setTitle("Cliente TCP");
        setSize(550, 430);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        messageStatusIndicator = new JLabel();
        messageStatusIndicator.setOpaque(true);
        messageStatusIndicator.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Agregar un borde

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(messageStatusIndicator, BorderLayout.CENTER);
        statusPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(statusPanel, BorderLayout.EAST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        disconnectButton = new JButton("Desconectar");
        disconnectButton.addActionListener(e -> disconnectFromServer());
        buttonPanel.add(disconnectButton);

        reconnectButton = new JButton("Reconectar");
        reconnectButton.addActionListener(e -> reconnectToServer());
        buttonPanel.add(reconnectButton);

        inputPanel.add(buttonPanel, BorderLayout.WEST);

        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            if (socket == null || socket.isClosed()) {
                JPanel panel = new JPanel(new GridLayout(3, 2));
                JTextField serverAddressField = new JTextField();
                JTextField clientNameField = new JTextField();
                panel.add(new JLabel("Dirección IP del servidor:"));
                panel.add(serverAddressField);
                panel.add(new JLabel("Nombre del cliente:"));
                panel.add(clientNameField);
                int result = JOptionPane.showConfirmDialog(null, panel, "Conectar al servidor", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String serverAddress = serverAddressField.getText();
                    clientName = clientNameField.getText();
                    socket = new Socket(serverAddress, 5001);
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(clientName);
                    receiveMessages();
                } else {
                    showErrorAndExit("Conexión cancelada por el usuario.");
                }
            } else {
                dos.writeUTF("Reconectar");
                updateMessageStatusIndicator(MessageStatus.SENT_AND_RECEIVED);
            }
        } catch (IOException e) {
            showErrorAndExit("Error al reconectar con el servidor.");
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        showMessage(clientName + ": " + message);
        updateMessageStatusIndicator(MessageStatus.SENDING);
        try {
            dos.writeUTF(message);
            updateMessageStatusIndicator(MessageStatus.SENT_AND_RECEIVED);
            if (message.equalsIgnoreCase("Terminar")) {
                showMessage("Terminando la conexión con el servidor...");
                socket.close();
                System.exit(0);
            }
        } catch (IOException e) {
            updateMessageStatusIndicator(MessageStatus.SERVER_CLOSED);
            showErrorAndExit("Error 504: No se puede conectar con el servidor.");
            e.printStackTrace();
        }
        messageField.setText("");
    }

    private void receiveMessages() {
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String message = dis.readUTF();
                    showMessage(message);
                }
            } catch (IOException e) {
                showErrorAndExit("Error 504: No se puede conectar con el servidor.");
                e.printStackTrace();
            }
        });
        receiveThread.start();
    }

    private void disconnectFromServer() {
        try {
            dos.writeUTF("Desconectar");
            updateMessageStatusIndicator(MessageStatus.DISCONNECTED);
        } catch (IOException e) {
            showErrorAndExit("Error al desconectar del servidor.");
            e.printStackTrace();
        }
    }

    private void reconnectToServer() {
        try {
            dos.writeUTF("Reconectar");
            updateMessageStatusIndicator(MessageStatus.SENT_AND_RECEIVED);
        } catch (IOException e) {
            showErrorAndExit("Error al reconectar con el servidor.");
            e.printStackTrace();
        }
    }

    private void terminateConnection() {
        try {
            dos.writeUTF("Terminar");
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            showErrorAndExit("Error al terminar la conexión con el servidor.");
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    private void showErrorAndExit(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(ClienteTCP2_0.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        });
    }

    private void updateMessageStatusIndicator(MessageStatus status) {
        SwingUtilities.invokeLater(() -> {
            switch (status) {
                case SENT_AND_RECEIVED:
                    messageStatusIndicator.setBackground(Color.GREEN);
                    messageStatusIndicator.setText("Enviado y recibido");
                    break;
                case SENDING:
                    messageStatusIndicator.setBackground(Color.YELLOW);
                    messageStatusIndicator.setText("Se está enviando");
                    break;
                case SERVER_CLOSED:
                    messageStatusIndicator.setBackground(Color.RED);
                    messageStatusIndicator.setText("Se cerró el servidor");
                    break;
                case DISCONNECTED:
                    messageStatusIndicator.setBackground(Color.BLUE);
                    messageStatusIndicator.setText("Desconectado");
                    break;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteTCP());
    }
}