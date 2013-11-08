package com.archivo.acceso;

// Variables a utilizarse para extraer datos de una DB
import java.sql.Connection;				//Variable para la conexión hacia la Base de Datos		
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
	private FormateaMensaje DBMsj; 
	
	public ConexionDB(String DBMS, GrabaArchivo archLog, String Fecha_Actual)
	{
		DBMsj = new FormateaMensaje (null,Fecha_Actual, null,"Base Datos" ,archLog);
		
		// Se verificará si existe el Driver para el DBMS seleccionado
		// SQL --> SQLServer
		// ORA --> Oracle
		// MYS --> MySQL
		// PGS --> PostGresSQL
		
		if(DBMS.equals("SQL"))
		{
			//Aca va la lógica para el driver de SQLServer
			try {
		        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
				}
		   	 catch ( ClassNotFoundException Er )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver SQLServer", Er.getMessage(), archLog);
		    	}
		}

		if(DBMS.equals("ORA"))
		{
			//Aca va la lógica para el driver de SQLServer
			try {
		        Class.forName("oracle.jdbc.driver.OracleDriver()"); 
				}
		   	 catch ( ClassNotFoundException Er )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver Oracle", Er.getMessage(), archLog);
		    	}
		}

		if(DBMS.equals("MYS"))
		{
			//Aca va la lógica para el driver de SQLServer
			try {
		        Class.forName("com.mysql.jdbc.Driver"); 
				}
		   	 catch ( ClassNotFoundException Er )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver MySQL", Er.getMessage(), archLog);
		    	}
		}
		
		if(DBMS.equals("PGS"))
		{
			//Aca va la lógica para el driver de SQLServer
			try {
		        Class.forName("org.postgresql.Driver"); 
				}
		   	 catch ( ClassNotFoundException Er )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver PostgreSQL", Er.getMessage(), archLog);
		    	}
		}
	}
	
	public Connection CreaConexion(String DBMS, String Host, String Prt, String DB, String Usr, String Pwd, GrabaArchivo archLog, String Fecha_Actual)
	{
		Connection Conect=null;
		
		String Surl = null;
		
		if(DBMS.equals("SQL"))
		{
			// Se crea la cadena de conexion previo a realizarla
			Surl = "jdbc:sqlserver://"+Host+":"+Prt+";databasename="+DB+";user="+Usr+";password="+Pwd;
		}
		
		if(DBMS.equals("ORA"))
		{
			// Se crea la cadena de conexion previo a realizarla
			Surl = "jdbc:oracle:thin:@"+Host+":"+Prt+":"+DB+","+Usr+","+Pwd;
		}
		
		if(DBMS.equals("MYS"))
		{
			// Se crea la cadena de conexion previo a realizarla
			Surl = "jdbc:mysql://"+Host+":"+Prt+"/"+DB+","+Usr+","+Pwd;
		}
		
		if(DBMS.equals("PGS"))
		{
			// Se crea la cadena de conexion previo a realizarla
			Surl = "jdbc:postgresql://"+Host+":"+Prt+"/"+DB+","+Usr+","+Pwd;
		}
		
		try {
			Conect =DriverManager.getConnection(Surl);
			
			if (Conect!=null)
			{
				return Conect; 
			}
			else {
				DBMsj.Mensaje("DB", Fecha_Actual, "PROBLEMA DE CONEXION CON LA DB", "", archLog);
				return null;
			}
		} catch (SQLException error) {
			
			DBMsj.Mensaje("DB", Fecha_Actual, "Problema en Conexión a la DB", error.getMessage(), archLog);
			return null;
		}
	}
}
