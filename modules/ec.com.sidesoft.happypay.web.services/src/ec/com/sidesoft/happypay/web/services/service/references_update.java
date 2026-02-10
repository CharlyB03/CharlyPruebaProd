package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
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
import ec.com.sidesoft.credit.factory.SscfCom1;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.credit.factory.SscfPersonalReference;
import ec.com.sidesoft.credit.factory.maintenance.Profession;
import ec.com.sidesoft.credit.factory.maintenance.Relationship;
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

public class references_update implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		/*String No_Opportunity = request.getParameter("No_Opportunity");
		
		// Get cuerpo de la solicitud
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        JSONObject requestJSON = new JSONObject(requestBody.toString());*/
        
        

	    
	}

	
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		 String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		
		// TODO Auto-generated method stub
		StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        JSONObject requestJSON = new JSONObject(requestBody.toString());
        String requestParameters =  requestJSON.toString();
		Scactu_Log log = logger.log_start_register(accesApi, "references_update", requestParameters);
        
        String No_Opportunity = requestJSON.getString("No_Opportunity");
        JSONArray records = new JSONArray();
		JSONObject record = new JSONObject();
		SscfCreditOperation objCreditOperation = new SscfCreditOperation();
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
			objCreditOperation =listobjCreditOperation.get(0);
			
			OBCriteria<SscfCom1> queryCOM = OBDal.getInstance().createCriteria(SscfCom1.class);
			queryCOM.add(Restrictions.eq(SscfCom1.PROPERTY_SSCFCREDITOPERATION,objCreditOperation));
    		List<SscfCom1> listqueryCOM = queryCOM.list();
    		SscfCom1 objComS =listqueryCOM.get(0);
			
    		OBCriteria<SscfPersonalReference> queryExistRefer = OBDal.getInstance().createCriteria(SscfPersonalReference.class);
    		queryExistRefer.add(Restrictions.eq(SscfPersonalReference.PROPERTY_SSCFCOM1,objComS));
    		List<SscfPersonalReference> listExistRefer = queryExistRefer.list();
    		
    		for (SscfPersonalReference objExistRef : listExistRefer) {
    		    OBDal.getInstance().remove(objExistRef);
    		}
    		OBDal.getInstance().flush();
        	
    	    JSONArray referencesArray = requestJSON.getJSONArray("personal_reference_arrangement");
            // Recorre el Array del JSON
            Map<String, Object> listMessage = new HashMap<>();
            for (int i = 0; i < referencesArray.length(); i++) {
                JSONObject newRef = referencesArray.getJSONObject(i);
                String Ref_Surnames = newRef.getString("Surname");
                String Ref_Names = newRef.getString("Name");
                String Ref_RelationShip = newRef.getString("RelationShip");
                String Ref_CellPhone = newRef.getString("Cell_Phone");
                SscfPersonalReference objPersonalReference = OBProvider.getInstance().get(SscfPersonalReference.class);
                updatePersonalReference( objPersonalReference, accesApi, objComS, Ref_Surnames, Ref_Names, Ref_RelationShip, Ref_CellPhone, listMessage);
            }
            
            objCreditOperation.setCom1Status("G");
            objCreditOperation.setCom2Status("G");
            objCreditOperation.setSComStatus("G");
            objCreditOperation.setCallCenterStatus("O");
            objCreditOperation.setDocumentStatus("IP");
            OBDal.getInstance().save(objCreditOperation);
			OBDal.getInstance().flush();
            
    		    record.put("No_Opportunity", No_Opportunity);
    		    record.put("message", "Actualizado");
    		        records.put(record);
        }catch(Exception e) {
    		    record.put("No_Opportunity", No_Opportunity);
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
		    String Process = "Actualizar Referencias";
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
	
	
	public void updatePersonalReference(SscfPersonalReference objPersonalReference, shppws_config accesApi, SscfCom1 objComS, String Ref_Surnames,String Ref_Names, String Ref_RelationShip, String Ref_CellPhone, Map<String, Object> listMessage) {
		String message6="";
		try {
			objPersonalReference.setClient(accesApi.getClient());
	  		message6+=" setClient:"+accesApi.getClient().getId();
	  		objPersonalReference.setOrganization(accesApi.getOrganization());
	  		message6+=" setOrganization:"+accesApi.getOrganization().getId();
	  		objPersonalReference.setActive(accesApi.isActive());
	  		message6+=" setActive:"+accesApi.isActive().toString();
	  		objPersonalReference.setCreatedBy(accesApi.getCreatedBy());
	  		message6+=" setCreatedBy:"+accesApi.getCreatedBy().getId();
	  		objPersonalReference.setUpdatedBy(accesApi.getUpdatedBy());
	  		message6+=" setUpdatedBy:"+accesApi.getUpdatedBy().getId();
	  		objPersonalReference.setSscfCom1(objComS);
	  		message6+=" setSscfCom1:"+objComS.getId();
	  		
	  		objPersonalReference.setNames(Ref_Surnames+" "+Ref_Names);
	  		objPersonalReference.setAddress(".");
	  		objPersonalReference.setJob(".");
	  		objPersonalReference.setJOBAddress(".");
	  		objPersonalReference.setPhone(Ref_CellPhone);
	  		
	  		OBCriteria<Relationship> queryRelationship= OBDal.getInstance().createCriteria(Relationship.class);
	  		queryRelationship.add(Restrictions.eq(Relationship.PROPERTY_NAME, Ref_RelationShip));
	  		List<Relationship> listRelationship = queryRelationship.list();
	  		
	  		if(listRelationship.size() <= 0) {
	  			Relationship objRelationship = OBProvider.getInstance().get(Relationship.class);
	  			objRelationship.setClient(accesApi.getClient());
	  			objRelationship.setOrganization(accesApi.getOrganization());
	  			objRelationship.setActive(accesApi.isActive());
	  			objRelationship.setCreatedBy(accesApi.getCreatedBy());
	  			objRelationship.setUpdatedBy(accesApi.getUpdatedBy());
	  			objRelationship.setValue(Ref_RelationShip);
	  			objRelationship.setName(Ref_RelationShip);
				OBDal.getInstance().save(objRelationship);
				OBDal.getInstance().flush();
				objPersonalReference.setRelationship(objRelationship);
	  		}else {
	  			Relationship objRelationship = listRelationship.get(0);
	  			objPersonalReference.setRelationship(objRelationship);
	  		}
	  		
	  	
	  		OBDal.getInstance().save(objPersonalReference);
			OBDal.getInstance().flush();
			message6=" SI Actualiza Referencias"+"-----"+ objPersonalReference.getId();
			listMessage.put("message", message6);
		}catch(Exception e) {
			message6+=" NO Actualiza Referencias"+"-----"+ e.getMessage();
	  		listMessage.put("message", message6);
		}
	}


}
