package ec.com.sidesoft.happypay.web.services.monitor;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.service.db.DalConnectionProvider;

import ec.com.sidesoft.credit.simulator.scsl_Creditservices;
import ec.com.sidesoft.happypay.credit.factory.ShpcfPaymentChannel;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.shppws_monitor;
import ec.com.sidesoft.happypay.web.services.shppws_monitor;

public class ProccesDataMonitor {
	
	public ResultJSON processDataMonitor(JSONObject jsonMonitor, shppws_config accesApi) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		String TypeOfMonitor ="";
        
		List<shppws_monitor> monitorList = accesApi.getShppwsMonitorList();
		if(monitorList.size()>0) {
			String Identifier = validatorData("Identifier", jsonMonitor);
			String Identifier2 = validatorData("Identifier2", jsonMonitor);
			if(!Identifier.equals("") || !Identifier2.equals("")) {
				for(shppws_monitor obj: monitorList) {
					String Interface = obj.getEndpoint();
					if(Interface != null && !Interface.equals("") && jsonMonitor.has(Interface)) {
						TypeOfMonitor = validatorData("TypeOfMonitor", jsonMonitor);
						JSONObject objJSON = null;
						if(!TypeOfMonitor.equals("") && TypeOfMonitor.equals("Reverso")) {
							objJSON = fillJSON3(obj, jsonMonitor);
						}else if(!TypeOfMonitor.equals("") && TypeOfMonitor.equals("Pagos")) {
							objJSON = fillJSON2(obj, jsonMonitor);
						}else {
							objJSON = fillJSON(obj, jsonMonitor);
						}
						if(objJSON != null) {
							jsonArray.put(objJSON);
						}
					}
				}
			}
		}
		return new ResultJSON(jsonArray,TypeOfMonitor);
	}
	
	public JSONObject fillJSON3(shppws_monitor obj, JSONObject jsonMonitor) throws JSONException {
		JSONObject objJSON =  new JSONObject();
		String Interface = obj.getEndpoint();
		String MSG = obj.getMessage() != null? obj.getMessage() :"default";
		String Provider = obj.getProvider() != null? validatorProvider(obj.getProvider()) :"default";
		
		
		OBCriteria<FIN_Payment> queryFIN_Payment= OBDal.getInstance().createCriteria(FIN_Payment.class);
  		queryFIN_Payment.add(Restrictions.eq(FIN_Payment.PROPERTY_DOCUMENTNO, validatorData("Identifier",jsonMonitor)));
  		List<FIN_Payment> listFIN_Payment = queryFIN_Payment.list();
  		FIN_Payment payment = listFIN_Payment.size() > 0 ? listFIN_Payment.get(0):null;
  		ShpcfPaymentChannel channel =payment.getShpicPaymentchannelcharg();
		String customerIdentification = payment != null? payment.getBusinessPartner().getSearchKey():"";
		String status = validatorData("status"+Interface, jsonMonitor);
		Invoice invoice = payment != null && payment.getShpicNodocumeennt() != null ? payment.getShpicNodocumeennt() : null ;
		String opportunityNumber = invoice != null?invoice.getDocumentNo():"" ;
		String amount = payment != null? payment.getSwsslImportChargedTotal()+"":"0";
		BusinessPartner objCommerce = invoice != null && invoice.getShpicCbpcomers() != null? invoice.getShpicCbpcomers(): null;
		String businessName = objCommerce != null?	objCommerce.getName(): "";
		String startRequestTime = validatorData("start"+Interface, jsonMonitor);
		String endRequestTime = validatorData("end"+Interface, jsonMonitor);
		String message = validatorData("Message", jsonMonitor);
		String type = "Reverso";
		
		objJSON.put("channel", channel);
		objJSON.put("customerIdentification", customerIdentification);
		objJSON.put("status", status);
		objJSON.put("opportunityNumber", opportunityNumber);
		objJSON.put("amount", amount);
		objJSON.put("businessName", businessName);
		objJSON.put("startRequestTime", startRequestTime);
		objJSON.put("endRequestTime", endRequestTime);
		objJSON.put("status", status);
		objJSON.put("message", message);
		objJSON.put("type", type);
		
		return objJSON;
	}
	
	public JSONObject fillJSON2(shppws_monitor obj, JSONObject jsonMonitor) throws JSONException {
		JSONObject objJSON =  new JSONObject();
		String Interface = obj.getEndpoint();
		String MSG = obj.getMessage() != null? obj.getMessage() :"default";
		String Provider = obj.getProvider() != null? validatorProvider(obj.getProvider()) :"default";
		
		String channel = validatorData("paymenchannel", jsonMonitor.has("body")? jsonMonitor.getJSONObject("body"):jsonMonitor);
		String auxPaymentID = validatorData("paymentId", jsonMonitor.has("body")? jsonMonitor.getJSONObject("body"):jsonMonitor);
		FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, auxPaymentID);
		String customerIdentification = payment != null? payment.getBusinessPartner().getSearchKey():"";
		String opportunityNumber = validatorData("creditOperationId", jsonMonitor.has("body")? jsonMonitor.getJSONObject("body"):jsonMonitor);
		String amount = validatorData("amountToPay", jsonMonitor.has("body")? jsonMonitor.getJSONObject("body"):jsonMonitor);
		Invoice invoice = payment != null && payment.getShpicNodocumeennt() != null ? payment.getShpicNodocumeennt() : null ;
		BusinessPartner objCommerce = invoice != null && invoice.getShpicCbpcomers() != null? invoice.getShpicCbpcomers(): null;
		String businessName = objCommerce != null?	objCommerce.getName(): "";
		String startRequestTime = validatorData("start"+Interface, jsonMonitor);
		String endRequestTime = validatorData("end"+Interface, jsonMonitor);
		String status = validatorData("status"+Interface, jsonMonitor);
		String message = validatorData("Message", jsonMonitor.has("Message") && jsonMonitor.getJSONObject("Message") != null? jsonMonitor.getJSONObject("Message"): jsonMonitor );
		String type = "Pago";
		
		objJSON.put("channel", channel);
		objJSON.put("customerIdentification", customerIdentification);
		objJSON.put("status", status);
		objJSON.put("opportunityNumber", opportunityNumber);
		objJSON.put("amount", amount);
		objJSON.put("businessName", businessName);
		objJSON.put("startRequestTime", startRequestTime);
		objJSON.put("endRequestTime", endRequestTime);
		objJSON.put("status", status);
		objJSON.put("message", message);
		objJSON.put("type", type);
		
		return objJSON;
	}
	
	public JSONObject fillJSON(shppws_monitor obj, JSONObject jsonMonitor) throws JSONException {
		JSONObject objJSON =  new JSONObject();
		String Interface = obj.getEndpoint();
		String MSG = obj.getMessage() != null? obj.getMessage() :"default";
		String Provider = obj.getProvider() != null? validatorProvider(obj.getProvider()) :"default";
		
		String startRequestTime = validatorData("start"+Interface, jsonMonitor);
		String endRequestTime = validatorData("end"+Interface, jsonMonitor);
		String status = validatorData("status"+Interface, jsonMonitor);
		String opportunityNumber = validatorData("Identifier", jsonMonitor);
		String customerIdentificacion = validatorData("Identifier2", jsonMonitor);
		String supplierName = Provider;
		String message = MSG;
		String type = validatorData("type"+Interface, jsonMonitor);
		
		objJSON.put("startRequestTime", startRequestTime);
		objJSON.put("endRequestTime", endRequestTime);
		objJSON.put("status", status);
		objJSON.put("opportunityNumber", opportunityNumber);
		objJSON.put("customerIdentificacion", customerIdentificacion);
		objJSON.put("supplierName", supplierName);
		objJSON.put("message", message);
		objJSON.put("type", type);
		
		return objJSON;
	}
	
	public String validatorData(String key, JSONObject jsonMonitor) throws JSONException {
		String value = "";
		if(jsonMonitor.has(key) && jsonMonitor.getString(key) != null) {
			value = jsonMonitor.getString(key);
		}
		return value;
	}
	
	
	public String validatorProvider(String provider) {
		if(provider.equals("SHPPWS_SideSoft")) {
			provider = "SideSoft";
		}else if(provider.equals("SHPPWS_Recover")) {
			provider = "Recover";
		}else if(provider.equals("SHPPWS_Netby")) {
			provider = "Netby";
		}
		return provider;
	}

}
