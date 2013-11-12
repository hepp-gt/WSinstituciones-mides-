package mides.gob.ws.utilerias;

/**
 * Clase que se utiliza para leer el archivo de configuración Parametro.ini
 * @author	Mynor Pacheco
 * @version 1.0
 * @see		java.io
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LeeArchivo 
{
	private File Archivo;
	private FileReader LectorArchivo;
	private BufferedReader LectorBuffer;
	private String Linea_parametro;
	
	/**
	 * Constructor	LeeArchivo	Verifica que el archivo Parametro.ini exista.
	 * Crea el apuntador de memoria para utilizar y leer el archivo.
	 * @param directorio	Contiene la ruta y el nombre del archivo de configuración (Parametro.ini)
	 */
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
	
	/**
	 * Método Leerlinea	Utiliza el apuntador de memoria hacia el archivo Parametro.ini de esa forma recorre cada línea de configuración
	 * @return	Retorna la línea leída del archivo
	 * @throws 	Exception Si al manipular el archivo se presenta un error, la exception se activa y lo reporta
	 */
	public String Leerlinea() throws Exception
	{
		while (LectorBuffer.ready())
		{
			if (!(Linea_parametro = LectorBuffer.readLine()).equals ("\000")){
				return Linea_parametro;}
		}
		return null;
	}
	
	/**
	 * Método cierra	Cierra el archivo Parametro.ini y se libera el apuntador hacia el mismo
	 */
	public void cierra()
	{
		try {
			LectorBuffer.close();
		} catch (IOException error) {
			// TODO Auto-generated catch block
			System.out.println("Error al cerrar el archivo: " + error.getMessage());
		}
	}
}