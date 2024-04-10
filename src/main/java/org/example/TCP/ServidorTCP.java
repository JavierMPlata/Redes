package org.example.TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Clase principal del servidor TCP
public class ServidorTCP {

    // Clase interna para representar a un cliente
    private static class Cliente {
        String nombre; // Nombre del cliente
        DataOutputStream dos; // Stream para enviar datos al cliente
        boolean conectado; // Estado de conexión del cliente
        List<String> mensajesPendientes = new ArrayList<String>(); // Lista de mensajes pendientes para el cliente

        // Constructor de la clase Cliente
        Cliente(String nombre, DataOutputStream dos) {
            this.nombre = nombre;
            this.dos = dos;
            this.conectado = true;
        }
    }

    // Constante para el nombre del archivo de clientes
    private static final String CLIENTS_FILE = "clientes.txt";

    // Clase interna para representar un mensaje
    private static class Mensaje {
        String nombreCliente; // Nombre del cliente que envió el mensaje
        String contenido; // Contenido del mensaje
        boolean enviado; // Estado de envío del mensaje

        // Constructor de la clase Mensaje
        Mensaje(String nombreCliente, String contenido) {
            this.nombreCliente = nombreCliente;
            this.contenido = contenido;
            this.enviado = false;
        }
    }

    // Listas para almacenar los clientes y los mensajes
    private static List<Cliente> clientes = new ArrayList<>();
    private static List<Mensaje> mensajes = new ArrayList<>();

    // Método principal del servidor
    public static void main(String[] args) throws Exception {
        // Creación del socket del servidor en el puerto 5004
        ServerSocket server = new ServerSocket(5004);

        // Inicio del servidor
        System.out.println("Iniciando el servidor...");

        // Bucle principal del servidor
        while (true) {
            System.out.println("Servidor iniciado, esperando clientes...");

            // Aceptación de una conexión de un cliente
            Socket socket = server.accept();

            // Creación de los streams de entrada y salida
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // Lectura del nombre del cliente y creación del objeto Cliente
            String clientName = dis.readUTF();
            Cliente cliente = new Cliente(clientName, dos);
            clientes.add(cliente);

            // Envío de mensajes pendientes al cliente reconectado
            enviarMensajesPendientes(cliente);

            // Creación y inicio del hilo para recibir mensajes del cliente
            Thread receiveFromClientThread = new Thread(() -> {
                while (true) {
                    try {
                        // Recepción y procesamiento del mensaje del cliente
                        String clientMessage = dis.readUTF();
                        System.out.println("Mensaje recibido desde el cliente (" + clientName + "): " + clientMessage);
                        if (clientMessage.equals("Terminar")) {
                            // Procesamiento del mensaje de terminación
                            System.out.println("Terminando la conexión con el cliente...");
                            // No cerrar el socket, solo actualizar el estado del cliente
                            actualizarEstadoCliente(cliente, false);
                            Files.deleteIfExists(Paths.get(CLIENTS_FILE));
                            break; // Salir del bucle para dejar de escuchar a este cliente
                        } else if (clientMessage.equals("Desconectar")) {
                            // Procesamiento del mensaje de desconexión
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
                            // Procesamiento del mensaje de reconexión
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

    // Método para actualizar el estado de conexión de un cliente
    private static void actualizarEstadoCliente(Cliente cliente, boolean conectado) {
        cliente.conectado = conectado;
    }

    // Método para enviar los mensajes pendientes a un cliente
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

    // Método para enviar un mensaje a los otros clientes
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

    // Método para enviar un mensaje a un cliente
    private static void enviarMensaje(Cliente cliente, Mensaje mensaje) {
        try {
            cliente.dos.writeUTF(mensaje.nombreCliente + ": " + mensaje.contenido);
            mensaje.enviado = true;
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje al cliente: " + e.getMessage());
        }
    }
}