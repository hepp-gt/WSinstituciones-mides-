package com.archivo.acceso;

import java.sql.Connection;

public class FormateaMensaje {

	private String Espacios="                              ";
                               
	public FormateaMensaje(Connection Conexion, String Fecha, String Tabla, String Mensaje, GrabaArchivo archLog)
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