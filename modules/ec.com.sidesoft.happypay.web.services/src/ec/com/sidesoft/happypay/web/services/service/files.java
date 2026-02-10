package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.web.WebService;

import com.google.gson.JsonObject;

import org.hibernate.criterion.Restrictions;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.shppws_files;

import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

import java.util.HashSet;
import java.util.Set;

public class files implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String No_Opportunity = request.getParameter("No_Opportunity");
		
	}

	
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		
		//Validar duplicidad
		Set<String> urlsSet = new HashSet<>();
		String Message="";
		
		
	        // Get cuerpo de la solicitud
	        StringBuilder requestBody = new StringBuilder();
	        try (BufferedReader reader = request.getReader()) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                requestBody.append(line);
	            }
	        }
	
	        // JSON
	        JSONObject requestJSON = new JSONObject(requestBody.toString());
	        String No_Opportunity = requestJSON.getString("No_Opportunity");
	        JSONArray filesArray = requestJSON.getJSONArray("files");
	        
	        String requestParameters =  requestJSON.toString();
	        Scactu_Log log = logger.log_start_register(accesApi, "files", requestParameters);
	
	        // Recorre el JSON and verification
	        JSONArray files = new JSONArray();
	        for (int i = 0; i < filesArray.length(); i++) {
	            JSONObject file = filesArray.getJSONObject(i);
	            String name = file.getString("Name_file");
	            String url = file.getString("Url_file");
		            if (urlsSet.contains(url)) {
		            	Message="Archivos duplicados";
		                break;
		            }
		            urlsSet.add(url);
	            JSONObject objfile = new JSONObject();
	            objfile.put("Name", name);
	            objfile.put("URL", url);
	            files.put(objfile);
	        }
	        
	        //Save and return
	        JSONArray arrayParentFiles = new JSONArray();
				JSONObject objParentFiles = new JSONObject();
		        objParentFiles.put("No_Opportunity", No_Opportunity);
		        if(Message.equals("") && files.length() > 0) {
		        	try {
			        	for (int i = 0; i < filesArray.length(); i++) {
							JSONObject file = filesArray.getJSONObject(i);
							String name = file.getString("Name_file");
							String url = file.getString("Url_file");
							OBCriteria<Invoice> queryobjInvoice = OBDal.getInstance().createCriteria(Invoice.class);
							queryobjInvoice.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO, No_Opportunity));
							List<Invoice> listobjInvoice = queryobjInvoice.list();
							Invoice objInvoice = listobjInvoice.get(0);
							shppws_files newfile = OBProvider.getInstance().get(shppws_files.class);
							newfile.setInvoice(objInvoice);
							newfile.setClient(accesApi.getClient());
							newfile.setOrganization(accesApi.getOrganization());
							newfile.setNamefile(name);
							newfile.setLinkfile(url);
							OBDal.getInstance().save(newfile);
							OBDal.getInstance().flush();
							
				        }
			        	objParentFiles.put("Message", "Ok");
		        	}catch(Exception e) {
		        		objParentFiles.put("Message", "Error");
		        		Error = e.getMessage();
		        	}
		        	
		        }else {
		        	objParentFiles.put("Message", "Ningún archivo encontrado o están duplicados");
		        }
	        
	        arrayParentFiles.put(objParentFiles);

        
		//Finalmente devuelvo
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json = arrayParentFiles.toString();
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
				
		String requestUrl = request.getRequestURL().toString();
		
		
		try {
        	String noReference = No_Opportunity;
	        String Interface = "SHPPWS_NT";
		    String Process = "Ajuntar documentos";
		    String idRegister = "";
		    if(Error.equals("")) {
			    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
		    }else {
			    logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "OUT", Interface, Process, idRegister, Error);
		    }
        }catch(Exception e){}
	}

	
	public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	



}
