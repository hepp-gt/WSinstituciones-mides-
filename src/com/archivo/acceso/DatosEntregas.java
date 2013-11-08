package com.archivo.acceso;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatosEntregas {

	private Statement 		ejSQL_ENTREGAS;		//Para ejecutar el select
	private ResultSet 		rs_ENTREGAS; 		//Datos_ENTREGAS es el result set que almacena los resultados del select
	private FormateaMensaje EntregasMsj;
	
	private String			Tablaxusar="tbl_entrega_beneficio";
	private int				Total_Registros=0;	//Almacena el total de registros a leer	
				
	String qrySQL_ENTREGAS="Select " +
			"id_beneficio" +
			",id_programa" +
			",cui" +
			",nombre1" +
			",nombre2" +
			",nombre3" +
			",apellido1" +
			",apellido2" +
			",apellido_casada" +
			",Departamento_Otorgamiento_Beneficio" +
			",Municipio_Otorgamiento_Beneficio" +
			",Fecha_Otorgamiento_Beneficio" +
			",Valor" +
 			" from "+Tablaxusar;
		
	public DatosEntregas(String Fecha_Proceso, Connection Conexion, GrabaArchivo archLog)
	{
		EntregasMsj = new FormateaMensaje (Conexion,Fecha_Proceso, Tablaxusar,"Inicio Proceso",archLog);

		if (Conexion==null)
		{
			EntregasMsj.Mensaje("Entregas", Fecha_Proceso, Tablaxusar,"Verifique la CONEXIOON hacia la DB", archLog);
		} else 
		{
			EntregasMsj.Mensaje("Entregas",Fecha_Proceso, Tablaxusar,"SE INICIA LA LECTURA", archLog);			
		}
	}
	
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
			rs_ENTREGAS = ejSQL_ENTREGAS.executeQuery("select totales=count(*) from tbl_entrega_beneficio");

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
						"<id_beneficio>" + String.valueOf(rs_ENTREGAS.getInt("id_beneficio")) + "</id_beneficio>" +
						"<id_programa>" + String.valueOf(rs_ENTREGAS.getInt("id_programa")) + "</id_programa>" +
						"<cui>" + String.valueOf(rs_ENTREGAS.getLong("cui")) + "</cui>" +
						"<nombre1>" + rs_ENTREGAS.getString("nombre1") + "</nombre1>" +
						"<nombre2>" + rs_ENTREGAS.getString("nombre2") + "</nombre2>" +
						"<nombre3>" + rs_ENTREGAS.getString("nombre3") + "</nombre3>" +
						"<apellido1>" + rs_ENTREGAS.getString("apellido1") + "</apellido1>" +
						"<apellido2>" + rs_ENTREGAS.getString("apellido2") + "</apellido2>" +
						"<apellido_casada>" + rs_ENTREGAS.getString("apellido_casada") + "</apellido_casada>" +
						"<departamento_otorgamiento_beneficio>" + String.valueOf(rs_ENTREGAS.getLong("Departamento_Otorgamiento_Beneficio")) + "</departamento_otorgamiento_beneficio>" +
						"<municipio_otorgamiento_beneficio>" + String.valueOf(rs_ENTREGAS.getLong("Municipio_Otorgamiento_Beneficio")) + "</municipio_otorgamiento_beneficio>" +
						"<fecha_otorgamiento_beneficio>" + rs_ENTREGAS.getString("Fecha_Otorgamiento_Beneficio") + "</fecha_otorgamiento_beneficio>" +
						"<valor>" + String.valueOf(rs_ENTREGAS.getFloat("Valor")) + "</valor>" +
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
