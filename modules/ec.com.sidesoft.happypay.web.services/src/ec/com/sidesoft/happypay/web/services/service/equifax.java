package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.web.WebService;
import org.hibernate.criterion.Restrictions;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.ws.equifax.SweqxEquifax;
import ec.com.sidesoft.happypay.web.services.shppws_detailDue;

import org.openbravo.model.common.businesspartner.BusinessPartner;

public class equifax implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String netbyShopGroup = request.getParameter("shop_group");
		String netbyIdentifier = request.getParameter("identifier");
	    //Double age = new Double(ageString);
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		
	    OBCriteria<BusinessPartner> query = OBDal.getInstance().createCriteria(BusinessPartner.class);
	    List<BusinessPartner> businessPartners = query.list();
	    
	    OBCriteria<SweqxEquifax> query2 = OBDal.getInstance().createCriteria(SweqxEquifax.class);
	    List<SweqxEquifax> equifaxs = query2.list();
	    
	    OBCriteria<shppws_detailDue> query3 = OBDal.getInstance().createCriteria(shppws_detailDue.class);
	    List<shppws_detailDue> detailDue = query3.list();
	    
	    
	    if (netbyIdentifier!=null) {
	        List<Map<String, Object>> records = new ArrayList<>();
	        
	        // Realiza la consulta a la API y obtiene el JSON
	        String apiResponse = getApiResponse(accesApi, netbyIdentifier);
	        
	        //Ahora se procesa
	        //procesarApiResponse(apiResponse, records, businessPartners, netbyShopGroup);
	        
	        //Finalmente devuelvo
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        JSONArray jsonArray = new JSONArray();
	        for (Map<String, Object> record : records) {
	            JSONObject jsonObject = new JSONObject();
	            for (Map.Entry<String, Object> entry : record.entrySet()) {
	                jsonObject.put(entry.getKey(), entry.getValue());
	            }
	            jsonArray.put(jsonObject);
	        }

	        String json = jsonArray.toString();

	        PrintWriter writer = response.getWriter();
	        writer.write(json);
	        writer.close();

	    }
	}

	
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public String getApiResponse(shppws_config accesApi, String Identifier) throws Exception {
	    String apiUrl=accesApi.getRCNamespace();
	    String apiEndPoint=accesApi.getRCReadEndpoint();
	    String apiTypeAuth=accesApi.getRCTypeAuth();
	    String apiUser=accesApi.getRCUser();
	    String apiPass=accesApi.getRCPass();
	    String apiToken=accesApi.getRCToken();
	    int responseCode = 500;
	    HttpURLConnection connectionhttp=null;
	    HttpsURLConnection connectionhttps=null;
	    HttpURLConnection connection = null;

	    //BA -> Basic auth
	    //TA -> Token auth
			    if (apiTypeAuth.equals("BA")) {
					    	URL url = new URL(apiUrl + apiEndPoint);
						     connectionhttp = (HttpURLConnection) url.openConnection();
						     connectionhttp.setRequestMethod("GET");
						    String username = apiUser;
						    String password = apiPass;
						    String authString = username + ":" + password;
						    String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
						    connectionhttp.setRequestProperty("Authorization", authHeaderValue);
						    
				 // Obtiene la respuesta de la API
				     responseCode = connectionhttp.getResponseCode();
				     connection = connectionhttp;
			    }else if (apiTypeAuth.equals("AT")) {
					    	// Deshabilitar la validación de certificados SSL
						    TrustManager[] trustAllCerts = new TrustManager[] {
						        new X509TrustManager() {
						            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						                return null;
						            }
						            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
						            }
						            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
						            }
						        }
						    };
				
						     // Configurar SSLContext con la configuración personalizada
						     SSLContext sslContext = SSLContext.getInstance("TLS");
						     sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
						     // Obtener la conexión HTTPS y aplicar la configuración personalizada
						     URL url = new URL(apiUrl + apiEndPoint);
						     connectionhttps = (HttpsURLConnection) url.openConnection();
						     // Desactivar la verificación estricta del nombre del host
						     connectionhttps.setHostnameVerifier((hostname, session) -> true);
						     connectionhttps.setSSLSocketFactory(sslContext.getSocketFactory());
						     connectionhttps.setRequestMethod("POST");
					    	
					    	
					    	JSONObject requestBody = new JSONObject();
					    	requestBody.put("ci", Identifier);
					    	requestBody.put("apikey", apiToken);
					    	connectionhttps.setRequestProperty("Content-Type", "application/json");
				
					    	connectionhttps.setDoOutput(true);
					    	OutputStreamWriter writer = new OutputStreamWriter(connectionhttps.getOutputStream());
					    	writer.write(requestBody.toString());
					    	writer.flush();
					    	writer.close();
			    	
			    	// Obtiene la respuesta de la API
				     responseCode = connectionhttps.getResponseCode();
				     connection = connectionhttps;
			    }
	    
	    if (responseCode == HttpURLConnection.HTTP_OK) {
	        // S lee la respuesta de la API
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        StringBuilder responseBuilder = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            responseBuilder.append(line);
	        }
	        reader.close();

	        // Se retorna el string de la api
	        return responseBuilder.toString();
	    } else {
	        throw new Exception("Error en la consulta a la API. Código de respuesta: " + responseCode );
	    }
	}
	
	public void procesarApiResponse(String apiResponse, List<Map<String, Object>> records, List<shppev_age> reasonAges, String netbyShopGroup) throws JSONException {
		// Converts the JSON string to a JSONObject
	    JSONObject jsonResponse = new JSONObject(apiResponse);

	    // Access the "persona" field
	    JSONObject persona = jsonResponse.getJSONObject("persona");

	    // Access the "datos" field
	    JSONObject datos = persona.getJSONObject("datos");

	    // Access the individual fields
	    String identifier = datos.getString("cedula");
	    String entityName = datos.getString("nombres");
	    String birthDate = datos.getString("fecha_nacimiento");
	    
			    // Obtén la fecha actual
			    LocalDate fechaActual = LocalDate.now();
		
			    // Obtén la fecha de nacimiento en formato de texto
			    String fechaNacimientoTexto = "5/2/1999";
		
			    // Define el formato de la fecha
			    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		
			    // Convierte la fecha de nacimiento en LocalDate
			    LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoTexto, formatter);
		
			    // Calcula la diferencia entre la fecha de nacimiento y la fecha actual
			    Period diferencia = Period.between(fechaNacimiento, fechaActual);
		
			    // Calcula la edad en años con parte decimal para meses y días
			    double auxAge = diferencia.getYears() + ((double) diferencia.getMonths() / 12) + ((double) diferencia.getDays() / 365);
			    BigDecimal auxAgeBigDecimal = new BigDecimal(auxAge);


	    // Se crea un mapa para procesar los datos
	    Map<String, Object> recordMap = new HashMap<>();
	    recordMap.put("identifier", identifier);
	    recordMap.put("entityName", entityName);
	    recordMap.put("birthdate", birthDate);
	        
	     // Comparación de campos "shop_group" y "id"
	        String shopGroup = netbyShopGroup;
	        for (shppev_age reasonAge : reasonAges) {
	            if (shopGroup.equals(reasonAge.getShopGroup())) {
	                BigDecimal ageInitial = reasonAge.getInitialAge();
	                BigDecimal ageFinal = reasonAge.getFinalAge();
	                String answer = reasonAge.getAnswer();
	                if (auxAgeBigDecimal.compareTo(ageInitial) > 0 && auxAgeBigDecimal.compareTo(ageFinal) < 0) {
	                	if(answer.equals("C")) {
	                		recordMap.put("message", "Continua");
		                    recordMap.put("age", auxAge);
	                	}else {
	                		recordMap.put("message", "R");
		                    recordMap.put("age", auxAge);
	                	}
	                    
	                }else {
	                	recordMap.put("message", "R");
	                }
	                break; // Si se encuentra una coincidencia, se detiene la iteración
	            }
	        }
	        
	        records.add(recordMap);
	    
	    
	 
	}



}
