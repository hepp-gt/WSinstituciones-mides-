package mides.gob.ws.utilerias;

/**
 * Clase se utilizar para formatear de mejor forma los mensajes que se reportan en el archivo de bitácora
 * @author	Mynor Pacheco
 * @version 1.0
 */

public class FormateaMensaje {

	private String Espacios="                              ";

	/**
	 * Constructor	FormateaMensaje: Se utiliza para generar los mensajes de Error de las verificaciones preliminares
	 * 				acerca del estado de los servicios Active MQ y la Base de Datos
	 * @param Fecha		Es la fecha en la cual el proceso se ejecuta
	 * @param Mensaje	Descripción del mensaje a reportarse
	 * @param archLog	Nombre del archivo bitácora correspondiente a la ejecución
	 */
	
	public FormateaMensaje(String Fecha,  String Mensaje, GrabaArchivo archLog)
	{
		if (Mensaje.equals("REVISION"))
		{
			archLog.GrabaLinea("Fecha Proceso: " + Fecha + " Revisión de Parámetros");
		}
		if (Mensaje.equals("Active MQ"))
		{
			archLog.GrabaLinea("Fecha Proceso: " + Fecha + " Conexión a Active MQ");
		} 
		if (Mensaje.equals("Base Datos"))
		{
			archLog.GrabaLinea("Fecha Proceso: " + Fecha + " Conexión a DB");
		}
	}
		
	/**
	 * Método	Mensaje: Se encarga en ordenar los parámetros recibidos y colocarlos en un formato entendible
	 * 			para luego ser reportado en el archivo bitácora
	 * @param Titulo	Sección del proceso donde se genera el mensaje a reportarse
	 * @param Fecha		Es la fecha en la cual el proceso se ejecuta
	 * @param Tabla		Refiere a la tabla que se está procesando y donde se genera el mensaje a reportar
	 * @param Mensaje	Descripción del mensaje a reportarse
	 * @param archLog	Nombre del archivo bitácora correspondiente a la ejecución
	 */
	public void Mensaje(String Titulo,String Fecha, String Tabla, String Mensaje, GrabaArchivo archLog)
	{
		String Msj=null;
		
		if(Titulo.equals("PROGRAMA"))
		{
			Msj = "Fecha Proceso: "+ Fecha +" Nombre Programa: " + Tabla + " Estado: " + Mensaje;
		} else if(Titulo.equals("ACTIVE"))
		{
			Msj = "Fecha Proceso: "+ Fecha +" Mensaje Active MQ: " + Mensaje;
		} else if(Titulo.equals("DB"))
		{
			Msj = "Fecha Proceso: "+ Fecha +" Mensaje DB: " + Mensaje;
		}else 
		{
			if(Titulo.length()<22)
			{
				Titulo = Titulo + Espacios.substring(0,22-Titulo.length());
			}
			
			if(Tabla.length()<22)
			{
				Tabla = Tabla + Espacios.substring(0,22-Tabla.length());
			}
			Msj = "Fecha Proceso: "+ Fecha +" Procesando: " + Titulo +  " Tbl: "+ Tabla + " Mensaje: " + Mensaje;
		}
		
		archLog.GrabaLinea(Msj);
	}
}