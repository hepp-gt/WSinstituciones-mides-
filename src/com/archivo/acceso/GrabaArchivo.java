package com.archivo.acceso;

import java.io.*;

public class GrabaArchivo {

	private FileWriter 	ArchivoGrabar;
	private PrintWriter BufferGrabar;
	
	public GrabaArchivo(String Directorio)
	{
		try {
	       ArchivoGrabar = new FileWriter(Directorio);
		}  catch (Exception e) {
			// Mensaje va a cola de ERRORES
            e.printStackTrace();
        }
		
		BufferGrabar = new PrintWriter(ArchivoGrabar);
	}
	
	public void GrabaLinea(String Linea)
	{
		try {
			if (Linea.equals("fin"))
			{
				ArchivoGrabar.close();
			}
			else 
			{
				BufferGrabar.println(Linea);
			}
		} catch (IOException e) {
				// Mensaje que va a la cola de ERRORES
				e.printStackTrace();
		}
	}
}