package org.example.UDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;

public class ClienteUDP extends JFrame implements ActionListener {
    // Declaración de campos de la GUI y variables relacionadas con el socket UDP
    private JTextField nombreField, codigoField, edadField, semestreField, ciudadField;
    private JButton enviarButton, limpiarButton;
    private DatagramSocket miSocket;
    private InetAddress host;
    private int puertoServidor;

    // Constructor de la clase ClienteUDP
    public ClienteUDP() {
        // Configuración de la ventana
        setTitle("Cliente UDP");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(8, 2, 10, 10)); // Diseño de la ventana

        // Paleta de colores utilizada en la interfaz gráfica
        Color lightBlue = new Color(173, 216, 230);
        Color buttonColor = new Color(70, 130, 180);

        // Establecer el color de fondo de la ventana
        getContentPane().setBackground(lightBlue);

        // Definir fuentes para etiquetas y botones
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);

        // Etiquetas y campos de texto para ingresar datos del estudiante
        add(new JLabel("Nombre del estudiante:")).setFont(labelFont);
        nombreField = new JTextField();
        add(nombreField);

        add(new JLabel("Código:")).setFont(labelFont);
        codigoField = new JTextField();
        add(codigoField);

        add(new JLabel("Edad:")).setFont(labelFont);
        edadField = new JTextField();
        add(edadField);

        add(new JLabel("Semestre:")).setFont(labelFont);
        semestreField = new JTextField();
        add(semestreField);

        add(new JLabel("Ciudad de residencia:")).setFont(labelFont);
        ciudadField = new JTextField();
        add(ciudadField);

        // Espacio en blanco para separar y alinear los campos de texto del botón
        add(new JLabel(""));
        add(new JLabel(""));

        // Botón para enviar los datos
        enviarButton = new JButton("Enviar");
        enviarButton.addActionListener(this);
        enviarButton.setFont(buttonFont);
        enviarButton.setBackground(buttonColor);
        enviarButton.setForeground(Color.WHITE); // Texto en blanco para mayor contraste
        add(enviarButton);

        // Botón para limpiar los campos
        limpiarButton = new JButton("Limpiar");
        limpiarButton.addActionListener(this);
        limpiarButton.setFont(buttonFont);
        limpiarButton.setBackground(buttonColor);
        limpiarButton.setForeground(Color.WHITE); // Texto en blanco para mayor contraste
        add(limpiarButton);

        // Inicialización del socket UDP y obtención de la dirección del servidor
        try {
            miSocket = new DatagramSocket();
            host = InetAddress.getByName("172.17.3.135");
            puertoServidor = 9107;
        } catch (SocketException e) {
            showErrorDialog("Error al crear el socket: " + e.getMessage());
            e.printStackTrace();
        } catch (UnknownHostException e) {
            showErrorDialog("Error al obtener la dirección del host: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para manejar eventos de botón
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enviarButton) {
            enviarDatos();
        } else if (e.getSource() == limpiarButton) { // Manejo del evento del botón limpiar
            limpiarCampos();
        }
    }

    // Método para enviar datos al servidor
    private void enviarDatos() {
        try {
            // Construcción del mensaje con los datos del estudiante
            String mensaje = nombreField.getText() + ";" + codigoField.getText() + ";" + edadField.getText() + ";" +
                    semestreField.getText() + ";" + ciudadField.getText();
            byte[] buffer = mensaje.getBytes();

            // Creación y envío del paquete UDP
            DatagramPacket miPaquete = new DatagramPacket(buffer, buffer.length, host, puertoServidor);
            miSocket.send(miPaquete);

            JOptionPane.showMessageDialog(this, "Datagrama enviado con éxito!");
        } catch (IOException e) {
            showErrorDialog("Error al enviar el datagrama: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para limpiar los campos de texto
    private void limpiarCampos() {
        nombreField.setText("");
        codigoField.setText("");
        edadField.setText("");
        semestreField.setText("");
        ciudadField.setText("");
    }

    // Método para mostrar un mensaje de error en un cuadro de diálogo
    private void showErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Método principal que inicia la aplicación Swing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteUDP().setVisible(true));
    }
}
