package com.archivo.acceso;

//Libreria para utilizar SQL
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;


public class DatosUsuario {

	private Statement 		ejSQL_USUARIOS;		//Para ejecutar el select
	private ResultSet 		rs_USUARIOS; 		//Datos_USUARIOS es el result set que almacena los resultados del select
	private FormateaMensaje UsuariosMsj;
	private String			Tablaxusar="tbl_usuario";	
	private int				Total_Registros=0;	//Almacena el total de registros a leer	
				
	String qrySQL_USUARIOS="SELECT 	cui" + 
						",id_nucleo" + 					 
						",id_ocupacion" + 				 
						",id_parentesco" +			 
						",id_escolaridad" +			 
						",id_pueblo" + 					 
						",id_comunidad_linguistica" + 	 
						",sexo" + 						 
						",trabaja" + 					 
						",apellido1" + 					 
						",apellido2" + 					 
						",apellido_casada" + 			 
						",nombre1" + 					 
						",nombre2" + 					 
						",nombre3" + 					 
						",telefono" + 					 
						",direccion" + 					
						",fecha_nacimiento" + 			
						",id_municipio_nacimiento" + 	 
						",id_departamento_nacimiento" + 	 
						",id_municipio_residencia" + 	 
						",id_departamento_residencia" + 	 
						",id_lugar_poblado_residencia" +  
						",orden_cedula" + 				
						",registro_cedula" + 			
						",libro_partida" + 				 
						",folio_partida" + 				 
						",numero_partida" + 				 
						" from " + Tablaxusar;	
	
