package org.example.UDP;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class clienteUDP {

    public static void main(String args[]) {

        byte[] buffer = new byte[1024];

        try {
            //Creo el socket de UDP
            DatagramSocket miSocket = new DatagramSocket();

            //Muestro datos del cliente
            System.out.println("Esta usted intentado una conexion desde:");
            System.out.println("Nombre de host: " + InetAddress.getLocalHost().getHostName());
            System.out.println("Ip de host: " + InetAddress.getLocalHost().getHostAddress());

            //Obtengo la ip localizacion del host
            InetAddress host = InetAddress.getByName("172.17.17.140");

            //Preparo el mensaje a enviar
            Scanner scanner = new Scanner(System.in);
            String[] prompts = {"Nombre del estudiante", "Código", "Edad", "Semestre", "Ciudad de residencia"};

            //Indico el puerto del servidor
            int puertoserver = 5000;

            for (String prompt : prompts) {
                System.out.println(prompt + ": ");
                String input = scanner.nextLine();
                buffer = input.getBytes();

                // Construimos un datagrama para enviar el mensaje al servidor indicando puerto e ip del cliente
                DatagramPacket miPaquete = new DatagramPacket(buffer, buffer.length, host, puertoserver);

                // Enviamos el datagrama
                try {
                    miSocket.send(miPaquete);
                    System.out.println("Datagrama enviado con exito!");
                } catch (SocketException e) {
                    System.out.println("Error 504: El servidor no está en línea.");
                } catch (IOException e) {
                    System.out.println("Error al enviar el datagrama: " + e.getMessage());
                }
            }

            //cierro el socket
            System.out.println("Socket cerrado desde el cliente");
            miSocket.close();

        } catch (SocketException e) {
            System.out.println("Error al crear el socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Error al obtener la dirección del host: " + e.getMessage());
        }
    }
}