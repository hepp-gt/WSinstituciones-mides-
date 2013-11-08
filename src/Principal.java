import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.FileNotFoundException;
// Libreria para utilizar SQL
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

// Mis clases para el proyecto
import com.archivo.acceso.*;

//Libreria para utilizar mensajeria de JAVA
import javax.jms.*;

//Libreria para utilizar Colas de ActiveMQ
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.selector.ParseException;

import com.archivo.acceso.ConexionDB;
import com.archivo.acceso.FormateaMensaje;
import com.archivo.acceso.LeeArchivo;
import com.archivo.acceso.VerificaFechaCarga;
import com.archivo.acceso.GrabaArchivo;
import com.archivo.acceso.DatosUsuario;
import com.archivo.acceso.DatosBeneficiosUsuario;
import com.archivo.acceso.DatosEntregas;
import com.archivo.acceso.ManejoAMQ;


public class Principal {
	private ConexionDB 		conecta;
	private Connection 		con_SISO;				//Variable para la conexión hacia la Base de Datos		
	private Statement 		ejSQL_SISO;				//Para ejecutar el select
	private ResultSet 		rs_SISO; 				//Datos_SISO es el result set que almacena los resultados del select
	private GrabaArchivo 	ArchLog;
	private FormateaMensaje	Msjs;
	private String			Fecha_Proceso;
	private String 			Fecha_Actual;
	private ManejoAMQ		ColaAMQ=null;
	private Date			Fecha_Hoy;
	
	public String Ejecucion(String Programa, String strFecIni, String strFecCar, String strDias, String strDBMS, String strHost, String strPrt, String strDB, String strUsr,String strPwd,String strInst,String strPrg,String strBen, String strHostMQ,String strPrtMQ, String strNMQ) throws SQLException, JMSException
	{
		
		// Tomo la fecha de carga, la fecha inicial y el parametro de días
		// Se calcula la siguiente fecha en que se debe ejecutar la transmisión de datos
		VerificaFechaCarga CalculaDias = null;
		
		//Si la fecha de proceso en el archivo ini es nula implica que no se ha ejecutado; entonces la fecha de carga inicial es la fecha de proceso
		try {
			CalculaDias = new VerificaFechaCarga(strFecIni, strFecCar);
		} catch (java.text.ParseException e) {
			Msjs.Mensaje("PROGRAMA", Fecha_Actual,Programa, "La Fecha de Carga Inicial o Fecha de última Carga son Inválidas" + e.getMessage(), ArchLog);
			// Mensaje va a la cola de ERRORES
		}
		
		//Si existe una fecha de proceso, a esta se le suma el parámetro de días para obtener la nueva fecha para ejecución
		//Obtengo la nueva fecha_proceso = fecha_proceso_anterior + cantidad_de_días
		try {
			Fecha_Proceso = CalculaDias.CalculaFechaProceso(Integer.parseInt(strDias));
		} catch (NumberFormatException | java.text.ParseException e) {
			// Mensaje va a la cola de ERRORES
			Msjs.Mensaje("PROGRAMA", Fecha_Actual,Programa, "Problema al calcular la siguiente Fecha de Carga, Verifique el parámetro de Días " + e.getMessage(), ArchLog);
		}
		
		Fecha_Actual = CalculaDias.FechaActual();

		// Si la nueva fecha de proceso es igual a la fecha actual, se ejecuta el select
		if (Fecha_Proceso.equals(Fecha_Actual))
		{
			// Se debe enviar a la cola de ERRORES el nombre del programa a procesar y la fecha de proceso
			Msjs.Mensaje("PROGRAMA", Fecha_Actual, Programa, "FECHA DE PROCESO VALIDA", ArchLog);
			
			// Se crea el manejo de la cola AMQ
			ColaAMQ = new ManejoAMQ(strHostMQ, strPrtMQ, strNMQ, ArchLog, Msjs, Fecha_Actual);
	
			
			if (ColaAMQ.ConectAMQ(strHostMQ, strPrtMQ, strNMQ, ArchLog, Msjs, Fecha_Actual))
			{
			// Debo crear la conexion a la Cola de ActiveMQ
			// Se crea y realiza la conexión a la DB
			conecta = new ConexionDB(strDBMS,ArchLog,Fecha_Actual);
			con_SISO = conecta.CreaConexion(strDBMS, strHost, strPrt, strDB, strUsr, strPwd,ArchLog,Fecha_Actual);
	
			// Si no existe conexión se envia mensaje de error, de lo contrario se accesa la data y se barre
			if((con_SISO!=null))
			{

				//Para las diferentes ejecuciones se envia la fecha de proceso, la conexión y el archivo log
				
				//Ejecución de la clase para extraer los datos de Usuarios
				DatosUsuario Usuarios = new DatosUsuario(Fecha_Proceso, con_SISO, ArchLog);
				Usuarios.LeeDatosUsuario(Fecha_Proceso, con_SISO, ColaAMQ, ArchLog);
				
				//Ejecución de la clase para extraer los datos de BeneficiosxUsuario
				DatosBeneficiosUsuario Beneficiosxusuario = new DatosBeneficiosUsuario (Fecha_Proceso, con_SISO, ArchLog);
				Beneficiosxusuario.LeeDatosBeneficioUsuario(Fecha_Proceso, con_SISO, ColaAMQ, ArchLog);
				
				//Ejecución de la clase para extraer los datos de Entregas
				DatosEntregas Entregas = new DatosEntregas(Fecha_Proceso, con_SISO, ArchLog);
				Entregas.LeeDatosEntregas(Fecha_Proceso, con_SISO, ColaAMQ, ArchLog);
				
				//Cerrar los objetos de manejo de BD
				con_SISO.close();   
				
				return Fecha_Actual;	
			} //Condicion de conexion (con_SISO!=null)
			else {
				Msjs.Mensaje("PROGRAMA", Fecha_Actual, Programa, "CONEXION INVALIDA REVISE EL ESTADO DE LA DB", ArchLog);
			}			}
		} // Fin Condicion Fecha_Proceso.equals(Fecha_Actual)
		else {
			Msjs.Mensaje("PROGRAMA", Fecha_Actual,Programa, "NO ES FECHA DE EJECUCION", ArchLog);
			//ColaAMQ.EnvioMsjAMQ(Fecha_Proceso, "fin", ArchLog, Msjs);
		}
		return null;
	}
	
