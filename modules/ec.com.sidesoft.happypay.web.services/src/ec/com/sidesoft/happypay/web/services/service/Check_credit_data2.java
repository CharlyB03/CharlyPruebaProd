package ec.com.sidesoft.happypay.web.services.service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.service.web.WebService;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.fast.quotation.EcsfqAmortization;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import it.openia.crm.Opcrmopportunities;
import org.hibernate.criterion.Order;

public class Check_credit_data2 implements WebService{

	@Override
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
	    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
	    log_records logger = new log_records();
	    String requestParameters =  request.getQueryString();
	    Scactu_Log log = logger.log_start_register(accesApi, "check_credit_data2", requestParameters);
		String Identifier = request.getParameter("Cedula");
		
		
		JSONObject record = new JSONObject();
        JSONArray records = new JSONArray();
        String idRegister = "";
        
        String codeResponse = "";
        String partnerName = "";
        BigDecimal amount = BigDecimal.ZERO;
        //01 -> CONSULTA EXITOSA
        //04 -> NO EXISTE CLIENTE
        
        String message = "NO EXISTE CLIENTE";
        try {
        	
        	OBCriteria<BusinessPartner> queryPartner = OBDal.getInstance().createCriteria(BusinessPartner.class);
        	queryPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, Identifier)); //CI
	    	List<BusinessPartner> listPartner = queryPartner.list();
	    	
	    	if(listPartner.size() > 0) {
	    		OBCriteria<Invoice> queryInvoice = OBDal.getInstance().createCriteria(Invoice.class);
				queryInvoice.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, listPartner.get(0))); //CI
				queryInvoice.add(Restrictions.gt(Invoice.PROPERTY_OUTSTANDINGAMOUNT, BigDecimal.ZERO)); // > 0
				queryInvoice.addOrder(Order.asc(Invoice.PROPERTY_INVOICEDATE));//Order date
		    	List<Invoice> listInvoice = queryInvoice.list();
		    	
		    	List<Invoice> listValidatorInvoice = new ArrayList<Invoice>();
		    	
		    	for(Invoice objInvoice : listInvoice) {
		    		DocumentType objTypeDoc = objInvoice.getDocumentType();
	        		if(objTypeDoc != null && objTypeDoc.getDocumentCategory().equals("ARI") && objTypeDoc.isShpfrIscreditope() && objTypeDoc.getGLCategory() != null && objTypeDoc.getGLCategory().getName().equals("AR Invoice")) {
	        			String currentDocNo = objInvoice.getDocumentNo();
	        			listValidatorInvoice.add(objInvoice);
	        			break;
	        		}
		    	}
		    	
		    	if(listValidatorInvoice.size() > 0) {
		    		Invoice objInvoice = listValidatorInvoice.get(0);
		    		BusinessPartner ObjPartner = objInvoice.getBusinessPartner() != null &&  objInvoice.getBusinessPartner().isActive() && objInvoice.getBusinessPartner().getSearchKey() != null && objInvoice.getBusinessPartner().getSearchKey().equals(Identifier) ? objInvoice.getBusinessPartner() : null;
		    		if(objInvoice != null & ObjPartner != null) {
		    			BigDecimal pendingAmount = objInvoice.getShpicMostoverInstall();
		    			BigDecimal advanceAmount = objInvoice.getShpicAdvancevalue();
		    			amount = pendingAmount.subtract(advanceAmount);
		    			idRegister = objInvoice.getDocumentNo();
		    			partnerName = ObjPartner.getName()+"";
		    			codeResponse = "01";
		    			message = "CONSULTA EXITOSA";
		    		}else {
		    			codeResponse = "04";
		    		}
		    	}else {
		    		codeResponse = "04";
		    	}
	    	}else {
	    		codeResponse = "04";
	    	}
	    	
        	
		}catch(Exception e) {
			Error = e.getMessage();
			codeResponse = "04";
		}
        
        record.put("Codigo_respuesta", codeResponse);
        record.put("Cedula", Identifier);
        record.put("Nombres", partnerName);
        record.put("monto", amount);
        record.put("Mensaje", message);
        
	        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = record.toString();
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.close();
        
        String requestUrl = request.getRequestURL().toString();
        
        
        try {
        	String noReference = Identifier;
	        String Interface = "SHPPWS_NT";
		    String Process = "Consulta datos de crédito 2";
        logger.log_end_register(log, requestUrl, noReference, json, "OK", "IN", Interface, Process, idRegister, Error);
        }catch(Exception e) {
        	String noReference = Identifier+" unknow";
	        String Interface = "SHPPWS_NT";
		    String Process = "Consulta datos de crédito 2";
		    idRegister = "";
        logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "IN", Interface, Process, idRegister, Error);
        }
		
	}

	@Override
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
