package mides.gob.ws.utilerias;

/**
 * Clase que se utiliza para el manejo del archivo bit�cora y as� reportar los mensajes de errores
 * @author	Mynor Pacheco
 * @version 1.0
 * @see		java.io
 */

import java.io.*;

public class GrabaArchivo {

	private FileWriter 	ArchivoGrabar;
	private PrintWriter BufferGrabar;
	
	/**
	 * Constructor	GrabaArchivo: crea el archivo de bit�cora con el nombre y ruta enviada
	 * @param 	Directorio	Contiene la ruta y el nombre del archivo bit�cora a crear
	 */

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
	
	/**
	 * M�todo	GrabaLinea: este m�todo se utiliza para grabar una l�nea de mensaje al archivo bit�cora
	 * @param	Linea	Contiene el mensaje que se desea grabar en la bit�cora
	 */
	
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