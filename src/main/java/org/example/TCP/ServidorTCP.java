package org.example.TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServidorTCP {

    private static class Cliente {
        String nombre;
        DataOutputStream dos;
        boolean conectado;
        List<String> mensajesPendientes = new ArrayList<String>();

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
        boolean enviado;

        Mensaje(String nombreCliente, String contenido) {
            this.nombreCliente = nombreCliente;
            this.contenido = contenido;
            this.enviado = false;
        }
    }

    private static List<Cliente> clientes = new ArrayList<>();
    private static List<Mensaje> mensajes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5004);

        System.out.println("Iniciando el servidor...");

        while (true) {
            System.out.println("Servidor iniciado, esperando clientes...");

            Socket socket = server.accept();

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String clientName = dis.readUTF();
            Cliente cliente = new Cliente(clientName, dos);
            clientes.add(cliente);

            // Enviar mensajes pendientes al cliente reconectado
            enviarMensajesPendientes(cliente);

            Thread receiveFromClientThread = new Thread(() -> {
                while (true) {
                    try {
                        String clientMessage = dis.readUTF();
                        System.out.println("Mensaje recibido desde el cliente (" + clientName + "): " + clientMessage);
                        if (clientMessage.equals("Terminar")) {
                            System.out.println("Terminando la conexión con el cliente...");
                            // No cerrar el socket, solo actualizar el estado del cliente
                            actualizarEstadoCliente(cliente, false);
                            Files.deleteIfExists(Paths.get(CLIENTS_FILE));
                            break; // Salir del bucle para dejar de escuchar a este cliente
                        } else if (clientMessage.equals("Desconectar")) {
                            System.out.println("Desconectando al cliente...");
                            dos.writeUTF("Desconectado");
                            // Actualizar estado del cliente a desconectado
                            actualizarEstadoCliente(cliente, false);
                            try (PrintWriter out = new PrintWriter(new FileWriter(CLIENTS_FILE, true))) {
                                out.println(clientName + "," + false);
                            } catch (IOException e) {
                                System.out.println("Error al escribir en el archivo: " + e.getMessage());
                            }
                        } else if (clientMessage.equals("Reconectar")) {
                            System.out.println("Reconectando al cliente...");
                            // Actualizar estado del cliente a conectado
                            actualizarEstadoCliente(cliente, true);
                            // Enviar mensajes pendientes al cliente reconectado
                            enviarMensajesPendientes(cliente);
                        } else {
                            // Agregar mensaje a la lista de mensajes
                            mensajes.add(new Mensaje(clientName, clientMessage));
                            // Enviar mensaje a otros clientes
                            enviarMensajeOtrosClientes(cliente, clientName, clientMessage);
                        }
                    } catch (IOException e) {
                        System.out.println("Error al recibir el mensaje del cliente: " + e.getMessage());
                        // Actualizar estado del cliente a desconectado
                        actualizarEstadoCliente(cliente, false);
                        break; // Salir del bucle para dejar de escuchar a este cliente
                    }
                }
            });
            receiveFromClientThread.start();
        }
    }

    private static void actualizarEstadoCliente(Cliente cliente, boolean conectado) {
        cliente.conectado = conectado;
    }

    private static void enviarMensajesPendientes(Cliente cliente) {
        for (String mensaje : cliente.mensajesPendientes) {
            try {
                cliente.dos.writeUTF(mensaje);
            } catch (IOException e) {
                System.out.println("Error al enviar el mensaje al cliente: " + e.getMessage());
            }
        }
        // Limpiamos la lista de mensajes pendientes
        cliente.mensajesPendientes.clear();
    }

    private static void enviarMensajeOtrosClientes(Cliente senderCliente, String senderName, String message) {
        for (Cliente cliente : clientes) {
            if (cliente != senderCliente) {
                if (cliente.conectado) {
                    enviarMensaje(cliente, new Mensaje(senderName, message));
                } else {
                    // Agregamos el mensaje a la lista de mensajes pendientes del cliente
                    cliente.mensajesPendientes.add(senderName + ": " + message);
                }
            }
        }
    }

    private static void enviarMensaje(Cliente cliente, Mensaje mensaje) {
        try {
            cliente.dos.writeUTF(mensaje.nombreCliente + ": " + mensaje.contenido);
            mensaje.enviado = true;
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje al cliente: " + e.getMessage());
        }
    }
}