	public Principal () throws Exception
	{
		
		//Variables generales a utilizar durante el proceso
		String Linea=null;				//Se utiliza para leer cada linea del archivo de parametros
		String Encabezado=null;			//De cada línea leída se extrae el encabezado el cual se separa del dato por el simbolo =
		String Nombre_Programa=null;	//En el archivo ini pueden existir varios programas, el nombre debe venir entre corchetes []
		
		// Variables de Parametros que se utilizaran para la conexión a la Base de Datos
		String strHost=null;		//Nombre del host o dirección IP del Servidor de Base de Datos
		String strPrt=null;			//Puerto donde escucha la DB
		String strDB=null;			//Nombre de la Base de Datos a utilizar
		String strUsr=null;			//Almacena el usuario
		String strPwd=null;			//Almacena el password
		String strDBMS=null;		//Almacena el DBMS que será utilizado
		String strFecIni=null;		//Fecha de la primera carga
		String strDias=null;		//Cuantos días pasaran para la siguiente carga
		String strGracia=null;		//Días de gracias para recargar la data
		String strNumInt=null;
		String strFecCar=null;		//Fecha de la última carga realizada, esta la coloca el WS
		
		// Variables que identifican los datos de la institucion, programa y beneficio que se extraeran 
		String strInst=null;		//Institución que envia los datos
		String strPrg=null;			//Programa descripcion
		String strBen=null;			//Beneficio que cubre el programa

		// Variables de Parametros que se utilizaran para la conexión hacia ActiveMQ
		String strHostMQ=null;		//Nombre del host o dirección IP del Servidor de Base de Datos
		String strPrtMQ=null;		//Puerto donde escucha la DB
		String strNMQ=null;			//Nombre la Cola a utilizar

		// Obtengo la fecha actual para generar el archivo log
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		Date Fecha_Hoy = new Date();
		Fecha_Actual = dateFormat.format(Fecha_Hoy);
		
		// Abro el archivo de Parámetros y se prepara para la lectura
		LeeArchivo Archivo = new LeeArchivo("C:\\SISO_Apps\\Parametros.ini");
		Linea =  Archivo.Leerlinea();
		
		// Abro el archivo temporal donde se almacenan los cambios que se haran al archivo de Parámetros
		GrabaArchivo NuevoArch = new GrabaArchivo("C:\\SISO_Apps\\temporal.ini");
		
		// Se crea el archivo log para la presente ejecución
		ArchLog = new GrabaArchivo("C:\\SISO_Apps\\Logs\\Log_"+ Fecha_Actual + ".dat");
		
		Msjs = new FormateaMensaje (null,Fecha_Actual,"Parametros","REVISION",ArchLog);		
				
		// Recorro el archivo para obtener los distintos parámetros que utiliza la aplicación
		while (Linea!=null)
		{
			
			if(Linea.indexOf("]")>0)
			{
				//Obtengo el nombre del programa a procesar
				Nombre_Programa = Linea.substring(1,Linea.indexOf("]"));
			}
			if(Linea.indexOf("=")>0)
			{	
				Encabezado = Linea.substring(0,Linea.indexOf("="));
				//System.out.println(Linea);
				if(Encabezado.equals("HOST"))
				{
					strHost=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				} else if(Encabezado.equals("PUERTO"))
				{
					strPrt=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}  else if(Encabezado.equals("BASENOMBRE"))
				{
					strDB=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("USUARIO"))
				{
					strUsr=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("PASSWORD"))
				{
					strPwd=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}  else if(Encabezado.equals("RDBMS"))
				{
					strDBMS=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("INSTITUCION"))
				{
					strInst=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("PROGRAMA"))
				{
					strPrg=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("BENEFICIO"))
				{
					strBen=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("HOSTMQ"))
				{
					strHostMQ=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("PUERTOMQ"))
				{
					strPrtMQ=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("NOMBREMQ"))
				{
					strNMQ=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("FECHA_INICIAL"))
				{
					strFecIni=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}    else if(Encabezado.equals("DIAS"))
				{
					strDias=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("FECHA_CARGA"))
				{
					strFecCar=Linea.substring(Linea.indexOf("=")+1,Linea.length());
					
					Msjs.Mensaje("PROGRAMA",Fecha_Actual, Nombre_Programa, "Calculo Fecha Ejecución", ArchLog);
					// El parametro FECHA_CARGA es el ultimo de cada programa por ello aca se ejecuta la obtención de datos
					String Ejecutado  = Ejecucion(Nombre_Programa, strFecIni, strFecCar, strDias, strDBMS, strHost, strPrt, strDB, strUsr, strPwd,strInst,strPrg,strBen,strHostMQ, strPrtMQ, strNMQ);
			
					if (Ejecutado!=null)
					{
						strFecCar = Ejecutado;
						Msjs.Mensaje("PROGRAMA", Fecha_Actual,Nombre_Programa, "FIN DE EJECUCION", ArchLog);
					}
					else {
						strFecCar = "";
						Msjs.Mensaje("PROGRAMA", Fecha_Actual,Nombre_Programa, "EJECUCION FALLIDA", ArchLog);
						// Se debe ingresar mensaje de no ejecucion en la cola de ERRORES
					}
					Linea = Linea.substring(0,Linea.indexOf("=")+1) + strFecCar;
				}
				
			} // Fin de la condición Linea.indexOf("=")>0
		
			NuevoArch.GrabaLinea(Linea);
			
			Linea =  Archivo.Leerlinea();
		} // Finaliza el While que recorre el archivo de parámetros
		NuevoArch.GrabaLinea("fin");
		ArchLog.GrabaLinea("fin");
}
	
	public static void main(String[] args) throws Exception
	{
			new Principal();
	}
}