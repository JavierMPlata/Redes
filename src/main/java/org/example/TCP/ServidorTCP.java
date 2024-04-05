package org.example.TCP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServidorTCP {

    private static List<DataOutputStream> clientOutputStreams = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5001);

        System.out.println("Iniciando el servidor...");

        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String serverInput = scanner.nextLine();
                System.out.println("Servidor dice: " + serverInput);
            }
        });
        inputThread.start();

        while (true) {
            System.out.println("Servidor iniciado, esperando clientes...");

            Socket socket = server.accept();

            DataInputStream dis = new DataInputStream(socket.getInputStream());

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            clientOutputStreams.add(dos);

            Thread sendToClientThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String serverInput = scanner.nextLine();
                    try {
                        for (DataOutputStream clientOutputStream : clientOutputStreams) {
                            clientOutputStream.writeUTF(serverInput);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            sendToClientThread.start();

            Thread receiveFromClientThread = new Thread(() -> {
                while (true) {
                    try {
                        String clientMessage = dis.readUTF();
                        System.out.println("Mensaje recibido desde el cliente (" + socket.getInetAddress() + "): " + clientMessage);
                        if (clientMessage.equals("Terminar")) {
                            System.out.println("Terminando la conexión con el cliente...");
                            socket.close();
                            System.exit(0);
                        } else {
                            for (DataOutputStream clientOutputStream : clientOutputStreams) {
                                if (clientOutputStream != dos) {
                                    clientOutputStream.writeUTF(clientMessage);
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error al recibir el mensaje del cliente: " + e.getMessage());
                        System.exit(0);
                    }
                }
            });
            receiveFromClientThread.start();
        }
    }
}