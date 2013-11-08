package com.archivo.acceso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class VerificaFechaCarga {

	 Date Fecha;
	 
		public VerificaFechaCarga (String fecha_inicial, String fecha_carga) throws ParseException
		{
		     SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd/MM/yyyy");
		     // Si no existe Fecha de Carga implica que es una carga nueva, por lo tanto no se debe utilizar el campo de Dias
			 // Se convierten los parametros fecha_inicial y fecha_carga en campo Date 
			if ((fecha_carga==null) || (fecha_carga.equals("")))
			{
				     Fecha = formatoDelTexto.parse(fecha_inicial);
			} else 
			{
				     Fecha = formatoDelTexto.parse(fecha_carga);
			} 
		}
		
		public String FechaActual()
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date Fecha = new Date();
			return dateFormat.format(Fecha);
		}	
		
		public String CalculaFechaProceso(int dias) throws java.text.ParseException 
	    {
		 	// Obtengo una instancia del calendario para luego sumarle el parametro de Días
			// a la última fecha procesada y así obtener la nueva fecha de ejecución 
			Calendar cal = Calendar.getInstance();  
			cal.setTime(Fecha);  
			cal.add(Calendar.DATE, dias);  // sumo el parámetro de Días a la última fecha de ejecución 
			Fecha = cal.getTime();
			
			// Convierto el formato de date a un string y se devuelve al programa principal
			SimpleDateFormat formatofinal = new SimpleDateFormat("dd/MM/yyyy");
			return formatofinal.format(Fecha);
	    }
}
