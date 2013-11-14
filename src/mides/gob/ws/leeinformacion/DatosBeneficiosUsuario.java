package mides.gob.ws.leeinformacion;

/**
 * Clase que se utiliza para la transferencia de información de la tabla tbl_beneficiosxusuario
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

public class DatosBeneficiosUsuario {


	private Statement 		ejSQL_BENEFICIOSXUSUARIOS;		//Para ejecutar el select
	private ResultSet 		rs_BENEFICIOSXUSUARIOS; 		//Datos_BENEFICIOSXUSUARIOS es el result set que almacena los resultados del select
	private FormateaMensaje BeneficiosxUsuarioMsj;
	private String			Tablaxusar="tbl_beneficiosxusuario";
	
	private int			Total_Registros=0;	//Almacena el total de registros a leer	
				
	String qrySQL_BENEFICIOSXUSUARIOS="Select " +
					"cui" +
					",id_beneficio" +
					",status" +
					" from " + Tablaxusar;
		
	String qrySQL_BENEFICIOSxUSUARIOS_TOTAL="select totales=count(*) from " + Tablaxusar;
	
	/**
	 * Constructor	DatosBeneficiosUsuario	Verifica que la Conexion hacia la Base de Datos sea válida
	 * 										de lo contrario reporta error en la bitácora y finaliza el proceso.
	 * 										Verifica si se debe aplicar filtro por beneficio.	
	 * @param  		Fecha_Proceso			Es la fecha en la cual el proceso se ejecuta
	 * @param 		Beneficio				El valor de este parámetro se obtiene del archivo Parametro.ini, dependiendo de su valor
	 * 										se aplica un filtro para obtener solo los beneficios configurados, de lo contrario se leen 
	 * 										todos los beneficios que se encuentran en la tabla 
	 * @param  		Conexion 				Es la conección hacia la Base de Datos previamente establecida y se verifica si aún esta activa 
	 * @parama 		archLog  				Nombre del archivo bitácora correspondiente a la ejecución
	 */
	public DatosBeneficiosUsuario(String Fecha_Proceso, String Beneficio, Connection Conexion, GrabaArchivo archLog)
	{
		BeneficiosxUsuarioMsj = new FormateaMensaje (Fecha_Proceso, "Inicio Proceso",archLog);

		if (Conexion==null)
		{
			BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,"Verifique la CONEXIOON hacia la DB", archLog);
		} else 
		{
			BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,"SE INICIA LA LECTURA", archLog);			
		}

		Beneficio=Beneficio.trim();
		if(!Beneficio.equals("*"))
		{
			qrySQL_BENEFICIOSxUSUARIOS_TOTAL = qrySQL_BENEFICIOSxUSUARIOS_TOTAL + " Where id_beneficio in (" + Beneficio +")"; 
			qrySQL_BENEFICIOSXUSUARIOS = qrySQL_BENEFICIOSXUSUARIOS + " Where id_beneficio in (" + Beneficio +")";
		}
	}
	
	/**
	 * Método	LeeDatosBeneficioUsuario	Prepara y ejecuta los comandos para obtener los datos a ser transferidos.
	 * 										El primer comando es un count para verificar si en la tabla existen datos.
	 * 										El segundo comando es el select para obtener los registros los cuale se convierten a formato xml
	 * 										y se envia a la cola ActiveMQ.
	 * @param  		Fecha_Proceso			Es la fecha en la cual el proceso se ejecuta
	 * @param  		Conexion 				Es la conección hacia la Base de Datos previamente establecida 
	 * @param 		Cola					Nombre la cola a la cual será enviada la información obtenida
	 * @parama 		archLog  				Nombre del archivo bitácora correspondiente a la ejecución
	 * @throws 		SQLException			Manejo de errores generados por los comandos SQL
	 */
	public void LeeDatosBeneficioUsuario(String Fecha_Proceso, Connection Conexion, ManejoAMQ Cola, GrabaArchivo archLog) throws SQLException
	{
		String Mensaje=null;
		
		// Hay una conexion valida, se prepara el ambiente para ejecutar el select
		try {
			ejSQL_BENEFICIOSXUSUARIOS = Conexion.createStatement();
		} catch (SQLException error) {
			BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
		}

		try {
			// Obtengo el total de registros a leer para grabarlo en el log
			rs_BENEFICIOSXUSUARIOS = ejSQL_BENEFICIOSXUSUARIOS.executeQuery(qrySQL_BENEFICIOSxUSUARIOS_TOTAL);

			while (rs_BENEFICIOSXUSUARIOS.next())
			{
				Total_Registros = rs_BENEFICIOSXUSUARIOS.getInt("totales");
				BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,"Total Registros= "+ String.valueOf(Total_Registros), archLog);
			}
		}
		catch (SQLException error)
			{
				// No hay datos a procesar, se reporta en el log
				BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);

				// Se cierran conexiones y sesiones abiertas
				rs_BENEFICIOSXUSUARIOS.close();
				ejSQL_BENEFICIOSXUSUARIOS.close();
			}

		if(Total_Registros==0)
		{
			//No hay datos a procesar se reporta en el log
			BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,"NO HAY DATOS PARA PROCESAR", archLog);

			// Se cierran conexiones y sesiones abiertas
			rs_BENEFICIOSXUSUARIOS.close();
			ejSQL_BENEFICIOSXUSUARIOS.close();
		} 
		else {
		// Ejecuto el Query para obtener datos de Usuario
		try {
			rs_BENEFICIOSXUSUARIOS = ejSQL_BENEFICIOSXUSUARIOS.executeQuery(qrySQL_BENEFICIOSXUSUARIOS);
						
			while (rs_BENEFICIOSXUSUARIOS.next() && Total_Registros > 0)
			{
				
				//System.out.println(
				Mensaje = "<beneficiosxususario>" +
						"<row>" +
						"<cui>" + String.valueOf(rs_BENEFICIOSXUSUARIOS.getLong("cui")) + "</cui>" +
						"<id_beneficio>" + String.valueOf(rs_BENEFICIOSXUSUARIOS.getInt("id_beneficio")) + "</id_beneficio>" +
						"<status>" + rs_BENEFICIOSXUSUARIOS.getString("status") + "</status>" +
						"</row>" +		
						"</beneficiosxusuario>";
				 // Fin del Mensaje
				 Cola.EnvioMsjAMQ(Fecha_Proceso, Mensaje, archLog, BeneficiosxUsuarioMsj);
				 
				} // Fin del While que recorre la tabla while (rs_BENEFICIOSXUSUARIOS.next())
			
				// Se cierran conexiones y sesiones abiertas
				rs_BENEFICIOSXUSUARIOS.close();
				ejSQL_BENEFICIOSXUSUARIOS.close();
				}
			catch (SQLException error)
			{
				// Este mensaje debe ir a la cola de ERRORES
				BeneficiosxUsuarioMsj.Mensaje("BeneficiosxUsuario",Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
				
				// Se cierran conexiones y sesiones abiertas
				rs_BENEFICIOSXUSUARIOS.close();
				ejSQL_BENEFICIOSXUSUARIOS.close();
			}
		} // FIN contador==0
	}
}
