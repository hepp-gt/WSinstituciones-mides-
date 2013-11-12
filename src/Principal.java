// Librerias Genericas de Java
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Libreria para utilizar SQL
import java.sql.Connection;
import java.sql.SQLException;

//Libreria para utilizar mensajeria de JAVA
import javax.jms.*;

//Mis clases para el proyecto
import mides.gob.ws.leeinformacion.DatosBeneficiosUsuario;
import mides.gob.ws.leeinformacion.DatosEntregas;
import mides.gob.ws.leeinformacion.DatosUsuario;
import mides.gob.ws.utilerias.ConexionDB;
import mides.gob.ws.utilerias.FormateaMensaje;
import mides.gob.ws.utilerias.GrabaArchivo;
import mides.gob.ws.utilerias.LeeArchivo;
import mides.gob.ws.utilerias.ManejoAMQ;

public class Principal {
	private ConexionDB 		conecta;
	private Connection 		con_SISO;				//Variable para la conexión hacia la Base de Datos		
	private GrabaArchivo 	ArchLog;
	private FormateaMensaje	Msjs;
	private String 			Fecha_Actual;
	private ManejoAMQ		ColaAMQ=null;
	
	public boolean ParserParametro (String Parametro, String ValorHoy)
	{
		String valor=null;
		
		// Recorro el parametro, cada vez que encuentro un separador (,) obtengo el valor y sobre calculo si el ValorHoy es válido
		// De ser válido rompo el ciclo con un retorno verdadero, de lo contrario continua el ciclo
		// si el ciclo termina y no se válido el ValorHoy se retorna un false que implica que no hay que ejecutar el proceso
		
		if(Parametro.equals("*")) return true;
		if(Parametro.equals("0")) return false;
		
		while ((Parametro.length()>0))
		{
			if(Parametro.indexOf(",")>0)
			{
				valor = Parametro.substring(0, Parametro.indexOf(","));
				Parametro=Parametro.replace(valor+"," ,"");
			} else
			{
				valor=Parametro;
				Parametro=Parametro.replace(valor,"");
			}

			if(valor.equals(ValorHoy)) return true;
			
		}
		// Se recorrio el parametro en su totalidad, el ciclo no se rompio por lo tanto no existe el valor de hoy en el parametro, retorna falso
		return false;
	}
	
	public boolean ValidaEsFechaProceso(String p_dia_mes, String p_mes, String p_dia_semana,String Programa) throws java.text.ParseException 
    {
		
		boolean dia_mes_valido=false,mes_valido=false,dia_semana_valido=false;
		
		Calendar calc = Calendar.getInstance();
		
		String l_mes = String.valueOf(calc.get(Calendar.MONTH)+1);
		String l_dia_mes = String.valueOf(calc.get(Calendar.DAY_OF_MONTH));
		String l_dia_semana = String.valueOf(calc.get(Calendar.DAY_OF_WEEK));
		
		if ((p_dia_mes.equals(" ")) || (p_dia_mes==null))
		{
			Msjs.Mensaje("PARAMETROS", Fecha_Actual, Programa, "Parametro de dia del mes INVALIDO", ArchLog);
			return false;
		}
		
		if ((p_mes.equals(" ")) || (p_mes==null))
		{
			Msjs.Mensaje("PARAMETROS", Fecha_Actual, Programa, "Parametro del mes INVALIDO", ArchLog);
			return false;
		}
		
		if ((p_dia_semana.equals(" ")) || (p_dia_semana==null))
		{
			Msjs.Mensaje("PARAMETROS", Fecha_Actual, Programa, "Parametro día de la semana INVALIDO", ArchLog);
			return false;
		}
		
	    //Paseo el parametro buscando si el valor de hoy es válido para ejecutar el proceso
		//los parametros que inician con p_ son los parámetros que vienen del archivo parametro.ini 
		//las variables con los datos de hoy inician con l_ y esto son los que se validan contra los p_
		dia_mes_valido=ParserParametro(p_dia_mes,l_dia_mes);
		mes_valido=ParserParametro(p_mes,l_mes);
		dia_semana_valido=ParserParametro(p_dia_semana,l_dia_semana);
		
		if (p_dia_mes.equalsIgnoreCase("0")) mes_valido=false;
		if (p_mes.equalsIgnoreCase("0")) { dia_mes_valido=false; dia_semana_valido=false;}
		if (p_dia_semana.equalsIgnoreCase("0")) mes_valido=false;
		
		return dia_mes_valido || mes_valido || dia_semana_valido;
    }
	
