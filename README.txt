# Servidor TCP y UDP en Java
# Calculadora matemática discreta
Este proyecto contiene implementaciones de servidores TCP y UDP en Java. Ambos servidores están diseñados para recibir mensajes de clientes y mostrar información sobre las conexiones entrantes.
## Servidor TCP

El servidor TCP se encuentra en el paquete `org.example.TCP` y ofrece las siguientes funcionalidades:

- Escucha en el puerto 5004 para recibir mensajes de clientes TCP.
- Muestra información detallada sobre cada cliente que se conecta, incluyendo el nombre del cliente, el mensaje enviado y el estado de la conexión.
- Permite enviar mensajes desde el servidor a los clientes TCP.
- Proporciona botones para desconectar y reconectar clientes, así como un botón para enviar mensajes.

**Para ejecutar el servidor TCP:**

1. Ejecute el archivo `ClienteTCP.java` para iniciar el cliente TCP.
2. Complete la dirección IP del servidor y el nombre del cliente cuando se le solicite.
3. Utilice la interfaz gráfica para enviar y recibir mensajes, desconectar/reconectar clientes y controlar la conexión.

## Servidor UDP

El servidor UDP se encuentra en el paquete `org.example.UDP` y ofrece las siguientes funcionalidades:

- Escucha en el puerto 9107 para recibir mensajes de clientes UDP.
- Muestra información detallada sobre cada cliente que envía un mensaje, incluyendo la dirección IP y el puerto de origen.
- Limita la recepción de mensajes a un máximo de 5 antes de detenerse.

**Para ejecutar el servidor UDP:**

1. Ejecute el archivo `ServidorUDP.java` para iniciar el servidor UDP.
2. El servidor esperará a recibir mensajes de clientes UDP y mostrará información sobre los mensajes y clientes en la consola.

## Notas adicionales

- Ambos servidores están diseñados para propósitos educativos y pueden ser personalizados según sea necesario.
- Siempre asegúrese de cerrar correctamente los sockets al finalizar el uso para liberar recursos.
