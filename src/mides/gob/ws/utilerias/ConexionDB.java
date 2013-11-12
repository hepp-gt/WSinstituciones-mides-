package mides.gob.ws.utilerias;

/**
 * ConexionDB es la clase que permite realizar la conecci�n hacia la Base de Datos
 * @author 	Mynor Pacheco
 * @version	1.0
 * @see		java.sql.Connection		
 * @see		java.sql.DriverManager
 * @see		java.sql.SQLException
 */

// Variables a utilizarse para extraer datos de una DB
import java.sql.Connection;				//Variable para la conexi�n hacia la Base de Datos		
import java.sql.DriverManager;
import java.sql.SQLException;



public class ConexionDB {
	private FormateaMensaje DBMsj; 

	/** 
	 * Constructor	ConexionDB: Verifica que el driver requerido para la conecci�n se encuentre instalado en el equipo
	 * @author 		Mynor Pacheco
	 * @param		DBMS			Refiere a la DB utilizada por la instituci�n (SQLServer, Oracle, MySQL, PostgreSQL)
	 * @param 		archLog			Nombre del archivo bit�cora correspondiente a la ejecuci�n
	 * @param  		Fecha_Actual	Es la fecha en la cual el proceso se ejecuta		
	 */
	
	public ConexionDB(String DBMS, GrabaArchivo archLog, String Fecha_Actual)
	{
		DBMsj = new FormateaMensaje (Fecha_Actual, "Base Datos" ,archLog);
		
		// Se verificar� si existe el Driver para el DBMS seleccionado
		// SQL --> SQLServer
		// ORA --> Oracle
		// MYS --> MySQL
		// PGS --> PostGresSQL
		
		if(DBMS.equals("SQL"))
		{
			//Aca va la l�gica para el driver de SQLServer
			try {
		        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
				}
		   	 catch ( ClassNotFoundException Error )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver SQLServer", Error.getMessage(), archLog);
		    	}
		}

		if(DBMS.equals("ORA"))
		{
			//Aca va la l�gica para el driver de SQLServer
			try {
		        Class.forName("oracle.jdbc.driver.OracleDriver()"); 
				}
		   	 catch ( ClassNotFoundException Error )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver Oracle", Error.getMessage(), archLog);
		    	}
		}

		if(DBMS.equals("MYS"))
		{
			//Aca va la l�gica para el driver de SQLServer
			try {
		        Class.forName("com.mysql.jdbc.Driver"); 
				}
		   	 catch ( ClassNotFoundException Error )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver MySQL", Error.getMessage(), archLog);
		    	}
		}
		
		if(DBMS.equals("PGS"))
		{
			//Aca va la l�gica para el driver de SQLServer
			try {
		        Class.forName("org.postgresql.Driver"); 
				}
		   	 catch ( ClassNotFoundException Error )
		    	{
		   		 	DBMsj.Mensaje("DB", Fecha_Actual, "Problema de Driver PostgreSQL", Error.getMessage(), archLog);
		    	}
		}
	}
	
	/**
	 * M�todo		CreaConexion: Crea el URL o cadena de conecci�n hacia la Base de Datos.
	 * 	 			La conecci�n utilizar� el formato de URL adecuado seg�n el DBMS a utilizar. 
	 * @author 		Mynor Pacheco
	 * @param		DBMS			Refiere a la DB utilizada por la instituci�n (SQLServer, Oracle, MySQL, PostgreSQL)
	 * @param 		Host			Direcci�n IP o nombre del Servidor donde reside la Base de Datos
	 * @param 		Prt				Puerto en el cual la Base de Datos escucha las peticiones
	 * @param 		DB				Nombre de la Base de Datos donde residen las tablas a procesar
	 * @param 		Usr				Usuario de la Base de Datos
	 * @param 		Pwd				Contrase�a del usuario
	 * @param 		archLog			Nombre del archivo bit�cora correspondiente a la ejecuci�n
	 * @param  		Fecha_Actual	Es la fecha en la cual el proceso se ejecuta
	 * @return 		El valor que retorna es tipo Connection y refiere a la conecci�n establecida hacia la Base de Datos
	 */
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
			
			DBMsj.Mensaje("DB", Fecha_Actual, "Problema en Conexi�n a la DB", error.getMessage(), archLog);
			return null;
		}
	}
}
