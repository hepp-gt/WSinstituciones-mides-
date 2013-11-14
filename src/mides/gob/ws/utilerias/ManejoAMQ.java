package mides.gob.ws.utilerias; 

/**
 * Clase para el manejo y envio de mensajes hacia la cola de ActiveMQ
 * @author	Mynor Pacheco
 * @version 1.0
 * @see	javax.jms
 * @see	org.apache.activemq.ActiveMQConnectionFactory
 */

import javax.jms.*;


import org.apache.activemq.ActiveMQConnectionFactory;

public class ManejoAMQ 
{
	private ActiveMQConnectionFactory 	ConectAMQ;
	private Connection					ConexionAMQ=null;
	private Session						sesion;
	private Destination					destino;
	private MessageProducer				productor;
	private FormateaMensaje				AMQMsj;
	
	/**
	 * Constructor	ManejoAMQ	Crea la conección hacia el servicio de ActiveMQ 
	 * @param Servidor			Nombre o dirección IP del servidor donde se ubica el servicio ActiveMQ		
	 * @param Puerto			Puerto en el que escucha las peticiones el ActiveMQ
	 * @param ArchLog			Nombre del archivo bitácora correspondiente a la ejecución
	 * @param Fecha_Actual		Es la fecha en la cual el proceso se ejecuta
	 */
	public ManejoAMQ(String Servidor, String Puerto, GrabaArchivo ArchLog,String Fecha_Actual)
	{
		AMQMsj = new FormateaMensaje (Fecha_Actual, "Active MQ" ,ArchLog);
		
		ConectAMQ = new ActiveMQConnectionFactory("tcp://"+ Servidor + ":" + Puerto);
		
	}
	
	/**
	 * Método	ConectAMQ	Crea y levanta la conección hacia ActiveMQ. Crea la sesión NO transaccional (asincrona), crea la cola 
	 * 						y genera el canal hacia la cola como Productor de mensajería
	 * @param Cola			Nombre de la cola ActiveMQ que se utilizará para enviar los mensajes
	 * @param ArchLog		Nombre del archivo bitácora correspondiente a la ejecución
	 * @param AMQMsj		Manejo y formato de los mensajes a la bitácora
	 * @param Fecha_Actual	Es la fecha en la cual el proceso se ejecuta
	 * @return				Verdadero si la conección se realiza, Falso si existe un error
	 */
	
	public boolean ConectAMQ(String Cola, GrabaArchivo ArchLog, FormateaMensaje AMQMsj,String Fecha_Actual)
	{
		try {

			ConexionAMQ = ConectAMQ.createConnection();
		} catch (JMSException error) {
			// Se reporta el error al log de la institución
			AMQMsj.Mensaje("ACTIVE", Fecha_Actual, "Active MQ", error.getMessage(), ArchLog);
		}
		
		if (ConexionAMQ==null)
		{
			AMQMsj.Mensaje("ACTIVE", Fecha_Actual, "Active MQ", "CONEXION INVALIDA REVISE EL ESTADO DE  ACTIVE MQ", ArchLog);
			return false;
		} 
		else {
			try {
				ConexionAMQ.start();
				sesion= ConexionAMQ.createSession(false,Session.AUTO_ACKNOWLEDGE);
				destino = sesion.createQueue(Cola);
				productor = sesion.createProducer(destino);
				return true;
			} catch (JMSException error) {
				// Se reporta el error al log de la institución
				AMQMsj.Mensaje("ACTIVE", Fecha_Actual, "Active MQ", error.getMessage(), ArchLog);
			}
		}
		return false;
	}
	
	/**
	 * Método	EnvioMsAMQ	Envía un mensaje como Productor hacía la cola de ActiveMQ
	 * @param Fecha			Es la fecha en la cual el proceso se ejecuta
	 * @param Msj			Mensaje que se enviará a la cola ActiveMQ (por requerimiento de SISO, el mensaje se envia en formato xml)
	 * @param ArchLog		Nombre del archivo bitácora correspondiente a la ejecución
	 * @param AMQMsj		Manejo y formato de los mensajes a la bitácora
	 */
	
	public void EnvioMsjAMQ (String Fecha,String Msj, GrabaArchivo ArchLog,FormateaMensaje AMQMsj)
	{
		if (ConexionAMQ!=null)
		{
			TextMessage mensaje=null;
	
			// Se crea la clase para el envio del mensaje
			try {
				mensaje = sesion.createTextMessage(Msj);
			} catch (JMSException error) {
			// Se reporta el error al log de la institución
			AMQMsj.Mensaje("ACTIVE", Fecha, "Active MQ", error.getMessage(), ArchLog);
			}
		
			//Si el mensaje enviado es "fin" corresponde a que finaliza el proceso y se cierra la cola
			//De lo contrario es un mensaje que se debe enviar a la cola para su procesamiento
			if(Msj.equals("fin"))
			{
				try {
					ConexionAMQ.close();
				} catch (JMSException error) {
				// Se reporta el error al log de la institución
				AMQMsj.Mensaje("ACTIVE", Fecha, "Active MQ", error.getMessage(), ArchLog);
				}
			} else // mensaje para enviar a la cola
			{
				try {
					productor.send(mensaje);
				} catch (JMSException error) {
					// Se reporta el error al log de la institución
					AMQMsj.Mensaje("ACTIVE", Fecha, "Active MQ", error.getMessage(), ArchLog);
				}
			}
		}
	}
}