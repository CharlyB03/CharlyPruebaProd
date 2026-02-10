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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

public class Trade_Creation implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "Trade_Creation", requestParameters);
		
		String Id_Commerce = request.getParameter("Id_Commerce");
		String Business_Tax_Name = request.getParameter("Business_Tax_Name");
		String Type_Identification = request.getParameter("Type_Identification");
		String Type_Taxpayer = request.getParameter("Type_Taxpayer");
		String Address_Line_1 = request.getParameter("Address_Line_1");
		String Address_Line_2 = request.getParameter("Address_Line_2");
		String City = request.getParameter("City");
		String Province = request.getParameter("Province");
		String Country = request.getParameter("Country");
		String Phone = request.getParameter("Phone");
		String Contact_Name = request.getParameter("Contact_Name");
		String Contact_Mail = request.getParameter("Contact_Mail");
		String Contact_Position = request.getParameter("Contact_Position");
		String Contact_Cell = request.getParameter("Contact_Cell");
		String Bank_Account = request.getParameter("Bank_Account");
		String Bank_Transfer = request.getParameter("Bank_Transfer");
		String Account_Type = request.getParameter("Account_Type");
		String Beneficiary = request.getParameter("Beneficiary");
		String Beneficiary_ID = request.getParameter("Beneficiary_ID");
		
		
		String Message="";
		
	        JSONArray arrayTradeCreation = new JSONArray();
				JSONObject objTradeCreation = new JSONObject();
				
				if(!Id_Commerce.isEmpty()) {
					objTradeCreation.put("Id_Commerce", Id_Commerce);
					objTradeCreation.put("Message", "Ok");
					arrayTradeCreation.put(objTradeCreation);
				}else {
					objTradeCreation.put("Id_Commerce", Id_Commerce);
					objTradeCreation.put("Message", "Error");
					arrayTradeCreation.put(objTradeCreation);
				}
					
		//Finalmente devuelvo
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json = arrayTradeCreation.toString();
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
	    
		String requestUrl = request.getRequestURL().toString();
		
		try {
        	String noReference = Id_Commerce;
	        String Interface = "SHPPWS_NT";
		    String Process = "Creación Comercio";
		    String idRegister = "";
		    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
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