	public String Ejecucion(String Programa, String strFecIni, String strDBMS, String strHost, String strPrt, String strDB, String strUsr,String strPwd,String strInst,String strPrg,String strBen, String strHostMQ,String strPrtMQ, String strNMQ) throws SQLException, JMSException
	{
		
	
			// Se crea el manejo de la cola AMQ
			ColaAMQ = new ManejoAMQ(strHostMQ, strPrtMQ, strNMQ, ArchLog, Msjs, Fecha_Actual);
	
			
			if (ColaAMQ.ConectAMQ(strNMQ, ArchLog, Msjs, Fecha_Actual))
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
				DatosUsuario Usuarios = new DatosUsuario(Fecha_Actual, con_SISO, ArchLog);
		
				Usuarios.LeeDatosUsuario(Fecha_Actual, con_SISO, ColaAMQ, ArchLog);
				
				//Ejecución de la clase para extraer los datos de BeneficiosxUsuario
				DatosBeneficiosUsuario Beneficiosxusuario = new DatosBeneficiosUsuario (Fecha_Actual, strBen,  con_SISO, ArchLog);
				Beneficiosxusuario.LeeDatosBeneficioUsuario(Fecha_Actual, con_SISO, ColaAMQ, ArchLog);
				
				//Ejecución de la clase para extraer los datos de Entregas
				DatosEntregas Entregas = new DatosEntregas(Fecha_Actual, strPrg, strBen, con_SISO, ArchLog);
				Entregas.LeeDatosEntregas(Fecha_Actual, con_SISO, ColaAMQ, ArchLog);
				
				//Cerrar los objetos de manejo de BD
				con_SISO.close();   
				
				return Fecha_Actual;	
			} //Condicion de conexion (con_SISO!=null)
			else {
				Msjs.Mensaje("PROGRAMA", Fecha_Actual, Programa, "CONEXION INVALIDA REVISE EL ESTADO DE LA DB", ArchLog);
			}			}
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
		
		// Se crea el archivo log para la presente ejecución
		ArchLog = new GrabaArchivo("C:\\SISO_Apps\\Logs\\Log_"+ Fecha_Actual + ".dat");
		
		Msjs = new FormateaMensaje (Fecha_Actual,"REVISION",ArchLog);		
				
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
				}   else if(Encabezado.equals("HOSTMQ"))
				{
					strHostMQ=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("PUERTOMQ"))
				{
					strPrtMQ=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}   else if(Encabezado.equals("NOMBREMQ"))
				{
					strNMQ=Linea.substring(Linea.indexOf("=")+1,Linea.length());
				}    else if(Encabezado.equals("EJECUCION"))
				{
					Linea = Linea.substring(Linea.indexOf("=")+1,Linea.length());
				
					// Es una línea de calendarización
					// Debo parsear la línea para obtener los parametros que deciden si en la fecha presente se debe ejecutar
					String	dia_mes=null;
					String  mes=null;
					String  dia_semana=null;

					// Obtengo el día del mes definido para ejecución
					dia_mes = Linea.substring(0,Linea.indexOf(" "));
					Linea = Linea.substring(Linea.indexOf(" ")+1,Linea.length());
					
					// Obtengo el mes definido para ejecución
					mes = Linea.substring(0,Linea.indexOf(" "));
					Linea = Linea.substring(Linea.indexOf(" ")+1,Linea.length());
				
					// Obtengo el dia de la semana definido para ejecución
					dia_semana = Linea.substring(0,Linea.indexOf(" "));
					Linea = Linea.substring(Linea.indexOf(" ")+1,Linea.length());
				
					// Obtengo los beneficios a ejecutar
					strBen = Linea.substring(0,Linea.length());
				
					Msjs.Mensaje("PROGRAMA",Fecha_Actual, Nombre_Programa, "Calculo Fecha Ejecución", ArchLog);
	
					// El parametro FECHA_CARGA es el ultimo de cada programa por ello aca se ejecuta la obtención de datos
				
					if(ValidaEsFechaProceso(dia_mes, mes, dia_semana,Nombre_Programa)==true)
					{
						String Ejecutado=null;
				
						Ejecutado = Ejecucion(Nombre_Programa, strFecIni, strDBMS, strHost, strPrt, strDB, strUsr, strPwd,strInst,strPrg,strBen,strHostMQ, strPrtMQ, strNMQ);
				   
						if (Ejecutado!=null)
						{
							// Finaliza la ejecución del programa social
							Msjs.Mensaje("PROGRAMA", Fecha_Actual,Nombre_Programa, "FIN DE EJECUCION", ArchLog);
						}
						else {
							// Se debe ingresar mensaje de no ejecucion en la cola de ERRORES
							Msjs.Mensaje("PROGRAMA", Fecha_Actual,Nombre_Programa, "EJECUCION FALLIDA", ArchLog);
						}
					} // Fin if(SeProcesa.ValidaEsFechaProceso
				} // Entra a linea CAL
		  } // (Linea.indexOf("=")>0)
			
			// Leo la siguiente linea del archivo parametro.ini
			Linea =  Archivo.Leerlinea();
		
			
		} // Finaliza el While que recorre el archivo de parámetros
		Archivo.cierra();
		Msjs.Mensaje("PROGRAMA", Fecha_Actual,Nombre_Programa, "PROCESO FINALIZADO", ArchLog);
		ArchLog.GrabaLinea("fin");
}
	
	public static void main(String[] args) throws Exception
	{
			new Principal();
	}
}