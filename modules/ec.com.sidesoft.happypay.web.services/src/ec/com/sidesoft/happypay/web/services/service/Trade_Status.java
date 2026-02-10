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
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;
import java.text.SimpleDateFormat;

public class Trade_Status implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "Trade_Status", requestParameters);
		
	String Id_Commerce = request.getParameter("Id_Commerce");
	String Date_From = request.getParameter("Date_From");
	String Date_Until = request.getParameter("Date_Until");
			String Message="";
			JSONArray arrayParentTrade = new JSONArray();
			JSONObject objparentTrade = new JSONObject();
			JSONArray arrayTrade = new JSONArray();
			
			try {
			OBCriteria<BusinessPartner> querypartner= OBDal.getInstance().createCriteria(BusinessPartner.class);
		        querypartner.add(Restrictions.eq(BusinessPartner.PROPERTY_TAXID, Id_Commerce));
		        BusinessPartner objPartner = (BusinessPartner) querypartner.uniqueResult();
			
			OBCriteria<Invoice> AuxQueryCredit = OBDal.getInstance().createCriteria(Invoice.class);
			AuxQueryCredit.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, objPartner));
			AuxQueryCredit.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION,false));
			AuxQueryCredit.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS,"CO"));
			if(Date_From!="" && Date_From!=null && Date_Until!="" && Date_Until!=null) {
				String formatoFecha = "dd-MM-yyyy"; //
				SimpleDateFormat sdf = new SimpleDateFormat(formatoFecha);
				Date dateFrom = sdf.parse(Date_From);
			    Date dateUntil = sdf.parse(Date_Until);
			    Calendar calendar = Calendar.getInstance();
			    calendar.setTime(dateUntil);
			    calendar.set(Calendar.HOUR_OF_DAY, 23);
			    calendar.set(Calendar.MINUTE, 59);
			    calendar.set(Calendar.SECOND, 59);
			    dateUntil = calendar.getTime();
			    AuxQueryCredit.add(Restrictions.ge(Invoice.PROPERTY_INVOICEDATE, dateFrom)); // >=
				AuxQueryCredit.add(Restrictions.le(Invoice.PROPERTY_INVOICEDATE, dateUntil)); // <=
			}
			List<Invoice> auxListCredit = AuxQueryCredit.list();
			int devicesSold = auxListCredit.size();
			
			int currentCredits=0;
			String byproduct = "";
			BigDecimal amountPaid = BigDecimal.ZERO;
			BigDecimal amountPending = BigDecimal.ZERO;
			if(auxListCredit.size() > 0) {
				String refInvoice = "";
				for(Invoice auxObjCredit: auxListCredit) {
					refInvoice =  auxObjCredit.getOrderReference();
					OBCriteria<Invoice> queryCurrentCredit = OBDal.getInstance().createCriteria(Invoice.class);
					queryCurrentCredit.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO,refInvoice));
					queryCurrentCredit.add(Restrictions.eq(Invoice.PROPERTY_SHPICOPERATIONSTATE,"02"));//VIGENTE
					List<Invoice> listCurrentCredit = queryCurrentCredit.list();
					if(listCurrentCredit.size()>0) {
						currentCredits = currentCredits+1;
					}
					 amountPaid= amountPaid.add(auxObjCredit.getTotalPaid());//PAGADO
					 amountPending= amountPending.add(auxObjCredit.getOutstandingAmount());//PENDIENTE
				}
			}
						objparentTrade.put("Id_Commerce", objPartner.getTaxID());
						objparentTrade.put("Commerce_Name", objPartner.getSsscrbpName()+" "+objPartner.getSSSCRBPLastname() );
						objparentTrade.put("Currency", objPartner.getCurrency().getISOCode());
						objparentTrade.put("No_Devices_Sold", devicesSold);
						objparentTrade.put("Current_Credits", currentCredits);
						objparentTrade.put("Amount_Devices_Paid", amountPaid);
						objparentTrade.put("Outstanding_Balance", amountPending);
							
								if(auxListCredit.size() > 0) {
									for(Invoice auxObjCredit: auxListCredit) { //PROVIDER
										JSONObject objTrade = new JSONObject();
										String refInvoice =  auxObjCredit.getOrderReference();
										OBCriteria<Invoice> queryCurrentCredit = OBDal.getInstance().createCriteria(Invoice.class);//CLIENT
										queryCurrentCredit.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO,refInvoice));
										List<Invoice> listCurrentCredit = queryCurrentCredit.list();
										if(listCurrentCredit.size()>0) {
											Invoice objCurrentCredit = listCurrentCredit.get(0);
											objTrade.put("Credit_User", objCurrentCredit.getShppwsUser());
											objTrade.put("Agency", objCurrentCredit.getShpicAgency());
											byproduct=objCurrentCredit.getShpicByscspr().getCommercialName();
										}
										objTrade.put("Credit_Code", auxObjCredit.getDocumentNo());
										objTrade.put("Make/Model", auxObjCredit.getDescription());
										BigDecimal Amount_To_Finance = auxObjCredit.getGrandTotalAmount();
										BigDecimal valueFinance = Amount_To_Finance.setScale(2, RoundingMode.HALF_UP);
										objTrade.put("Amount_To_Finance", valueFinance);
										objTrade.put("Sale_Date", auxObjCredit.getInvoiceDate());
										BigDecimal valuePendingCredit = auxObjCredit.getOutstandingAmount();
										if(valuePendingCredit.compareTo(BigDecimal.ZERO) == 0){
											objTrade.put("State", "PAGADO");
										} else if (valuePendingCredit.compareTo(BigDecimal.ZERO) > 0) {
											objTrade.put("State", "PENDIENTE");
										}
										OBCriteria<FIN_PaymentSchedule> queryLastPayment = OBDal.getInstance().createCriteria(FIN_PaymentSchedule.class);
										queryLastPayment.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE,auxObjCredit));
										List<FIN_PaymentSchedule> listqueryLastPayment = queryLastPayment.list();
										if(listqueryLastPayment.size()>0) {
											try {
												Collections.sort(listqueryLastPayment, (e1, e2) -> e2.getLastPaymentDate().compareTo(e1.getLastPaymentDate()));
												FIN_PaymentSchedule objLastPayment = listqueryLastPayment.get(0);
												objTrade.put("Payment_Date", objLastPayment.getLastPaymentDate());
											}catch(Exception e) {objTrade.put("Payment_Date", ""); Error = e.getMessage();}
										}
										objTrade.put("Sub_Product", byproduct);
										
									 arrayTrade.put(objTrade);
									}
								}
			}catch(Exception e) {objparentTrade.put("Id_Commerce", Id_Commerce); Error = e.getMessage();}
			
			objparentTrade.put("Account_To_Pay", arrayTrade);
			arrayParentTrade.put(objparentTrade);
	        
			//Finalmente devuelvo
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			String json = arrayParentTrade.toString();
			PrintWriter writer = response.getWriter();
			writer.write(json);
			writer.close();
			
			String requestUrl = request.getRequestURL().toString();
			
			try {
	        	String noReference = Id_Commerce;
		        String Interface = "SHPPWS_NT";
			    String Process = "Estado Comercio";
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
