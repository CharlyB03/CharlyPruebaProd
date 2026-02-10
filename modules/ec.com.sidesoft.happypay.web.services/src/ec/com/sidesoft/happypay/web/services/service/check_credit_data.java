package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
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
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;
import java.text.SimpleDateFormat;

public class check_credit_data implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
	    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
	    log_records logger = new log_records();
	    String requestParameters =  request.getQueryString();
	    Scactu_Log log = logger.log_start_register(accesApi, "check_credit_data", requestParameters);
		String No_Opportunity = request.getParameter("No_Opportunity");
		//String Next_Quota_Date = request.getParameter("Next_Quota_Date");
		//String Amount_financed = request.getParameter("Amount_financed");
		//String Happy_Package = request.getParameter("Happy_Package");
		//String Total_financed = request.getParameter("Total_financed");
		//String Fee_Value = request.getParameter("Fee_Value");
		
		JSONObject records = new JSONObject();
        JSONArray Quotas = new JSONArray();
        
        records.put("No_Opportunity", No_Opportunity);
        try {//Operación de credito
        	OBCriteria<SscfCreditOperation> querySscfCreditOperation = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
			querySscfCreditOperation.add(Restrictions.eq(SscfCreditOperation.PROPERTY_DOCUMENTNO, No_Opportunity));
	    	List<SscfCreditOperation> listCreditOperation = querySscfCreditOperation.list();
	    	SscfCreditOperation objCreditOperation =listCreditOperation.get(0);
	    	BigDecimal Amount_financed = objCreditOperation.getFinancedValue();//valor financiado
	    	BigDecimal Value_To_Invoice = objCreditOperation.getValueInvoice();//total a financiar
	    	
        	
			OBCriteria<Invoice> queryInvoice = OBDal.getInstance().createCriteria(Invoice.class);
			queryInvoice.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO, No_Opportunity));
	    	List<Invoice> listInvoice = queryInvoice.list();
	    	Invoice objInvoice =listInvoice.get(0);
	    	Date Next_Quota_Date = objInvoice.getShpicProxdues();//siguiente cuota
	    	BigDecimal Fee_Value = objInvoice.getShpicFinancivaluedues();//valor cuota
	    	
	    	OBCriteria<FIN_PaymentSchedule> queryPaymentPlan = OBDal.getInstance().createCriteria(FIN_PaymentSchedule.class);
			queryPaymentPlan.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, objInvoice));
			List<FIN_PaymentSchedule> listAmortization = queryPaymentPlan.list();
			BigDecimal Assistance = BigDecimal.ZERO;
			BigDecimal Service = BigDecimal.ZERO;
			BigDecimal Happy_Package = BigDecimal.ZERO;
			
			for(FIN_PaymentSchedule objAmortization: listAmortization) {
				Assistance=Assistance.add(objAmortization.getShppsAttendance());
				Service=Service.add(objAmortization.getShppsService());
			}
			Happy_Package = Assistance.add(Service);
		    	
		    	records.put("Next_Quota_Date", Next_Quota_Date);
		    	records.put("Amount_financed", Amount_financed);
		    	records.put("Happy_Package", Happy_Package);
		    	records.put("Total_financed", Value_To_Invoice);
		    	records.put("Fee_Value", Fee_Value);
		    	
		}catch(Exception e) {
			Error = e.getMessage();
		}
        
        Opcrmopportunities objOpportunity = new Opcrmopportunities();
		try {//Oportunidad
			OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance().createCriteria(Opcrmopportunities.class);
	    	queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
	    	List<Opcrmopportunities> listobjOpportunity = queryOpportunity.list();
	    	objOpportunity =listobjOpportunity.get(0);
		    
	    	OBCriteria<ECSFQ_Quotation> queryobjFastQuotation = OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
	    	queryobjFastQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_OPCRMOPPORTUNITIES, objOpportunity));
	    	List<ECSFQ_Quotation> listFastQuotation = queryobjFastQuotation.list();
	    	ECSFQ_Quotation objFastQuotation=listFastQuotation.get(0);
	    	
	    	OBCriteria<EcsfqAmortization> queryobjAmortization = OBDal.getInstance().createCriteria(EcsfqAmortization.class);
	    	queryobjAmortization.add(Restrictions.eq(EcsfqAmortization.PROPERTY_ECSFQORDER, objFastQuotation));
	    	List<EcsfqAmortization> listobjAmortization = queryobjAmortization.list();
			
		    	if(listobjAmortization.size() > 0) {
		    		for(EcsfqAmortization objAmortization:listobjAmortization) {
		    			JSONObject objquota = new JSONObject();
		    			objquota.put("No. Quota", objAmortization.getNROCuota());
		    			objquota.put("Expiration_date", objAmortization.getShppwsPaymentDate());
		    			objquota.put("Quota_value", objAmortization.getAmount());
				        Quotas.put(objquota);
		    		}
		    	}
		    	
		}catch(Exception e) {
			Error = e.getMessage();}
		
		records.put("Quota", Quotas);
	        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(records);
        String json = jsonArray.getJSONObject(0).toString();
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.close();
        
        String requestUrl = request.getRequestURL().toString();
        
        try {
        	String noReference = No_Opportunity;
	        String Interface = "SHPPWS_NT";
		    String Process = "Consulta datos de crédito";
		    String idRegister = objOpportunity.getId();
        logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
        }catch(Exception e) {
        	String noReference = No_Opportunity;
	        String Interface = "SHPPWS_NT";
		    String Process = "Consulta datos de crédito";
		    String idRegister = "";
        logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "OUT", Interface, Process, idRegister, Error);
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
