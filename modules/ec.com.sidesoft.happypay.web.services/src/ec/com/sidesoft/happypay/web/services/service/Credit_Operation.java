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
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.service.web.WebService;
import org.hibernate.criterion.Order;
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
import ec.com.sidesoft.credit.simulator.AdditionalServicesInv;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.fast.quotation.EcsfqAmortization;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.localization.geography.secpm_canton;
import it.openia.crm.Opcrmopportunities;

import java.util.Base64;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;

public class Credit_Operation implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "Credit_Operation", requestParameters);
		
	String No_Credit = request.getParameter("No_Credit");
			String Message="";
			
		        JSONArray arrayParentOperation = new JSONArray();
					JSONObject objParentOperation = new JSONObject();
					
					Invoice objInvoice = new Invoice();
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
					    
					OBCriteria<Invoice> queryCredit = OBDal.getInstance().createCriteria(Invoice.class);
					queryCredit.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO, No_Credit));
					queryCredit.add(Restrictions.in(Invoice.PROPERTY_TRANSACTIONDOCUMENT + ".id", docTypeIds));
					objInvoice = (Invoice)queryCredit.uniqueResult();
					BusinessPartner objPartner = objInvoice.getBusinessPartner();
					BusinessPartner objCommerce = objInvoice.getShpicCbpcomers();
					JSONArray arrayParentOperations = new JSONArray();
					JSONObject objParentOperations = new JSONObject();
					
					OBCriteria<SscfCreditOperation> queryCreditOperations = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
					queryCreditOperations.add(Restrictions.eq(SscfCreditOperation.PROPERTY_DOCUMENTNO, No_Credit));
					List<SscfCreditOperation> listobjCreditOperations = queryCreditOperations.list();
					SscfCreditOperation objCreditOperation = listobjCreditOperations.get(0);
					
					
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
					
					String LocationPartner = "";

					OBCriteria<Location> bpLocationCrit = OBDal.getInstance().createCriteria(Location.class);
					bpLocationCrit.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, objPartner));
					bpLocationCrit.add(Restrictions.eq(Location.PROPERTY_ACTIVE, true));
					bpLocationCrit.addOrder(Order.desc(Location.PROPERTY_CREATIONDATE)); // O usa PROPERTY_CREATED si prefieres
					bpLocationCrit.setMaxResults(1);

					Location latestLocation = (Location) bpLocationCrit.uniqueResult();
					
					Location bpLocation = (Location) bpLocationCrit.uniqueResult();
					
					String ConcatDireccion="";

					if(bpLocation == null) {
						ConcatDireccion="";
					}else {
					
						org.openbravo.model.common.geography.Location objLocation = bpLocation.getLocationAddress();
						ConcatDireccion="";
						String direccion="";
						String ciudad="";
						String provincia="";
						String cantonNombre="";
						String pais="";
						
						if (bpLocation != null) {
							if(objLocation != null) {
							    Country country = objLocation.getCountry();
							    Region region = objLocation.getRegion();
							    secpm_canton canton = objLocation.getScrmactCanton();
							    
							    if(country != null) {
								    pais = country.getName();
							    }
							    if(region != null) {
							    	provincia = region.getName();
							    }
							    if(canton != null) {
							    	cantonNombre = canton.getName();
							    }
								direccion=objLocation.getAddressLine1();
								ciudad=objLocation.getCityName();
							}
						    ConcatDireccion = direccion + " " + ciudad + " " + pais + " " + provincia + " " + cantonNombre;
						}
					}


						objParentOperation.put("No_Credit", objInvoice.getDocumentNo());
						objParentOperation.put("Client_Name", objPartner.getSsscrbpName()+" "+objPartner.getSSSCRBPLastname());
						objParentOperation.put("Identifier", objPartner.getTaxID());
						objParentOperation.put("Address", ConcatDireccion);
						String descripcion="";
						if(objInvoice.getDescription()==null || objInvoice.getDescription().equals("")) {
							descripcion="";
						}else {
							descripcion=objInvoice.getDescription();
						}
						objParentOperation.put("Product_Information", descripcion);
						try {objParentOperation.put("Additional_value", objInvoice.getShpctAdditional1());}catch(Exception e) {}
						try {objParentOperation.put("Product_Name", objInvoice.getShpicProduct().getIdentifier());}catch(Exception e) {}
						BigDecimal grandTotal = objInvoice.getGrandTotalAmount();
						if(grandTotal == null) {grandTotal = BigDecimal.ZERO;}
						BigDecimal roundedTotal = grandTotal.setScale(2, RoundingMode.HALF_UP);
						objParentOperation.put("Credit_Amount", roundedTotal);
						BigDecimal Input_Value = objInvoice.getShpicEntrance();
						if(Input_Value == null) {Input_Value = BigDecimal.ZERO;}
						BigDecimal roundedInput_Value = Input_Value.setScale(2, RoundingMode.HALF_UP);
						objParentOperation.put("Input_Value", roundedInput_Value);
						objParentOperation.put("Fee_Value", objInvoice.getShpicFinancivaluedues().setScale(2, RoundingMode.HALF_UP));
						objParentOperation.put("Term", objInvoice.getShppwsTerm());
						objParentOperation.put("Fees_Paid", objInvoice.getShpicLastduespaid());
						objParentOperation.put("Credit_Date",objInvoice.getInvoiceDate() );
						objParentOperation.put("Due_Date", objInvoice.getShpicDuedate());
						objParentOperation.put("Maximum_Delay", objInvoice.getShpicMaxdelay());
						int averageDelay=0;
						try {averageDelay = objInvoice.getShpicAveragedelay().intValue();}catch(Exception e) {}
						objParentOperation.put("Average_Delay", averageDelay);
						objParentOperation.put("Credit_Status", TypeStatus);
						if(objCommerce!=null) {
							objParentOperation.put("Trade_Name", objCommerce.getSsscrbpName()+" "+objCommerce.getSSSCRBPLastname());
						}
						objParentOperation.put("Currency_Type", objInvoice.getCurrency().getISOCode());
						objParentOperation.put("Seller", objInvoice.getShppwsUser());
						objParentOperation.put("Advance_Value_Fees", objInvoice.getShpicAdvancevalue().setScale(2, RoundingMode.HALF_UP));
						try {BigDecimal total_credit_amount = objCreditOperation.getValueInvoice();
						BigDecimal fee_total_paid = objInvoice.getTotalPaid();
						objParentOperation.put("Credit_Balance", (total_credit_amount.subtract(fee_total_paid)).setScale(2, RoundingMode.HALF_UP));
						objParentOperation.put("Type_Assistance", objInvoice.getShpcfTypeatt().getName()==null?"null":objInvoice.getShpcfTypeatt().getName());
						objParentOperation.put("ValueActivation", objInvoice.getShppwsValueActivation() == null ? ""
						: objInvoice.getShppwsValueActivation().setScale(2, RoundingMode.HALF_UP));
						}catch(Exception e) {Error = e.getMessage();}
						
						OBCriteria<FIN_PaymentSchedule> queryPaymentPlan = OBDal.getInstance().createCriteria(FIN_PaymentSchedule.class);
						queryPaymentPlan.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, objInvoice));
						List<FIN_PaymentSchedule> listAmortization = queryPaymentPlan.list();
						Collections.sort(listAmortization, (e1, e2) -> e1.getSwsslNroCuota().compareTo(e2.getSwsslNroCuota()));
						
							JSONArray arrayOperation = new JSONArray();
							for(FIN_PaymentSchedule objAmortization:listAmortization) {
								JSONObject objOperation = new JSONObject();
								BigDecimal valueAdditionalServices = objAmortization.getScslAddservices() != null? objAmortization.getScslAddservices(): BigDecimal.ZERO;
								JSONArray arrayServices = new JSONArray();
								
								objOperation.put("No_Fee", objAmortization.getSwsslNroCuota());
								objOperation.put("F_Expires", objAmortization.getDueDate());
								String stateFee = objAmortization.getShppsFeeStatus();
								if(stateFee.equals("PYT")) {
									stateFee = "PAGADO";
								}else if(stateFee.equals("PDG")) {
									stateFee = "PENDIENTE";
								}else if(stateFee.equals("DFT")) {
									stateFee = "VENCIDO";
								}
								objOperation.put("State", stateFee);
								objOperation.put("Capital", objAmortization.getShppsCapital());
								objOperation.put("Interest", 0);
								objOperation.put("Balance", objAmortization.getOutstandingAmount());
								objOperation.put("F_Payment", objAmortization.getLastPaymentDate());
								objOperation.put("Delay", objAmortization.getShppsDaysArrears());
								objOperation.put("G_Collection", objAmortization.getShppsExpensesCollection());
								objOperation.put("Desc_Expense_Collection", 0);
								objOperation.put("Service", objAmortization.getShppsService());
								objOperation.put("assistance", objAmortization.getShppsAttendance());
								objOperation.put("AdditionalServicesValue", valueAdditionalServices);
								for(AdditionalServicesInv objAddServices : objAmortization.getScslAdditionalServicesInvList()) {
									JSONObject jsonService = new JSONObject();
									String nameService = objAddServices.getAdditionalservice() != null ? objAddServices.getAdditionalservice().getValidationCode():" No parametrizado";
									BigDecimal amtService = objAddServices.getSearchKey() != null ? objAddServices.getSearchKey() : BigDecimal.ZERO;
									jsonService.put("Name", nameService);
									jsonService.put("Amount", amtService);
									arrayServices.put(jsonService);
								}
								objOperation.put("Discount", 0);
								BigDecimal Total_To_Pay = objAmortization.getShppsCapital().add(objAmortization.getShppsService()).add(objAmortization.getShppsAttendance()).add(objAmortization.getShppsExpensesCollection()).add(valueAdditionalServices);
								objOperation.put("Total_To_Pay", Total_To_Pay);
								objOperation.put("Paid", objAmortization.getPaidAmount());
								String checkFreeQuota = "";
								if(objAmortization.isShppwsFreeQuota()) {
									checkFreeQuota="TRUE";
								}else {
									checkFreeQuota="FALSE";
								}
								objOperation.put("Cuota_Gratis", checkFreeQuota);
								objOperation.put("AdditionalServices", arrayServices);
									arrayOperation.put(objOperation);
							}
								
						objParentOperation.put("Quota", arrayOperation);
					}catch(Exception e) {
						objParentOperation.put("No_Credit",No_Credit);
						objParentOperation.put("Quota", 0);
						Error = Error + e.getMessage();
					}
					arrayParentOperation.put(objParentOperation);
					
			//Finalmente devuelvo
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			String json = arrayParentOperation.toString();
			PrintWriter writer = response.getWriter();
			writer.write(json);
			writer.close();

			String requestUrl = request.getRequestURL().toString();
			try {
	        	String noReference = No_Credit;
		        String Interface = "SHPPWS_NT";
			    String Process = "Detalle Operación Crédito";
			    String idRegister = objInvoice.getId();
			    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
			}catch(Exception e) {
				String noReference = No_Credit;
		        String Interface = "SHPPWS_NT";
			    String Process = "Detalle Operación Crédito";
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