	public DatosUsuario(String Fecha_Proceso, Connection Conexion, GrabaArchivo archLog)
	{
		UsuariosMsj = new FormateaMensaje (Conexion,Fecha_Proceso, Tablaxusar ,"Inicio Proceso",archLog);

		if (Conexion==null)
		{
			UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,"Verifique la CONEXIOON hacia la DB", archLog);
		} else 
		{
			UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,"SE INICIA LA LECTURA", archLog);			
		}	

	}
	
	public void LeeDatosUsuario(String Fecha_Proceso, Connection Conexion,ManejoAMQ Cola, GrabaArchivo archLog) throws SQLException
	{
		String Mensaje=null;
		
		// Hay una conexion valida, se prepara el ambiente para ejecutar el select
		try {
			ejSQL_USUARIOS = Conexion.createStatement();
		} catch (SQLException error) {
			UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
		}

		try {
			// Obtengo el total de registros a leer para grabarlo en el log
			rs_USUARIOS = ejSQL_USUARIOS.executeQuery("select totales=count(*) from tbl_usuario");

			while (rs_USUARIOS.next())
			{
				Total_Registros = rs_USUARIOS.getInt("totales");
				UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,"Total Registros= "+ String.valueOf(Total_Registros), archLog);
			}
		}
		catch (SQLException error)
			{
				// No hay datos a procesar, se reporta en el log
				UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);

				// Se cierran las conexiones y sesiones abiertas
				rs_USUARIOS.close();
				ejSQL_USUARIOS.close();
			}

		if(Total_Registros==0)
		{
			//No hay datos a procesar se reporta en el log
			UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,"NO HAY DATOS PARA PROCESAR", archLog);
			
			// Se cierran las conexiones y sesiones abiertas
			rs_USUARIOS.close();
			ejSQL_USUARIOS.close();
		}
		else {
		// Ejecuto el Query para obtener datos de Usuario
		try {
			rs_USUARIOS = ejSQL_USUARIOS.executeQuery(qrySQL_USUARIOS);
						
			while (rs_USUARIOS.next() && Total_Registros > 0)
			{
				//System.out.println(
				Mensaje = 
						"<usuario>" +
						"<row>" +		
						"<cui>" + String.valueOf(rs_USUARIOS.getLong("cui")) + "</cui>" +
						"<id_nucleo>" +	String.valueOf(rs_USUARIOS.getInt("id_nucleo")) + "</id_nucleo>" +
						"<id_ocupacion>" + String.valueOf(rs_USUARIOS.getInt("id_ocupacion")) + "</id_ocupacion>" +
						"<id_parentesco>" + String.valueOf(rs_USUARIOS.getInt("id_parentesco")) + "</id_parentesco>" +
						"<id_escolaridad>" + String.valueOf(rs_USUARIOS.getInt("id_escolaridad")) + "</id_escolaridad>" +
						"<id_pueblo>" + String.valueOf(rs_USUARIOS.getInt("id_pueblo")) + "</id_pueblo>" +
						"<id_comunidad_linguistica>" + String.valueOf(rs_USUARIOS.getInt("id_comunidad_linguistica")) + "</id_comunidad_linguistica>" +
						"<sexo>" + String.valueOf(rs_USUARIOS.getInt("sexo")) + "</sexo>" +
						"<trabaja>" + String.valueOf(rs_USUARIOS.getInt("trabaja")) + "</trabaja>" +
						"<apellido1>" + rs_USUARIOS.getString("apellido1") + "</apellido1>" +
						"<apellido2>" + rs_USUARIOS.getString("apellido2") + "</apellido2>" +
						"<apellido_casada>" + rs_USUARIOS.getString("apellido_casada") + "</apellido_casada>" +
						"<nombre1>" + rs_USUARIOS.getString("nombre1") + "</nombre1>" +
						"<nombre2>" + rs_USUARIOS.getString("nombre2") + "</nombre2>" +
						"<nombre3>" + rs_USUARIOS.getString("nombre3") + "</nombre3>" +
						"<telefono>" + String.valueOf(rs_USUARIOS.getInt("telefono")) + "</telefono>" +
						"<direccion>" + rs_USUARIOS.getString("direccion") + "</direccion>" +
						"<fecha_nacimiento>" + rs_USUARIOS.getString("fecha_nacimiento") + "</fecha_nacimiento>" +
						"<id_municipio_nacimiento>" + String.valueOf(rs_USUARIOS.getInt("id_municipio_nacimiento")) + "</id_municipio_nacimiento>" +
						"<id_departamento_nacimiento>" + String.valueOf(rs_USUARIOS.getInt("id_departamento_nacimiento")) + "</id_departamento_nacimiento>" +
						"<id_municipio_residencia>" + String.valueOf(rs_USUARIOS.getInt("id_municipio_residencia")) + "</id_municipio_residencia>" +
						"<id_departamento_residencia>" + String.valueOf(rs_USUARIOS.getInt("id_departamento_residencia")) + "</id_departamento_residencia>" +
						"<id_lugar_poblado_residencia>" + String.valueOf(rs_USUARIOS.getInt("id_lugar_poblado_residencia")) + "</id_lugar_poblado_residencia>" +
						"<orden_cedula>" + rs_USUARIOS.getString("orden_cedula") + "</orden_cedula>" +
						"<registro_cedula>" + rs_USUARIOS.getString("registro_cedula") + "</registro_cedula>" +
						"<libro_partida>" + rs_USUARIOS.getString("libro_partida") + "</libro_partida>" +
						"<folio_partida>" + rs_USUARIOS.getString("folio_partida") + "</folio_partida>" +
						"<numero_partida>" + rs_USUARIOS.getString("numero_partida") + "</numero_partida>" +
						"</row>" +
						"</usuario>";
			
					Cola.EnvioMsjAMQ(Fecha_Proceso, Mensaje, archLog, UsuariosMsj);
					// Fin del Mensaje
				} // Fin del While que recorre la tabla while (rs_USUARIOS.next())
			
				// Se cierran las conexiones y sesiones abiertas
				rs_USUARIOS.close();
				ejSQL_USUARIOS.close();
			}
			catch (SQLException error)
			{
				// Este mensaje debe ir a la cola de ERRORES
				UsuariosMsj.Mensaje("Usuarios", Fecha_Proceso, Tablaxusar,error.getMessage(), archLog);
				
				// Se cierran las conexiones y sesiones abiertas
				rs_USUARIOS.close();
				ejSQL_USUARIOS.close();
			}
		} //Fin del else contador==0

	}
}
