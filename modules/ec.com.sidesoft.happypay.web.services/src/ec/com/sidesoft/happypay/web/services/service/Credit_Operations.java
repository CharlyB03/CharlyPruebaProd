package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.web.WebService;
import org.hibernate.criterion.Order;
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
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.ws.equifax.SweqxEquifax;
import it.openia.crm.Opcrmopportunities;

import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

public class Credit_Operations implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "Credit_Operations", requestParameters);
		
		String Identifier = request.getParameter("Identifier");
		String Type = request.getParameter("Type");
		String Code_Commerce = request.getParameter("Code_Commerce");
		String Message="";
		
		JSONArray arrayParentOperations = new JSONArray();
		JSONObject objParentOperations = new JSONObject();
		
		if (Type != null) {
			if(Type.equals("ANULADO")) {
				Type="01";
			}else if(Type.equals("VIGENTE")) {
				Type="02";
			}else if(Type.equals("VENCIDO")) {
				Type="03";
			}else if(Type.equals("CASTIGADO")) {
				Type="04";
			}else if(Type.equals("CANCELADO")) {
				Type="05";
			}
		}
		String segmentacionValue="";
		List<Invoice> listCredits = new ArrayList<>();
		try {
			final OBCriteria<DocumentType> queryDocumenType= OBDal.getInstance().createCriteria(DocumentType.class);//Tipo de Documento
		    queryDocumenType.add(Restrictions.eq(DocumentType.PROPERTY_SHPFRISCREDITOPE,true));//Es operación de crédito
		    List<DocumentType> listDocType = queryDocumenType.list();
		    String[] docTypeIds = new String[listDocType.size()]; // tipos de documentos necesarios
		    int index = 0;
		    for (DocumentType docType : listDocType) {
		        docTypeIds[index] = docType.getId();
		        index++;
		    }
		    
		    
			OBCriteria<BusinessPartner> querypartner = OBDal.getInstance().createCriteria(BusinessPartner.class);
			querypartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, Identifier));
			List<BusinessPartner> listPartner = querypartner.list();
			BusinessPartner objPartner = listPartner.get(0);
			OBCriteria<Invoice> queryCredit = OBDal.getInstance().createCriteria(Invoice.class);
			queryCredit.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, objPartner));
			queryCredit.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION,true));
			
			OBCriteria<SweqxEquifax> queryEquifax = OBDal.getInstance().createCriteria(SweqxEquifax.class);
			queryEquifax.add(Restrictions.eq(SweqxEquifax.PROPERTY_BUSINESSPARTNER + ".id", objPartner.getId()));
			queryEquifax.addOrder(Order.desc(SweqxEquifax.PROPERTY_CREATIONDATE));
			queryEquifax.setMaxResults(1);
			List<SweqxEquifax> listEquifax = queryEquifax.list();
			if (listEquifax.isEmpty()) {
				segmentacionValue="";
			}else {
				SweqxEquifax objEquifax = listEquifax.get(0);
				segmentacionValue=objEquifax.getSegmentation();
			}
			
			if (Type != null) {
				queryCredit.add(Restrictions.eq(Invoice.PROPERTY_SHPICOPERATIONSTATE, Type));
			}
			if (Code_Commerce != null && !(Code_Commerce.equals(""))) {
				OBCriteria<BusinessPartner> querypartnerCommerce = OBDal.getInstance().createCriteria(BusinessPartner.class);
				querypartnerCommerce.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, Code_Commerce));
				List<BusinessPartner> listPartnerCommerce = querypartnerCommerce.list();
				if(listPartnerCommerce.size()>0) {
					BusinessPartner objPartnerCommerce = listPartnerCommerce.get(0);
					queryCredit.add(Restrictions.eq(Invoice.PROPERTY_SHPICCBPCOMERS, objPartnerCommerce));
				}
			}
			queryCredit.add(Restrictions.in(Invoice.PROPERTY_TRANSACTIONDOCUMENT + ".id", docTypeIds));
			listCredits=queryCredit.list();
		} catch (Exception e) {objParentOperations.put("Credit_Operations","Sin registro"); Error=e.getMessage();}

		objParentOperations.put("Identifier", Identifier);
		objParentOperations.put("Equifax_segmentacion", segmentacionValue);
    	if(listCredits.size()>0) {
    		JSONArray arrayOperations = new JSONArray();
    		for(Invoice objInvoice:listCredits) {
				JSONObject objOperation = new JSONObject();
				BusinessPartner objPartner = objInvoice.getBusinessPartner();
				BusinessPartner objCommerce = objInvoice.getShpicCbpcomers();
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
				objOperation.put("No_Credit", objInvoice.getDocumentNo());
				objOperation.put("Client_Name", objPartner.getSsscrbpName()+" "+objPartner.getSSSCRBPLastname());
				objOperation.put("Identifier", objPartner.getTaxID());
				try {objOperation.put("Product_Name", objInvoice.getShpicProduct().getIdentifier());}catch(Exception e) {}
				BigDecimal grandTotal = objInvoice.getGrandTotalAmount();
				if(grandTotal == null) {grandTotal = BigDecimal.ZERO;}
				BigDecimal roundedTotal = grandTotal.setScale(2, RoundingMode.HALF_UP);
				objOperation.put("Credit_Amount", roundedTotal);
				BigDecimal Input_Value = objInvoice.getShpicEntrance();
				if(Input_Value == null) {Input_Value = BigDecimal.ZERO;}
				BigDecimal roundedInput_Value = Input_Value.setScale(2, RoundingMode.HALF_UP);
				objOperation.put("Input_Value", roundedInput_Value);
				objOperation.put("Fee_Value", objInvoice.getShpicFinancivaluedues().setScale(2, RoundingMode.HALF_UP));
				objOperation.put("Term", objInvoice.getShppwsTerm());
				objOperation.put("Fees_Paid", objInvoice.getShpicLastduespaid());
				objOperation.put("Credit_Date",objInvoice.getInvoiceDate() );
				objOperation.put("Due_Date", objInvoice.getShpicDuedate());
				objOperation.put("Maximum_Delay", objInvoice.getShpicMaxdelay());
				int averageDelay=0;
				try {averageDelay = objInvoice.getShpicAveragedelay().intValue();}catch(Exception e) {}
				objOperation.put("Average_Delay", averageDelay);
				objOperation.put("Credit_Status", TypeStatus);
				if(objCommerce!=null) {
					objOperation.put("Trade_Name", objCommerce.getSsscrbpName()+" "+objCommerce.getSSSCRBPLastname());
				}
				objOperation.put("Currency_Type", objInvoice.getCurrency().getISOCode());
				objOperation.put("Seller", objInvoice.getShppwsUser());
				objOperation.put("Advance_Value_Fees", objInvoice.getShpicAdvancevalue().setScale(2, RoundingMode.HALF_UP));
				
				try {OBCriteria<SscfCreditOperation> queryCreditOperations = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
				queryCreditOperations.add(Restrictions.eq(SscfCreditOperation.PROPERTY_DOCUMENTNO, objInvoice.getDocumentNo()));
				List<SscfCreditOperation> listobjCreditOperations = queryCreditOperations.list();
				SscfCreditOperation objCreditOperation = listobjCreditOperations.get(0);
				BigDecimal total_credit_amount = objCreditOperation.getValueInvoice();
				BigDecimal fee_total_paid = objInvoice.getTotalPaid();
				objOperation.put("Credit_Balance", (total_credit_amount.subtract(fee_total_paid)).setScale(2, RoundingMode.HALF_UP));
				}catch(Exception e) {Error = e.getMessage();}
				
					arrayOperations.put(objOperation);
    		}
		objParentOperations.put("Credit_Operations", arrayOperations);
    	}else {
    		objParentOperations.put("Credit_Operations", "Sin registro");
    	}
    	
    	arrayParentOperations.put(objParentOperations);
		//Finalmente devuelvo
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json = arrayParentOperations.toString();
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
	    
		String requestUrl = request.getRequestURL().toString();
		
		try {
        	String noReference = Identifier;
	        String Interface = "SHPPWS_NT";
		    String Process = "Estado cuenta clientes";
		    String idRegister = "";
		    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
		}catch(Exception e) {}
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
