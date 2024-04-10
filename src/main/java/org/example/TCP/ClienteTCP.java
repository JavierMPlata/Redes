package org.example.TCP;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.io.*;
import java.net.*;

// Clase ClienteTCP que extiende de JFrame para crear la interfaz gráfica
public class ClienteTCP extends JFrame {

    // Declaración de variables para la conexión y comunicación con el servidor
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String clientName;
    private boolean isConnected = false; // Variable para controlar el estado de la conexión

    // Declaración de componentes de la interfaz gráfica
    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel messageStatusIndicator;
    private JButton disconnectButton;
    private JButton reconnectButton;

    // Enumeración para los diferentes estados de los mensajes
    enum MessageStatus {
        SENT_AND_RECEIVED, SENDING, SERVER_CLOSED, DISCONNECTED
    }

    // Constructor de la clase ClienteTCP
    public ClienteTCP() {
        // Configuración de la ventana
        setTitle("Cliente TCP");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Creación de un panel para los botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Creación del área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Creación del panel de entrada de mensajes
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        messageStatusIndicator = new JLabel();
        messageStatusIndicator.setOpaque(true);
        messageStatusIndicator.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Creación del panel de estado de los mensajes
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(messageStatusIndicator, BorderLayout.CENTER);
        statusPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(statusPanel, BorderLayout.EAST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Creación del botón de desconexión
        disconnectButton = new JButton("Desconectar");
        setButtonStyle(disconnectButton, Color.RED); // Cambiar color a rojo
        disconnectButton.addActionListener(e -> disconnectFromServer());
        buttonPanel.add(disconnectButton);

        // Creación del botón de reconexión
        reconnectButton = new JButton("Reconectar");
        setButtonStyle(reconnectButton, Color.CYAN); // Cambiar color a cyan
        reconnectButton.addActionListener(e -> reconnectToServer());
        buttonPanel.add(reconnectButton);

        inputPanel.add(buttonPanel, BorderLayout.WEST);

        // Hacer visible la ventana
        setVisible(true);

        // Conectar al servidor
        connectToServer();
    }

    // Método para establecer el estilo de los botones
    private void setButtonStyle(JButton button, Color color) {
        button.setUI(new BasicButtonUI());
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        button.setBackground(color); // Establecer el color
        button.setForeground(UIManager.getColor("Button.foreground"));
        button.setFocusPainted(false);
    }

    // Método para conectar al servidor
    private void connectToServer() {
        try {
            if (socket == null || socket.isClosed()) {
                // Creación de un panel para ingresar la dirección del servidor y el nombre del cliente
                JPanel panel = new JPanel(new GridLayout(3, 2));
                JTextField serverAddressField = new JTextField();
                JTextField clientNameField = new JTextField();
                panel.add(new JLabel("Dirección IP del servidor:"));
                panel.add(serverAddressField);
                panel.add(new JLabel("Nombre del cliente:"));
                panel.add(clientNameField);
                int result = JOptionPane.showConfirmDialog(null, panel, "Conectar al servidor", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    // Conexión al servidor
                    String serverAddress = serverAddressField.getText();
                    clientName = clientNameField.getText();
                    socket = new Socket(serverAddress, 5004);
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(clientName);
                    isConnected = true; // Marcar como conectado
                    receiveMessages();
                } else {
                    showErrorAndExit("Conexión cancelada por el usuario.");
                }
            } else {
                dos.writeUTF("Reconectar");
                updateMessageStatusIndicator(MessageStatus.SENT_AND_RECEIVED);
                receiveMessages();
            }
        } catch (IOException e) {
            showErrorAndExit("Error al reconectar con el servidor.");
            e.printStackTrace();
        }
    }

    // Método para enviar mensajes al servidor
    private void sendMessage() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "No estás conectado al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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

    // Método para recibir mensajes del servidor
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

    // Método para desconectar del servidor
    private void disconnectFromServer() {
        try {
            dos.writeUTF("Desconectar");
            updateMessageStatusIndicator(MessageStatus.DISCONNECTED);
            isConnected = false; // Marcar como desconectado
        } catch (IOException e) {
            showErrorAndExit("Error al desconectar del servidor.");
            e.printStackTrace();
        }
    }

    // Método para reconectar al servidor
    private void reconnectToServer() {
        try {
            dos.writeUTF("Reconectar");
            updateMessageStatusIndicator(MessageStatus.SENT_AND_RECEIVED);
            isConnected = true; // Marcar como conectado
        } catch (IOException e) {
            showErrorAndExit("Error al reconectar con el servidor.");
            e.printStackTrace();
        }
    }

    // Método para terminar la conexión con el servidor
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

    // Método para mostrar mensajes en el área de chat
    private void showMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    // Método para mostrar errores y terminar la ejecución
    private void showErrorAndExit(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(ClienteTCP.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        });
    }

    // Método para actualizar el indicador de estado de los mensajes
    private void updateMessageStatusIndicator(MessageStatus status) {
        SwingUtilities.invokeLater(() -> {
            switch (status) {
                case SENT_AND_RECEIVED:
                    messageStatusIndicator.setBackground(new Color(46, 204, 113));
                    messageStatusIndicator.setText("Enviado y recibido");
                    break;
                case SENDING:
                    messageStatusIndicator.setBackground(new Color(241, 196, 15));
                    messageStatusIndicator.setText("Se está enviando");
                    break;
                case SERVER_CLOSED:
                    messageStatusIndicator.setBackground(new Color(231, 76, 60));
                    messageStatusIndicator.setText("Se cerró el servidor");
                    break;
                case DISCONNECTED:
                    messageStatusIndicator.setBackground(new Color(52, 152, 219));
                    messageStatusIndicator.setText("Desconectado");
                    break;
            }
        });
    }

    // Método principal para iniciar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteTCP());
    }
}