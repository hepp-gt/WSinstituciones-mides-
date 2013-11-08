package com.archivo.acceso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class LeeArchivo 
{
	private File Archivo;
	private FileReader LectorArchivo;
	private BufferedReader LectorBuffer;
	private String Linea_parametro;
	
	public LeeArchivo(String directorio)
	{
		Archivo = new File(directorio);
		try 
		{
			LectorArchivo=new FileReader(Archivo);	
		} catch (FileNotFoundException error)
		{
			System.out.println(error.getMessage());
		}
		
		LectorBuffer = new BufferedReader(LectorArchivo);
	}
	
	public String Leerlinea() throws Exception
	{
		while (LectorBuffer.ready())
		{
			if (!(Linea_parametro = LectorBuffer.readLine()).equals ("\000")){
				return Linea_parametro;}
		}
		LectorBuffer.close();
		return null;
	}
}