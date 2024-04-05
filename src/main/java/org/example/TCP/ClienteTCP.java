package org.example.TCP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClienteTCP extends JFrame {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String clientName; // Almacenar el nombre del cliente

    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel messageStatusIndicator;

    enum MessageStatus {
        SENT_AND_RECEIVED, SENDING, SERVER_CLOSED
    }

    public ClienteTCP() {
        setTitle("Cliente TCP");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        messageStatusIndicator = new JLabel();
        messageStatusIndicator.setOpaque(true);
        inputPanel.add(messageStatusIndicator, BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
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
            clientName = clientNameField.getText(); // Almacenar el nombre del cliente
            try {
                socket = new Socket(serverAddress, 5001);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(clientName); // Enviar el nombre del cliente al servidor
                receiveMessages();
            } catch (IOException e) {
                showErrorAndExit("Error 504: No se puede conectar con el servidor.");
                e.printStackTrace();
            }
        } else {
            showErrorAndExit("Conexión cancelada por el usuario.");
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        showMessage(clientName + ": " + message); // Mostrar el nombre del cliente junto con el mensaje enviado
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
        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = dis.readUTF();
                        showMessage(message); // Mostrar mensaje recibido del servidor
                    }
                } catch (IOException e) {
                    showErrorAndExit("Error 504: No se puede conectar con el servidor.");
                    e.printStackTrace();
                }
            }
        });
        receiveThread.start();
    }

    private void showMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatArea.append(message + "\n");
            }
        });
    }

    private void showErrorAndExit(String errorMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(ClienteTCP.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private void updateMessageStatusIndicator(MessageStatus status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClienteTCP();
            }
        });
    }
}