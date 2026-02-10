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

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

public class mcsimulation implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "mcsimulation", requestParameters);
		
		String Interface = request.getParameter("Interface");
		String Channel = request.getParameter("Channel");
		String Trade_Code = request.getParameter("Trade_Code");
		String Code_Agency = request.getParameter("Code_Agency");
		String Product_Code = request.getParameter("Product_Code");
		String Code_Product = request.getParameter("Code_Product");
		String Identifier = request.getParameter("Identifier");
		String Cell_Phone = request.getParameter("Cell_Phone");
		String Mail = request.getParameter("Mail");
		String Amount_Finance = request.getParameter("Amount_Finance");
		String Input_Value = request.getParameter("Input_Value");
		String User = request.getParameter("User");
		String No_Opportunity = request.getParameter("No_Opportunity");
		
	    
	        List<Map<String, Object>> records = new ArrayList<>();
	        // Subproductos Prod 1
	        JSONArray subproducts1 = new JSONArray();

				        // Subproducto 1
				        JSONObject subproduct1_1 = new JSONObject();
				        subproduct1_1.put("Name", "2 semanas");
				        subproduct1_1.put("Term_Code", "S001");
				        subproduct1_1.put("Term", "2");
				        subproduct1_1.put("Total_Due", "420");
				        subproduct1_1.put("Quota_Value", "210");
				        subproducts1.put(subproduct1_1);
			
				        // Subproducto 2
				        JSONObject subproduct1_2 = new JSONObject();
				        subproduct1_2.put("Name", "4 semanas");
				        subproduct1_2.put("Term_Code", "S002");
				        subproduct1_2.put("Term", "4");
				        subproduct1_2.put("Total_Due", "800");
				        subproduct1_2.put("Quota_Value", "200");
				        subproducts1.put(subproduct1_2);
			
				        // Subproducto 3
				        JSONObject subproduct1_3 = new JSONObject();
				        subproduct1_3.put("Name", "16 semanas");
				        subproduct1_3.put("Term_Code", "S003");
				        subproduct1_3.put("Term", "16");
				        subproduct1_3.put("Total_Due", "1600");
				        subproduct1_3.put("Quota_Value", "100");
				        subproducts1.put(subproduct1_3);
				        
			Map<String, Object> product1 = new HashMap<>();
			product1.put("Product", "001");
	        product1.put("Subproducts", subproducts1);
	        records.add(product1);

	        // Subproductos Prod 2
			   JSONArray subproducts2 = new JSONArray();
			
				        // Subproducto 1
				        JSONObject subproduct2_1 = new JSONObject();
				        subproduct2_1.put("Name", "2 meses");
				        subproduct2_1.put("Term_Code", "E001");
				        subproduct2_1.put("Term", "2");
				        subproduct2_1.put("Total_Due", "500");
				        subproduct2_1.put("Quota_Value", "250");
				        subproducts2.put(subproduct2_1);
				        
			Map<String, Object> product2 = new HashMap<>();
			product2.put("Product", "002");	        
			product2.put("Subproducts", subproducts2);

	        records.add(product2);
		    
	        
	        //Finalmente devuelvo
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        JSONArray jsonArray = new JSONArray();
	        for (Map<String, Object> recordMap : records) {
	            JSONObject jsonObject = new JSONObject();
	            for (Map.Entry<String, Object> entry : recordMap.entrySet()) {
	                jsonObject.put(entry.getKey(), entry.getValue());
	            }
	            jsonArray.put(jsonObject);
	        }

	        String json = jsonArray.toString();

	        PrintWriter writer = response.getWriter();
	        writer.write(json);
	        writer.close();

	        String requestUrl = request.getRequestURL().toString();
	        
	        
	        try {
	        	String noReference = No_Opportunity;
		        String InterfaceLOG = "SHPPWS_NT";
			    String Process = "Simulacion";
			    String idRegister = "";
			    String Error = "";
			    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", InterfaceLOG, Process, idRegister, Error);
	        }catch(Exception e){}
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
	
	



}
