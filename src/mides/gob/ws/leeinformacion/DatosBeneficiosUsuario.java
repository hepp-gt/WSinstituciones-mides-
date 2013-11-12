package mides.gob.ws.leeinformacion;

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
					",fecha_cambio" + 
					" from " + Tablaxusar;
		
	String qrySQL_BENEFICIOSxUSUARIOS_TOTAL="select totales=count(*) from " + Tablaxusar;
	
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

		if(!Beneficio.equals("*"))
		{
			qrySQL_BENEFICIOSxUSUARIOS_TOTAL = qrySQL_BENEFICIOSxUSUARIOS_TOTAL + " Where id_beneficio in (" + Beneficio +")"; 
			qrySQL_BENEFICIOSXUSUARIOS = qrySQL_BENEFICIOSXUSUARIOS + " Where id_beneficio in (" + Beneficio +")";
			
		}
	}
	
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
						"<fecha_cambio>" + rs_BENEFICIOSXUSUARIOS.getString("fecha_cambio") + "</fecha_cambio>" +
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
