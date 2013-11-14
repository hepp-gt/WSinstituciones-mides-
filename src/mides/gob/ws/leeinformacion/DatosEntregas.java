package mides.gob.ws.leeinformacion;

/**
 * Clase que se utiliza para la transferencia de información de la tabla tbl_entrega_beneficio
 * hacia el Registro Unificado de Usuarios
 * @author	Mynor Pacheco
 * @version 1.0
 * @see 	java.sql.Connection;
 * @see 	java.sql.SQLException;
 * @see 	java.sql.Statement;
 * @see 	java.sql.ResultSet;
 * @see 	mides.gob.ws.utilerias.FormateaMensaje;
 * @see 	mides.gob.ws.utilerias.GrabaArchivo;
 * @see 	mides.gob.ws.utilerias.ManejoAMQ;
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mides.gob.ws.utilerias.FormateaMensaje;
import mides.gob.ws.utilerias.GrabaArchivo;
import mides.gob.ws.utilerias.ManejoAMQ;

public class DatosEntregas {

	private Statement 		ejSQL_ENTREGAS;		//Para ejecutar el select
	private ResultSet 		rs_ENTREGAS; 		//Datos_ENTREGAS es el result set que almacena los resultados del select
	private FormateaMensaje EntregasMsj;
	
	private String			Tablaxusar="tbl_entrega_beneficio";
	private int				Total_Registros=0;	//Almacena el total de registros a leer	
				
	String qrySQL_ENTREGAS="Select " +
			"cui" +
			",id_institucion" +
			",id_programa" +
			",id_beneficio" +
			",id_departamento_otorgamiento_beneficio" +
			",id_municipio_otorgamiento_beneficio" +
			",fecha_otorgamiento_beneficio" + 
			",valor" +
			",evento" +
 			" from "+Tablaxusar;
	
	String qrySQL_ENTREGAS_TOTALES = "select totales=count(*) from " + Tablaxusar;
		
	/**
	 * Constructor	DatosEntregas	Verifica que la Conexion hacia la Base de Datos sea válida
	 * 								de lo contrario reporta error en la bitácora y finaliza el proceso.
	 * 								Verifica si se deben aplicar filtros por programa social y/o por beneficio.	
	 * @param  		Fecha_Proceso	Es la fecha en la cual el proceso se ejecuta
	 * @param 		Programa		El valor de este parámetro se obtiene del archivo Parametro.ini, aunque en el momento de la ejecución del proceso
	 * 								los datos disponibles deberían pertenecer solo a un programa, por seguridad se aplica un filtro
	 * @param 		Beneficio		El valor de este parámetro se obtiene del archivo Parametro.ini, dependiendo de su valor
	 * 								se aplica un filtro para obtener solo los beneficios configurados, de lo contrario se leen todos los beneficios
	 * 								que se encuentran en la tabla 
	 * @param  		Conexion 		Es la conección hacia la Base de Datos previamente establecida 
	 * @parama 		archLog  		Nombre del archivo bitácora correspondiente a la ejecución
	 */
	public DatosEntregas(String Fecha_Proceso, String Programa, String Beneficio, Connection Conexion, GrabaArchivo archLog)
	{
		EntregasMsj = new FormateaMensaje (Fecha_Proceso, "Inicio Proceso",archLog);

		if (Conexion==null)
		{
			EntregasMsj.Mensaje("Entregas", Fecha_Proceso, Tablaxusar,"Verifique la CONEXIOON hacia la DB", archLog);
		} else 
		{
			EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,"SE INICIA LA LECTURA", archLog);
			
			Beneficio=Beneficio.trim();
			Programa=Programa.trim();
			
			qrySQL_ENTREGAS = qrySQL_ENTREGAS + " Where id_programa=" + Programa;
			qrySQL_ENTREGAS_TOTALES = qrySQL_ENTREGAS_TOTALES +  " Where id_programa=" + Programa;
			
			
			if (!Beneficio.equals("*"))
			{
				qrySQL_ENTREGAS = qrySQL_ENTREGAS + " And id_beneficio in (" + Beneficio +")";
				qrySQL_ENTREGAS_TOTALES = qrySQL_ENTREGAS_TOTALES + " And id_beneficio in (" + Beneficio +")";
			}
		}
	}
	
	/**
	 * Método	LeeDatosEntregas			Prepara y ejecuta los comandos para obtener los datos a ser transferidos.
	 * 										El primer comando es un count para verificar si en la tabla existen datos.
	 * 										El segundo comando es el select para obtener los registros los cuale se convierten a formato xml
	 * 										y se envia a la cola ActiveMQ.
	 * @param  		Fecha_Proceso			Es la fecha en la cual el proceso se ejecuta
	 * @param  		Conexion 				Es la conección hacia la Base de Datos previamente establecida 
	 * @param 		Cola					Nombre la cola a la cual será enviada la información obtenida
	 * @parama 		archLog  				Nombre del archivo bitácora correspondiente a la ejecución
	 * @throws 		SQLException			Manejo de errores generados por los comandos SQL
	 */

	public void LeeDatosEntregas(String Fecha_Proceso, Connection Conexion, ManejoAMQ Cola, GrabaArchivo archLog) throws SQLException
	{
		String Mensaje=null;
		
		// Hay una conexion valida, se prepara el ambiente para ejecutar el select
		try {
			ejSQL_ENTREGAS = Conexion.createStatement();
		} catch (SQLException error) {
			EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
		}

		try {
			// Obtengo el total de registros a leer para grabarlo en el log
			rs_ENTREGAS = ejSQL_ENTREGAS.executeQuery(qrySQL_ENTREGAS_TOTALES);

			while (rs_ENTREGAS.next())
			{
				Total_Registros = rs_ENTREGAS.getInt("totales");
				EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,"Total Registros= "+ String.valueOf(Total_Registros), archLog);
			}
		}
		catch (SQLException error)
			{
				// No hay datos a procesar, se reporta en el log
				EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
				// Se cierran las conexiones y sesiones abiertas
				rs_ENTREGAS.close();
				ejSQL_ENTREGAS.close();
			}

		if(Total_Registros==0)
		{
			//No hay datos a procesar se reporta en el log
			EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,"NO HAY DATOS PARA PROCESAR", archLog);

			// Se cierran las conexiones y sesiones abiertas
			rs_ENTREGAS.close();
			ejSQL_ENTREGAS.close();
		}
		{
		// Ejecuto el Query para obtener datos de Usuario
		try {
			rs_ENTREGAS = ejSQL_ENTREGAS.executeQuery(qrySQL_ENTREGAS);
						
			while (rs_ENTREGAS.next() && Total_Registros > 0)
			{
				//System.out.println(
				Mensaje = 
						"<entregas>" +
						"<row>" +
						"<cui>" + String.valueOf(rs_ENTREGAS.getLong("cui")) + "</cui>" +
						"<id_institucion>" + String.valueOf(rs_ENTREGAS.getInt("id_institucion")) + "</id_institucion>" +
						"<id_programa>" + String.valueOf(rs_ENTREGAS.getInt("id_programa")) + "</id_programa>" +
						"<id_beneficio>" + String.valueOf(rs_ENTREGAS.getInt("id_beneficio")) + "</id_beneficio>" +
						"<departamento_otorgamiento_beneficio>" + String.valueOf(rs_ENTREGAS.getLong("id_departamento_otorgamiento_beneficio")) + "</departamento_otorgamiento_beneficio>" +
						"<municipio_otorgamiento_beneficio>" + String.valueOf(rs_ENTREGAS.getLong("id_municipio_otorgamiento_beneficio")) + "</municipio_otorgamiento_beneficio>" +
						"<fecha_otorgamiento_beneficio>" + rs_ENTREGAS.getString("fecha_otorgamiento_beneficio") + "</fecha_otorgamiento_beneficio>" +
						"<valor>" + String.valueOf(rs_ENTREGAS.getFloat("valor")) + "</valor>" +
						"<evento>" + rs_ENTREGAS.getString("evento") + "</evento>" +
						"</row>" +
						"</entregas>";
				
					Cola.EnvioMsjAMQ(Fecha_Proceso, Mensaje, archLog, EntregasMsj);
				// Fin del Mensaje
				} // Fin del While que recorre la tabla while (rs_ENTREGAS.next())
			
				// Se cierran las conexiones y sesiones abiertas
				rs_ENTREGAS.close();
				ejSQL_ENTREGAS.close();
			}
			catch (SQLException error)
			{
				// Este mensaje debe ir a la cola de ERRORES
				EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
				
				// Se cierran las conexiones y sesiones abiertas
				rs_ENTREGAS.close();
				ejSQL_ENTREGAS.close();
			}
		} // FIN contador==0
	}
}
