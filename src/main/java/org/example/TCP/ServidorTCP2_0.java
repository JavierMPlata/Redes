package org.example.TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServidorTCP2_0 {

    private static class Cliente {
        String nombre;
        DataOutputStream dos;
        boolean conectado;

        Cliente(String nombre, DataOutputStream dos) {
            this.nombre = nombre;
            this.dos = dos;
            this.conectado = true;
        }
    }

    private static final String CLIENTS_FILE = "clientes.txt";

    private static class Mensaje {
        String nombreCliente;
        String contenido;
        boolean leido;

        Mensaje(String nombreCliente, String contenido) {
            this.nombreCliente = nombreCliente;
            this.contenido = contenido;
            this.leido = false;
        }
    }

    private static List<Cliente> clientes = new ArrayList<>();
    private static List<Mensaje> mensajes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5001);

        System.out.println("Iniciando el servidor...");

        while (true) {
            System.out.println("Servidor iniciado, esperando clientes...");

            Socket socket = server.accept();

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String clientName = dis.readUTF();
            clientes.add(new Cliente(clientName, dos));

            Thread receiveFromClientThread = new Thread(() -> {
                while (true) {
                    try {
                        String clientMessage = dis.readUTF();
                        System.out.println("Mensaje recibido desde el cliente (" + clientName + "): " + clientMessage);
                        if (clientMessage.equals("Terminar")) {
                            System.out.println("Terminando la conexión con el cliente...");
                            socket.close();
                            Files.deleteIfExists(Paths.get(CLIENTS_FILE));
                            System.exit(0);
                        } else if (clientMessage.equals("Desconectar")) {
                            System.out.println("Desconectando al cliente...");
                            dos.writeUTF("Desconectado");
                            for (Cliente cliente : clientes) {
                                if (cliente.dos == dos) {
                                    cliente.conectado = false;
                                    break;
                                }
                            }
                            try (PrintWriter out = new PrintWriter(new FileWriter(CLIENTS_FILE, true))) {
                                out.println(clientName + "," + false);
                            } catch (IOException e) {
                                System.out.println("Error al escribir en el archivo: " + e.getMessage());
                            }
                        } else if (clientMessage.equals("Reconectar")) {
                            System.out.println("Reconectando al cliente...");
                            for (Mensaje mensaje : mensajes) {
                                if (mensaje.nombreCliente.equals(clientName) && !mensaje.leido) {
                                    dos.writeUTF(mensaje.contenido);
                                    mensaje.leido = true;
                                }
                            }
                        } else {
                            mensajes.add(new Mensaje(clientName, clientMessage));
                            for (Cliente cliente : clientes) {
                                if (cliente.dos != dos) {
                                    cliente.dos.writeUTF(clientName + ": " + clientMessage);
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