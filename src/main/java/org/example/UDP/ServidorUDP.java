package org.example.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServidorUDP {

    public static void main(String[] args) {

        // Arreglo de bytes para almacenar los datos recibidos
        byte[] bufer = new byte[1024];

        // StringBuilder para concatenar los mensajes recibidos
        StringBuilder receivedMessages = new StringBuilder(); // Se agrega un StringBuilder para concatenar mensajes

        // Contador para contar los mensajes recibidos
        int messageCount = 0; // Se agrega un contador para los mensajes recibidos

        // Sentencia de control de errores
        try {

            // Imprime un mensaje indicando que el servidor UDP ha iniciado
            System.out.println("Servidor UDP iniciado" );

            // Indica que el servidor está esperando a un cliente
            System.out.println("En espera de un cliente");

            // Creación del socket UDP en el puerto 9107
            DatagramSocket socketUDP = new DatagramSocket(9107);

            // Bucle para atender siempre las peticiones entrantes
            while (true) {

                // Construcción del DatagramPacket para recibir peticiones
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);

                // Leemos una petición del DatagramSocket
                socketUDP.receive(peticion);

                // Muestra información sobre el cliente que envía el mensaje
                System.out.println("Recibo la información de un cliente");
                System.out.println("Ip origen " + peticion.getAddress());
                System.out.println("Puerto origen: " + peticion.getPort());

                // Convertimos los datos recibidos a una cadena de texto
                String message = new String(peticion.getData(), 0, peticion.getLength());

                // Añadimos el mensaje recibido al StringBuilder
                receivedMessages.append(message).append(" "); // Agregar cada mensaje al StringBuilder
                System.out.println("Mensaje: " + message);

                // Incrementamos el contador de mensajes recibidos
                messageCount++; // Incrementar el contador

                // Si se han recibido 5 mensajes, salimos del bucle
                if (messageCount >= 5) { // Salir del bucle después de recibir 5 mensajes
                    break;
                }

                // Muestra un mensaje indicando que el servidor está esperando a un cliente
                System.out.print("Servidor  UDP en espera de un cliente \n");
            }

            // Imprime todos los mensajes recibidos después de haberlos recibido todos
            System.out.println("Mensajes recibidos: " + receivedMessages.toString());

        } catch (SocketException e) {
            // Captura y maneja la excepción de SocketException, en caso de que ocurra al crear el socket
            System.out.println("Error al crear el socket: " + e.getMessage());
        } catch (IOException e) {
            // Captura y maneja la excepción de IOException, en caso de que ocurra al recibir el datagrama
            System.out.println("Error al recibir el datagrama: " + e.getMessage());
        }
    }
}
