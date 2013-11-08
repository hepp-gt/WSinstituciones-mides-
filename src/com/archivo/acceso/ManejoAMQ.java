package com.archivo.acceso; 

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ManejoAMQ 
{
	private ConnectionFactory 	ConectAMQ;
	private Connection			ConexionAMQ=null;
	private Session				sesion;
	private Destination			destino;
	private MessageProducer		productor;
	private FormateaMensaje		AMQMsj2;
			
	public ManejoAMQ(String Servidor, String Puerto, String Cola, GrabaArchivo ArchLog, FormateaMensaje AMQMsj,String Fecha_Actual)
	{
		AMQMsj2 = new FormateaMensaje (null,Fecha_Actual, null,"Active MQ" ,ArchLog);
		
		ConectAMQ = new ActiveMQConnectionFactory("tcp://"+ Servidor + ":" + Puerto);
		
	}
	
	public boolean ConectAMQ(String Servidor, String Puerto, String Cola, GrabaArchivo ArchLog, FormateaMensaje AMQMsj,String Fecha_Actual)
	{
		try {

			ConexionAMQ = ConectAMQ.createConnection();
		} catch (JMSException error) {
			// Se reporta el error al log de la institución
			//AMQMsj2.Mensaje("ACTIVE", Fecha_Actual, "Active MQ", error.getMessage(), ArchLog);
		}
		
		if (ConexionAMQ==null)
		{
			AMQMsj2.Mensaje("ACTIVE", Fecha_Actual, "Active MQ", "CONEXION INVALIDA REVISE EL ESTADO DE  ACTIVE MQ", ArchLog);
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
				AMQMsj2.Mensaje("ACTIVE", Fecha_Actual, "Active MQ", error.getMessage(), ArchLog);
			}
		}
		return false;
	}
	
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
			AMQMsj2.Mensaje("ACTIVE", Fecha, "Active MQ", error.getMessage(), ArchLog);
			}
		
			//Si el mensaje enviado es "fin" corresponde a que finaliza el proceso y se cierra la cola
			//De lo contrario es un mensaje que se debe enviar a la cola para su procesamiento
			if(Msj.equals("fin"))
			{
				try {
					ConexionAMQ.close();
				} catch (JMSException error) {
				// Se reporta el error al log de la institución
				AMQMsj2.Mensaje("ACTIVE", Fecha, "Active MQ", error.getMessage(), ArchLog);
				}
			} else // mensaje para enviar a la cola
			{
				try {
					productor.send(mensaje);
				} catch (JMSException error) {
					// Se reporta el error al log de la institución
					AMQMsj2.Mensaje("ACTIVE", Fecha, "Active MQ", error.getMessage(), ArchLog);
				}
			}
		}
	}
}