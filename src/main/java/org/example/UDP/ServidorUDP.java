package org.example.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServidorUDP {

    public static void main(String[] args) {

        byte[] bufer = new byte[1024];
        StringBuilder receivedMessages = new StringBuilder(); // Added StringBuilder to concatenate messages
        int messageCount = 0; // Added counter for received messages

        //Sentencia de control de errores
        try {

            System.out.println("Servidor UDP iniciado" );
            System.out.println("En espera de un cliente");

            //Creacion del socket
            DatagramSocket socketUDP = new DatagramSocket(9107);

            //Siempre atendera peticiones
            while (true) {

                // Construimos el DatagramPacket para recibir peticiones
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);

                // Leemos una petición del DatagramSocket
                socketUDP.receive(peticion);

                // Muestro datos captados por el servidor en DatagramSocket
                System.out.println("Recibo la informacion de un cliente");
                System.out.println("Ip origen " + peticion.getAddress());
                System.out.println("Puerto origen: " + peticion.getPort());

                String message = new String(peticion.getData(), 0, peticion.getLength());
                receivedMessages.append(message).append(" "); // Append each message to the StringBuilder
                System.out.println("Mensaje :" + message);

                messageCount++; // Increment the counter

                if (messageCount >= 5) { // Break the loop after receiving 5 messages
                    break;
                }

                System.out.print("Servidor  UDP en espera de un cliente \n");
            }

            // Print all messages after receiving all of them
            System.out.println("Received messages: " + receivedMessages.toString());

        } catch (SocketException e) {
            System.out.println("Error al crear el socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error al recibir el datagrama: " + e.getMessage());
        }
    }
}