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
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import it.openia.crm.Opcrmopportunities;

import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

public class verification_check implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "verification_check", requestParameters);
		
		String No_Opportunity = request.getParameter("No_Opportunity");
		JSONArray records = new JSONArray();
		JSONObject record = new JSONObject();
		try {
			OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance().createCriteria(Opcrmopportunities.class);
	    	queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
	    	List<Opcrmopportunities> listobjOpportunity = queryOpportunity.list();
	    	Opcrmopportunities objOpportunity =listobjOpportunity.get(0);
	    	
	    	OBCriteria<ECSFQ_Quotation> queryobjFastQuotation = OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
	    	queryobjFastQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_OPCRMOPPORTUNITIES, objOpportunity));
	    	List<ECSFQ_Quotation> listFastQuotation = queryobjFastQuotation.list();
	    	ECSFQ_Quotation objFastQuotation=listFastQuotation.get(0);
	    	
	    	OBCriteria<SscfCreditOperation> querySscfCreditOperation = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
			querySscfCreditOperation.add(Restrictions.eq(SscfCreditOperation.PROPERTY_SSCORORDER,objFastQuotation));
			List<SscfCreditOperation> listobjCreditOperation = querySscfCreditOperation.list();
			SscfCreditOperation objCreditOperation =listobjCreditOperation.get(0);
			String docStatus = objCreditOperation.getDocumentStatus();
			//record.put("docStatus1", docStatus);
			    if(docStatus.equals("IP")) {
			    	docStatus = "En proceso";
			    }else if(docStatus.equals("A")) {
			    	docStatus = "Aprobado";
			    }else if(docStatus.equals("C")) {
			    	docStatus = "Retornar";
			    }else if(docStatus.equals("R")) {
			    	docStatus = "Rechazado";
			    }
			    
				record.put("No_Opportunity", No_Opportunity);
			    record.put("Status", docStatus);
			    record.put("message", objCreditOperation.getShppwsObservation());
			        records.put(record);
		}catch(Exception e) {
			record.put("No_Opportunity", No_Opportunity);
		    record.put("Status", "Error");
		    record.put("message", "Error");
		    		records.put(record);
		    		Error = e.getMessage();
		}
		
		        
	    
		        
			// |||||||||||||||||||||||||||||||||||//
			// |||||||||||||RESULTADO|||||||||||||//
			// |||||||||||||||||||||||||||||||||||//
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			String json = records.toString();
			PrintWriter writer = response.getWriter();
			writer.write(json);
			writer.close();
			
			String requestUrl = request.getRequestURL().toString();
			
			try {
				String noReference = No_Opportunity;
		        String Interface = "SHPPWS_NT";
			    String Process = "Estado de Verificación";
			    String idRegister = "";
				if(Error.equals("")) {
					logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
				}else {
					logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "OUT", Interface, Process, idRegister, Error);
				}
	        	
			    
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
