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
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
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
import ec.com.sidesoft.fast.quotation.EcsfqAmortization;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import it.openia.crm.Opcrmopportunities;

import java.util.Base64;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

public class CreditOperationByEmail implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "Credit_Operation", requestParameters);
		
		String email = request.getParameter("email");
		String Message="";
			
		JSONArray arrayParentOperation = new JSONArray();
		List<Invoice> listCredits = new ArrayList<>();
		
		try{
			final OBCriteria<DocumentType> queryDocumenType= OBDal.getInstance().createCriteria(DocumentType.class);//Tipo de Documento
		    queryDocumenType.add(Restrictions.eq(DocumentType.PROPERTY_SHPFRISCREDITOPE,true));//Es operación de crédito
		    List<DocumentType> listDocType = queryDocumenType.list();
		    String[] docTypeIds = new String[listDocType.size()]; // tipos de documentos necesarios
		    int index = 0;
		    for (DocumentType docType : listDocType) {
		        docTypeIds[index] = docType.getId();
		        index++;
		    }
		    
		    // Get business partner for email
			OBCriteria<BusinessPartner> partner = OBDal.getInstance().createCriteria(BusinessPartner.class);
			partner.add(Restrictions.eq(BusinessPartner.PROPERTY_EEIEMAIL, email));
			List<BusinessPartner> listPartner = partner.list();
			String[] partnerIds = new String[listPartner.size()]; 
			int indx = 0;
		    for (BusinessPartner prt : listPartner) {
		    	partnerIds[indx] = prt.getId();
		        indx++;
		    }
		    
			OBCriteria<Invoice> queryCredit = OBDal.getInstance().createCriteria(Invoice.class);
			if (partnerIds.length > 0 && docTypeIds.length > 0) {
				queryCredit.add(Restrictions.in(Invoice.PROPERTY_BUSINESSPARTNER + ".id", partnerIds));
				queryCredit.add(Restrictions.in(Invoice.PROPERTY_TRANSACTIONDOCUMENT + ".id", docTypeIds));
				listCredits = queryCredit.list();
			} else {
		        // Si no hay partner o tipo documento, retornar el JSON con noCredit: 0
		        JSONObject objParentOperationNull = new JSONObject();
		        objParentOperationNull.put("email", email);
		        objParentOperationNull.put("noCredit", 0);
		        arrayParentOperation.put(objParentOperationNull);
			 }
			
			if(listCredits.size()>0) {
	    		for(Invoice objInvoice:listCredits) {
	    			JSONObject objParentOperation = new JSONObject();

	    			String TypeStatus = objInvoice.getShpicOperationState();
	    			if (TypeStatus != null) {
	    				if(TypeStatus.equals("01")) {
	    					TypeStatus="ANULADO";
	    				}else if(TypeStatus.equals("02")) {
	    					TypeStatus="VIGENTE";
	    				}else if(TypeStatus.equals("03")) {
	    					TypeStatus="VENCIDO";
	    				}else if(TypeStatus.equals("04")) {
	    					TypeStatus="CASTIGADO";
	    				}else if(TypeStatus.equals("05")) {
	    					TypeStatus="CANCELADO";
	    				}else {
	    					TypeStatus="DESCONOCIDO";
	    				}
	    			}
	    			
	    			objParentOperation.put("email", email);
	    			objParentOperation.put("noCredit", objInvoice.getDocumentNo());
	    			objParentOperation.put("identifier", objInvoice.getBusinessPartner().getTaxID());
	    			objParentOperation.put("email", objInvoice.getBusinessPartner().getEEIEmail());
	    			BigDecimal grandTotal = objInvoice.getGrandTotalAmount();
	    			if(grandTotal == null) {grandTotal = BigDecimal.ZERO;}
	    			BigDecimal roundedTotal = grandTotal.setScale(2, RoundingMode.HALF_UP);
	    			objParentOperation.put("creditAmount", roundedTotal);
	    			objParentOperation.put("statusOperation", TypeStatus); 
	    			arrayParentOperation.put(objParentOperation);
	    		}
			}			
		}
		catch(Exception e) {
			JSONObject objParentOperationNull = new JSONObject();
			objParentOperationNull.put("email", email);
			objParentOperationNull.put("noCredit", 0);
			Error = Error + e.getMessage();
			arrayParentOperation.put(objParentOperationNull);
		}
					
		//Finalmente devuelvo
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
			
		String json = arrayParentOperation.toString();
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();

		String requestUrl = request.getRequestURL().toString();
		
		try {
	        	String noReference = email;
		        String Interface = "SHPPWS_NT";
			    String Process = "Operación Crédito - Email";
			    String idRegister = null;
			    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
			}catch(Exception e) {
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
	
}
