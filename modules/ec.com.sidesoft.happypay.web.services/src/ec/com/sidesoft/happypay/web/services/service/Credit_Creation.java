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
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.web.WebService;
import org.hibernate.criterion.Restrictions;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.credit.simulator.AdditionalServicesInv;
import ec.com.sidesoft.credit.simulator.AdditionalServicesOP;
import ec.com.sidesoft.credit.simulator.scsl_Product;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.fast.quotation.EcsfqAmortization;
import ec.com.sidesoft.fast.quotation.QuotationLine;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.monitor.MonitorManager;
import ec.com.sidesoft.ws.equifax.SweqxEquifax;
import it.openia.crm.Opcrmopportunities;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxCategory;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.base.exception.OBException;

import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.client.kernel.RequestContext;


public class Credit_Creation implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		JSONObject jsonMonitor = new JSONObject();
		jsonMonitor.put("SHPPWS_SideSoft_CCredit", "Service"+0);
	        jsonMonitor.put("startSHPPWS_SideSoft_CCredit", LocalDateTime.now());
	        jsonMonitor.put("typeSHPPWS_SideSoft_CCredit", "Creación del Crédito");
		
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "Credit_Creation", requestParameters);
		
		String No_Opportunity = request.getParameter("No_Opportunity");
		String Imei = request.getParameter("Imei");
		String Description = request.getParameter("Description");
		String Padlock = request.getParameter("Padlock");
		String Additional1 = request.getParameter("Additional1");
		String ValueActivation = request.getParameter("ValueActivation");
		String Identifier2 = "";
		
		JSONObject records = new JSONObject();
		String message = "";
		Map<String, Object> listMessage = new HashMap<>();
		listMessage.put("Imei", Imei);
		listMessage.put("Description", Description);
		listMessage.put("ValueActivation", ValueActivation);

		Opcrmopportunities objOpportunity = new Opcrmopportunities();
		try {
		message += " verify if exist Invoice";
		
		OBCriteria<Invoice> queryVerifyInvoice = OBDal.getInstance().createCriteria(Invoice.class);
		queryVerifyInvoice.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO, No_Opportunity));
		List<Invoice> listobjInvoices = queryVerifyInvoice.list();
		if(listobjInvoices.size()>0) {
			throw new Exception(" verifique existencia o duplicidad "+No_Opportunity);
		}
		
		OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance().createCriteria(Opcrmopportunities.class);
		queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
		List<Opcrmopportunities> listobjOpportunity = queryOpportunity.list();
		if(listobjOpportunity.size()<=0) {
			throw new Exception(" oportunidad no encontrada "+No_Opportunity);
		}
		objOpportunity = listobjOpportunity.get(0);
		Identifier2 = objOpportunity.getTAXBpartner();
		
		OBCriteria<ECSFQ_Quotation> queryobjFastQuotation = OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
    	queryobjFastQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_OPCRMOPPORTUNITIES, objOpportunity));
    	List<ECSFQ_Quotation> listFastQuotation = queryobjFastQuotation.list();
    	if(listFastQuotation.size()<=0) {
			throw new Exception(" cotización rápida no encontrada "+No_Opportunity);
		}
    	ECSFQ_Quotation objFastQuotation=listFastQuotation.get(0);
    	
    	OBCriteria<EcsfqAmortization> queryobjAmortization = OBDal.getInstance().createCriteria(EcsfqAmortization.class);
    	queryobjAmortization.add(Restrictions.eq(EcsfqAmortization.PROPERTY_ECSFQORDER, objFastQuotation));
    	List<EcsfqAmortization> listAmortization = queryobjAmortization.list();
    	if(listAmortization.size()<=0) {
			throw new Exception(" amortización no encontrada "+No_Opportunity);
		}
    	
    	OBCriteria<SscfCreditOperation> querySscfCreditOperation = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
		querySscfCreditOperation.add(Restrictions.eq(SscfCreditOperation.PROPERTY_SSCORORDER,objFastQuotation));
		List<SscfCreditOperation> listobjCreditOperation = querySscfCreditOperation.list();
		if(listobjCreditOperation.size()<=0) {
			throw new Exception(" operación de crédito no encontrada "+No_Opportunity);
		}
		SscfCreditOperation objCreditOperation =listobjCreditOperation.get(0);
    	
		scsl_Product products = objOpportunity.getShppwsOpProductcode();

		Product pCategory = products.getProduct();
		message += " obtiene producto";
		OBCriteria<Product> queryProduct = OBDal.getInstance().createCriteria(Product.class);
		queryProduct.add(Restrictions.eq(Product.PROPERTY_ID, pCategory.getId()));
		Product product = (Product) queryProduct.uniqueResult();
		TaxCategory idCategory = product.getTaxCategory();
		message += " obtiene TaxCategory";
		OBCriteria<TaxRate> queryTax = OBDal.getInstance().createCriteria(TaxRate.class);
		queryTax.add(Restrictions.eq(TaxRate.PROPERTY_TAXCATEGORY, idCategory));
		List<TaxRate> idTaxs = queryTax.list();
		TaxRate c_tax = new TaxRate();
		message += " obtiene TaxRate";
		for (TaxRate idTax : idTaxs) {
			 c_tax = OBDal.getInstance().get(TaxRate.class, idTax.getId());
			 message += " obtiene c_tax";
		}
		
		Invoice newInvoice = serviceSalesInvoice( accesApi, product, c_tax, objOpportunity, listAmortization, listMessage, objCreditOperation );
			message += " llega a actualizar ";
			message += "newInvoice: " + newInvoice.getCreationDate();
		    //newInvoice.setInvoiceDate(newInvoice.getCreationDate());
			long diferenciaMillis = objCreditOperation.getUpdated().getTime() - objOpportunity.getCreationDate().getTime();
			long diferenciaSeg = TimeUnit.MILLISECONDS.toSeconds(diferenciaMillis);
			newInvoice.setShppwsApprovalTime(new BigDecimal(diferenciaSeg));
			newInvoice.setProcessed(true);
			newInvoice.setShpicNodocument(newInvoice);//
			if(Padlock!="") {
				newInvoice.setShppwsPadlock(Padlock);
			}
			message += "newInvoice: " + diferenciaSeg;
			if (Additional1 != null && !Additional1.equals("")) {
				BigDecimal additional1BigDecimal;
	            try {
	                additional1BigDecimal = new BigDecimal(Additional1);
	            } catch (NumberFormatException e) {
	            	additional1BigDecimal = BigDecimal.ZERO;
	            }
	            newInvoice.setShpctAdditional1(additional1BigDecimal);
	        } 
			OBDal.getInstance().save(newInvoice);
			OBDal.getInstance().flush();
		List<FIN_PaymentSchedule> list_PaymentSchedule = InvoicePaymentSchedule(newInvoice, listAmortization, listMessage);
		InvoicePaymentScheduledetail( newInvoice, list_PaymentSchedule, listMessage);
		
		Invoice newInvoiceProvider = serviceSalesInvoiceProvider( accesApi, product, c_tax, objOpportunity, listAmortization, listMessage, objCreditOperation, newInvoice );
		
		final List<Object> parameters = new ArrayList<Object>();
		parameters.add(null);
		parameters.add(newInvoiceProvider.getId());
		final String procedureName = "c_invoice_post";
		CallStoredProcedure.getInstance().call(procedureName, parameters, null, true, false);
		OBDal.getInstance().refresh(newInvoiceProvider);
		OBDal.getInstance().save(newInvoiceProvider);
		OBDal.getInstance().flush();
		
		message += listMessage.get("message");
		records.put("No_Opportunity", No_Opportunity);
		records.put("Message", "Ok");
	    records.put("No_Invoice", newInvoice.getDocumentNo());
	    //OBDal.getInstance().commitAndClose();
	    OBDal.getInstance().getConnection().commit();
	    
		}catch(Exception e) {
			message = (String)listMessage.get("message");
			records.put("No_Opportunity", No_Opportunity);
			records.put("Message", "Error"+ e.getMessage());
		    records.put("No_Invoice", "");
		    Error = e.getMessage();
		    //OBDal.getInstance().rollbackAndClose();
		    OBDal.getInstance().getSession().getTransaction().rollback();
		}
		
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(records);
        String json = jsonArray.getJSONObject(0).toString();
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.close();
        jsonMonitor.put("endSHPPWS_SideSoft_CCredit", LocalDateTime.now());

        String requestUrl = request.getRequestURL().toString();
        
        try {
        	String noReference = No_Opportunity;
	        String Interface = "SHPPWS_NT";
		    String Process = "Creación Crédito";
		    String idRegister = objOpportunity.getId();
		    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
        }catch(Exception e){
        	String noReference = No_Opportunity;
	        String Interface = "SHPPWS_NT";
		    String Process = "Creación Crédito";
		    String idRegister = "";
		    logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "OUT", Interface, Process, idRegister, Error);
        }
        
        if(records.has("Message") && records.getString("Message").equals("Ok")) {
		jsonMonitor.put("statusSHPPWS_SideSoft_CCredit", "200");
	}else {
		jsonMonitor.put("statusSHPPWS_SideSoft_CCredit", "500");
	}
        jsonMonitor.put("Identifier", No_Opportunity);
        jsonMonitor.put("Identifier2", Identifier2);
	MonitorManager newMonitor = new MonitorManager();
	newMonitor.sendMonitorData(jsonMonitor, accesApi, true, null);
	OBDal.getInstance().getSession().close(); 
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
	
	public Invoice serviceSalesInvoice(shppws_config accesApi, Product product,TaxRate taxRate, Opcrmopportunities objOpportunity, List<EcsfqAmortization> listAmortization, Map<String, Object> listMessage, SscfCreditOperation objCreditOperation) throws Exception {
		String message=" llega a la func";
		listMessage.put("message", message);
		OBCriteria<ECSFQ_Quotation> queryobjFastQuotation = OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
		queryobjFastQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_OPCRMOPPORTUNITIES, objOpportunity));
    	List<ECSFQ_Quotation> listobjFastQuotation = queryobjFastQuotation.list();
    	ECSFQ_Quotation newobjFastQuotation =listobjFastQuotation.get(0);
    	
    	OBCriteria<QuotationLine> queryObjLine = OBDal.getInstance().createCriteria(QuotationLine.class);
    	queryObjLine.add(Restrictions.eq(QuotationLine.PROPERTY_ECSFQORDER, newobjFastQuotation));
    	List<QuotationLine> listObjLine = queryObjLine.list();
    	QuotationLine newObjLine =listObjLine.get(0);
		
		
		Invoice newInvoice = OBProvider.getInstance().get(Invoice.class);
		newInvoice.setSalesTransaction(true);
		newInvoice.setClient(accesApi.getClient());
		newInvoice.setOrganization(accesApi.getOrganization());
		newInvoice.setInvoiceDate(objOpportunity.getCreationDate());
		newInvoice.setAccountingDate(objOpportunity.getCreationDate());
		newInvoice.setDocumentStatus("CO");
		newInvoice.setDocumentAction("CO");
		newInvoice.setAPRMProcessinvoice("RE");
		newInvoice.setProcessed(false);
		newInvoice.setProcessNow(false);
		message+= " primeros pasos de Invoice";
		listMessage.put("message", message);
		DocumentType documentType = accesApi.getFACDocumenttype();

		newInvoice.setDocumentType(documentType);
		newInvoice.setTransactionDocument(documentType);
		newInvoice.setDocumentNo(objOpportunity.getShppwsOpDocumentno());
		newInvoice.setBusinessPartner(objOpportunity.getBusinessPartner());
		
		BusinessPartner objPartner=objOpportunity.getBusinessPartner();
		newInvoice.setBusinessPartner(objPartner);
		newInvoice.setPaymentMethod(objPartner.getPaymentMethod());
		newInvoice.setPaymentTerms(objPartner.getPaymentTerms());
		
		newInvoice.setShpcfTypeatt(accesApi.getShpcfTypeattcredit());
		
		message+= " segundos pasos de Invoice";
		listMessage.put("message", message);
		
		
		OBCriteria<Location> queryLocation = OBDal.getInstance().createCriteria(Location.class);
	    queryLocation.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, objPartner));
	    List<Location> listLocation = queryLocation.list();
	    if (!listLocation.isEmpty()) {
	    	Location objLocation = listLocation.get(0);
	    	newInvoice.setPartnerAddress(objLocation);
    	}
	    
	    message+= " terceros pasos de Invoice";
		newInvoice.setCurrency(accesApi.getPriceList().getCurrency());
		newInvoice.setPriceList(accesApi.getPriceList());
		newInvoice.setCostcenter(accesApi.getCostCenter());
		listMessage.put("message", message);
		
		message+= " cuartos pasos. de Invoice";
		listMessage.put("message", message);
		newInvoice.setSpincoTaxid(objOpportunity.getTAXBpartner());
		newInvoice.setShpicImei((String)listMessage.get("Imei"));
		newInvoice.setSHPPWSScore(new BigDecimal(objCreditOperation.getShppwsScore()));
		newInvoice.setShppwsTerm(new BigDecimal(listAmortization.size()));
		newInvoice.setShpicNodues(new Long(listAmortization.size()));//
		newInvoice.setShpicEntrance(objCreditOperation.getInputValue());
		newInvoice.setShpicAgency(objOpportunity.getShppwsOpAgencycode());
		newInvoice.setShppwsEndSegment(objCreditOperation.getShppwsSegment());
		newInvoice.setShpicProduct(objOpportunity.getShppwsOpProductcode());
		OBCriteria<SweqxEquifax> queryequifax= OBDal.getInstance().createCriteria(SweqxEquifax.class);
		queryequifax.add(Restrictions.eq(SweqxEquifax.PROPERTY_BUSINESSPARTNER, objPartner));
		List<SweqxEquifax> listquifax = queryequifax.list();
		if(listquifax.size() > 0) {
			  Collections.sort(listquifax, (e1, e2) -> e2.getCreationDate().compareTo(e1.getCreationDate()));
			  SweqxEquifax currentRegister = listquifax.get(0);
			  newInvoice.setShppwsScoreInclusion(new BigDecimal(currentRegister.getEvaluation()));
			  newInvoice.setShppwsSegmentationEqfx(currentRegister.getSegmentation());
		}
		
		newInvoice.setShppwsUser(objOpportunity.getSHPPWSUser());
		newInvoice.setShpicByscspr(objOpportunity.getShppwsOpByproduct());
		newInvoice.setShpicCreditrate("0");
		newInvoice.setShpicPortfolioType("01");
		
		newInvoice.setDescription((String)listMessage.get("Description"));
		Collections.sort(listAmortization, (e1, e2) -> e1.getShppwsPaymentDate().compareTo(e2.getShppwsPaymentDate()));
		EcsfqAmortization nextquotaAmortization = listAmortization.get(0);
		BigDecimal totalQuota = nextquotaAmortization.getAmount();
		newInvoice.setShpicProxdues(nextquotaAmortization.getShppwsPaymentDate());//first quota
		int lastquota = (listAmortization.size())-1;
		EcsfqAmortization lastquotaAmortization = listAmortization.get(lastquota);
		newInvoice.setShpicDuedate(lastquotaAmortization.getShppwsPaymentDate());//last quota
		newInvoice.setShpicCreditrate("0");
		newInvoice.setShpicInterestratemor(BigDecimal.ZERO);
		newInvoice.setShpicOperationState("02");
		newInvoice.setShpicCbpcomers(objOpportunity.getShppwsOpCodecommercial());
		newInvoice.setShpicLockstatus("001");
		newInvoice.setOutstandingAmount(newObjLine.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
		newInvoice.setShpicAdvancevalue(BigDecimal.ZERO);
		newInvoice.setShpicFinancivaluedues(totalQuota);
		newInvoice.setShpicMostoverInstall(totalQuota);
		newInvoice.setShpicMaxnomoradues(new Long(0));//max dias de atraso en cuotas
		newInvoice.setShpicMaxdelay(new Long(0));//Atraso maximo
		newInvoice.setShpicNoduesmorover(new Long(0));//numero de cuota mas vencida
		newInvoice.setShpicLastduespaid(new Long(0));//ultima cuota pagada
		newInvoice.setShpicNoduesover(new Long(0));//num cuotas vencidas
		newInvoice.setShpicMordayscnddelay(new Long(0)); //Dias mora2do atraso
		newInvoice.setShpicNosaleportaf("0"); //N0 Venta de cartera
		newInvoice.setShpicAveragedelay(BigDecimal.ZERO);//Atraso promedio
		newInvoice.setShpicExpirationUpdate(true);
		newInvoice.setShpicPaymentUpdate(true);
		newInvoice.setSscfCreditOperation(objCreditOperation);
		newInvoice.setShpctHomedelivery(objCreditOperation.isShpctHomedelivery());
		newInvoice.setShpctMaps(objCreditOperation.getShpctMaps() != null ? objCreditOperation.getShpctMaps() :"");
		String strValueActivation = (String) listMessage.get("ValueActivation");
		if (strValueActivation != null && !strValueActivation.trim().isEmpty()) {
			try {
				BigDecimal valActivationBD = new BigDecimal(strValueActivation.trim());
				newInvoice.setShppwsValueActivation(valActivationBD);
			} catch (NumberFormatException e) {
				throw new OBException(
						"ERROR: Formato inválido en ValueActivation. Se esperaba número decimal con punto (ej. 10.50). Valor recibido: "
								+ strValueActivation);
			}
		} else {
			newInvoice.setShppwsValueActivation(null);
		}
		message+= "Intenta guardar";
		listMessage.put("message", message);
		OBDal.getInstance().save(newInvoice);
		OBDal.getInstance().flush();
		

		message+= " ############################### Lines ################################";
		listMessage.put("message", message);
		
		Long lineNo = new Long(10);
			InvoiceLine line = OBProvider.getInstance().get(InvoiceLine.class);
			line.setInvoice(newInvoice);
			line.setClient(newInvoice.getClient());
			line.setOrganization(newInvoice.getOrganization());
			line.setLineNo(lineNo);
			line.setProduct(product);
			line.setSprliIdentifier(product.getSearchKey());
			line.setUOM(product.getUOM());
			line.setInvoicedQuantity(new BigDecimal("1"));
			
			line.setUnitPrice(newObjLine.getUnitPrice());
			line.setStandardPrice(newObjLine.getUnitPrice());
			line.setLineNetAmount(newObjLine.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
			line.setListPrice(newObjLine.getUnitPrice());
			line.setTaxAmount(newObjLine.getUnitPrice());
			line.setTax(taxRate);
			line.setSsbodDiscountRate(BigDecimal.ZERO);
			line.setSseedDiscount(BigDecimal.ZERO);
			line.setCostcenter(newInvoice.getCostcenter());
			line.setStDimension(newInvoice.getStDimension());

			OBDal.getInstance().save(line);
			OBDal.getInstance().flush();
			
			message+= " -------- ÉXITO ---------";
			listMessage.put("message", message);
			
			

		return newInvoice;
	}
	
	public List<FIN_PaymentSchedule> InvoicePaymentSchedule(Invoice newInvoice, List<EcsfqAmortization> listAmortization, Map<String, Object> listMessage){
		String message = " ############################### newAmortization ################################";
		listMessage.put("message", message);
		List<FIN_PaymentSchedule> list_PaymentSchedule = new ArrayList<>();
		
		for(EcsfqAmortization objAmortization : listAmortization) {
			FIN_PaymentSchedule newAmortization = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
			newAmortization.setInvoice(newInvoice);
			newAmortization.setClient(newInvoice.getClient());
			newAmortization.setOrganization(newInvoice.getOrganization());
			
			newAmortization.setDueDate(objAmortization.getShppwsPaymentDate());
			newAmortization.setExpectedDate(objAmortization.getShppwsPaymentDate());
			newAmortization.setFinPaymentmethod(newInvoice.getPaymentMethod());
			newAmortization.setNumberOfPayments(objAmortization.getNROCuota());
			newAmortization.setCurrency(newInvoice.getCurrency());
			newAmortization.setShppsCreditNumber(objAmortization.getNROCuota());
			newAmortization.setShppsExpensesCollection(BigDecimal.ZERO);
			newAmortization.setShppsDaysArrears(new Long(0));
			newAmortization.setShppsFeeStatus("PDG");
			
			BigDecimal Capital = objAmortization.getCapital().setScale(2, RoundingMode.HALF_UP);
			newAmortization.setShppsCapital(Capital);//
			BigDecimal Service = objAmortization.getShppwsService();
			newAmortization.setShppsService(Service);
			BigDecimal Attendance = objAmortization.getShppwsAssistance();
			newAmortization.setShppsAttendance(Attendance);
			
			newAmortization.setAmount(Capital);
			newAmortization.setOutstandingAmount(Capital);
			newAmortization.setSwsslNroCuota(objAmortization.getNROCuota());
			newAmortization.setPaidAmount(BigDecimal.ZERO);
			
			newAmortization.setScslTotalquota(objAmortization.getAmount());
			newAmortization.setScslAddservices(objAmortization.getScslAddservices());
			
			message+= " #Capiatal "+ Capital;
			message+= " #Servicio "+ Service;
			message+= " #Asistencia "+ Attendance;
			
			OBDal.getInstance().save(newAmortization);
			OBDal.getInstance().flush();
			
			for(AdditionalServicesOP additionalservOP : objAmortization.getScslAdditionalServicesOPAmortizationIDList()) {
				AdditionalServicesInv newService = OBProvider.getInstance().get(AdditionalServicesInv.class);
				newService.setClient(objAmortization.getClient());
				newService.setOrganization(objAmortization.getOrganization());
				newService.setFinPaymentSchedule(newAmortization);
				newService.setAdditionalservice(additionalservOP.getAdditionalservice());
				newService.setSearchKey(additionalservOP.getSearchKey());
				
				OBDal.getInstance().save(newService);
				OBDal.getInstance().flush();
			}
			
			list_PaymentSchedule.add(newAmortization);
		}
			message+= " -------- ÉXITO 2 ---------";
			listMessage.put("message", message);
			
			return list_PaymentSchedule;
	}
	
	
	public void InvoicePaymentScheduledetail (Invoice newInvoice, List<FIN_PaymentSchedule> list_PaymentSchedule, Map<String, Object> listMessage) {
		
		String message = " ############################### detailAmortization ################################";
		listMessage.put("message", message);
		for(FIN_PaymentSchedule objAmortization : list_PaymentSchedule) {
			FIN_PaymentScheduleDetail detailAmortization = OBProvider.getInstance().get(FIN_PaymentScheduleDetail.class);
			detailAmortization.setClient(objAmortization.getClient());
			detailAmortization.setOrganization(objAmortization.getOrganization());
			detailAmortization.setInvoicePaymentSchedule(objAmortization);
			detailAmortization.setBusinessPartner(newInvoice.getBusinessPartner());
			detailAmortization.setAmount(objAmortization.getAmount().setScale(2, RoundingMode.HALF_UP));
			
			OBDal.getInstance().save(detailAmortization);
			OBDal.getInstance().flush();
		}
		message+= " -------- ÉXITO 2 ---------";
		listMessage.put("message", message);
	}
	
	
	public Invoice serviceSalesInvoiceProvider(shppws_config accesApi, Product product,TaxRate taxRate, Opcrmopportunities objOpportunity, List<EcsfqAmortization> listAmortization, Map<String, Object> listMessage, SscfCreditOperation objCreditOperation, Invoice newInvoice) throws Exception {
		String message=" llega a la func";
		String newmessage = "";
		listMessage.put("message", message);
		Invoice newInvoiceProvider = OBProvider.getInstance().get(Invoice.class);
		newInvoiceProvider.setSalesTransaction(true);
		newInvoiceProvider.setClient(accesApi.getClient());
		newInvoiceProvider.setOrganization(accesApi.getOrganization());
		newInvoiceProvider.setInvoiceDate(objOpportunity.getCreationDate());
		newInvoiceProvider.setAccountingDate(objOpportunity.getCreationDate());
		newInvoiceProvider.setDocumentStatus("DR");//DR
		newInvoiceProvider.setDocumentAction("CO");
		newInvoiceProvider.setProcessed(false);
		newInvoiceProvider.setProcessNow(false);
		message+= " primeros pasos de InvoiceP";
		listMessage.put("message", message);
		DocumentType DocType = accesApi.getFACInvoiceDocumenttype();

		newInvoiceProvider.setShpcfTypeatt(accesApi.getShpcfTypeattcredit());
		
		newInvoiceProvider.setDocumentType(DocType);
		newInvoiceProvider.setTransactionDocument(DocType);
		/*//Viejo
	    	Sequence numberSequence = DocType.getDocumentSequence();
	    	String newNumber=numberSequence.getPrefix()+""+numberSequence.getNextAssignedNumber();
	    	Long nextValueSequence = numberSequence.getNextAssignedNumber();
	    	Long increment = numberSequence.getIncrementBy();
	    	nextValueSequence = nextValueSequence + increment;
		    numberSequence.setNextAssignedNumber(nextValueSequence);*/
		//Nuevo
		ConnectionProvider conn = new DalConnectionProvider(false);
		org.openbravo.base.secureApp.VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
		String newInvoiceNo = Utility.getDocumentNo(conn.getConnection(), conn, vars, "",Invoice.ENTITY_NAME, DocType.getId(), DocType.getId(), false, true);
		
		newInvoiceProvider.setDocumentNo(newInvoiceNo);
		newInvoiceProvider.setOrderReference(newInvoice.getDocumentNo());
		
		BusinessPartner objPartner=objOpportunity.getShppwsOpCodecommercial();
		
		FIN_PaymentMethod paymentMethod=objPartner.getPOPaymentMethod();
		PaymentTerm paymentTerms = objPartner.getPOPaymentTerms();
		Currency curren = objPartner.getCurrency();
		PriceList priceList = objPartner.getPurchasePricelist();
		newInvoiceProvider.setBusinessPartner(objPartner);
		if (paymentMethod == null || paymentTerms == null || curren == null || priceList == null ) {
			newInvoiceProvider.setPaymentMethod(accesApi.getSupplierFinPaymentmethod());//
			newInvoiceProvider.setPaymentTerms(accesApi.getPaymentterm());//
			newInvoiceProvider.setCurrency(accesApi.getSupplierMPricelist().getCurrency());
		    newInvoiceProvider.setPriceList(accesApi.getSupplierMPricelist());
		} else {
			newInvoiceProvider.setPaymentMethod(paymentMethod);//
			newInvoiceProvider.setPaymentTerms(paymentTerms);//
			newInvoiceProvider.setCurrency(curren);
		    newInvoiceProvider.setPriceList(priceList);
		}
		newInvoiceProvider.setCostcenter(accesApi.getCostCenter());
		message+= " segundos pasos de InvoiceP";
		listMessage.put("message", message);
		
		
		
		OBCriteria<Location> queryLocation = OBDal.getInstance().createCriteria(Location.class);
	    queryLocation.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, objPartner));//
	    List<Location> listLocation = queryLocation.list();
	    if (!listLocation.isEmpty()) {
	    	Location objLocation = listLocation.get(0);
	    	newInvoiceProvider.setPartnerAddress(objLocation);
    	}
	    
	    message+= " terceros pasos de InvoiceP";
		listMessage.put("message", message);
		
		message+= " cuartos pasos de InvoiceP";
		listMessage.put("message", message);
		newInvoiceProvider.setSpincoTaxid(objOpportunity.getTAXBpartner());
		newInvoiceProvider.setShppwsMailProvider((String)listMessage.get("Imei"));
		newInvoiceProvider.setShpicImei((String)listMessage.get("Imei"));
		newInvoiceProvider.setShppwsUserProvider(objOpportunity.getSHPPWSUser());
		newInvoiceProvider.setShppwsUser(objOpportunity.getSHPPWSUser());
		EcsfqAmortization objlistAmortization = listAmortization.get(0);
		BigDecimal sumaryQuota = objlistAmortization.getCapital().add(objlistAmortization.getShppwsAssistance()).add(objlistAmortization.getShppwsService());
		newInvoiceProvider.setShpicFinancivaluedues(sumaryQuota);
		newInvoiceProvider.setSHPPWSScore(new BigDecimal(objCreditOperation.getShppwsScore()));
		newInvoiceProvider.setShppwsTerm(new BigDecimal(listAmortization.size()));
		newInvoiceProvider.setShpicNodues(new Long(listAmortization.size()));//
		newInvoiceProvider.setShpicEntrance(objCreditOperation.getInputValue());
		newInvoiceProvider.setShpicAgency(objOpportunity.getShppwsOpAgencycode());
		
		newInvoiceProvider.setShppwsEndSegment(objCreditOperation.getShppwsSegment());
		newInvoiceProvider.setShpicProduct(objOpportunity.getShppwsOpProductcode());
		OBCriteria<SweqxEquifax> queryequifax= OBDal.getInstance().createCriteria(SweqxEquifax.class);
		queryequifax.add(Restrictions.eq(SweqxEquifax.PROPERTY_BUSINESSPARTNER, objPartner));//
		List<SweqxEquifax> listquifax = queryequifax.list();
		if(listquifax.size() > 0) {
			  Collections.sort(listquifax, (e1, e2) -> e2.getCreationDate().compareTo(e1.getCreationDate()));
			  SweqxEquifax currentRegister = listquifax.get(0);
			  newInvoiceProvider.setShppwsScoreInclusion(new BigDecimal(currentRegister.getEvaluation()));
		}
		
		newInvoiceProvider.setShpicByscspr(objOpportunity.getShppwsOpByproduct());
		
		newInvoiceProvider.setDescription((String)listMessage.get("Description"));
		Collections.sort(listAmortization, (e1, e2) -> e1.getShppwsPaymentDate().compareTo(e2.getShppwsPaymentDate()));
		EcsfqAmortization quotaAmortization = listAmortization.get(0);
		newInvoiceProvider.setShpicProxdues(quotaAmortization.getShppwsPaymentDate());//first quota
		int lastquota = (listAmortization.size())-1;
		EcsfqAmortization lastquotaAmortization = listAmortization.get(lastquota);
		newInvoiceProvider.setShpicDuedate(lastquotaAmortization.getShppwsPaymentDate());//last quota
		
		newInvoiceProvider.setShpicCreditrate("0");
		newInvoiceProvider.setShpicInterestratemor(BigDecimal.ZERO);
		newInvoiceProvider.setShpicOperationState("02");
		newInvoiceProvider.setShpicCbpcomers(objOpportunity.getShppwsOpCodecommercial());
		newInvoiceProvider.setShpicLockstatus("001");
		newInvoiceProvider.setShpicCreditrate("0");
		newInvoiceProvider.setShpicPortfolioType("01");
		
		message+= " Invoice Provider";
		listMessage.put("message", message);
		newInvoiceProvider.setSalesTransaction(false);
		newInvoiceProvider.setSswhIseinvoice(true);
		
		/*//Ref de retención
		Long currentSequence = accesApi.getSequence();
		currentSequence++;
		String formattedInvoiceNumber1 = String.format("%03d-%03d-%06d", 1, 1, currentSequence);
		newInvoiceProvider.setSswhWithholdingref(formattedInvoiceNumber1);
		accesApi.setSequence(currentSequence);*/
		
		newInvoiceProvider.setSswhLivelihood(accesApi.getLivelihood());//Tipo de comprobante
		newInvoiceProvider.setSswhCodelivelihood(accesApi.getCodelivelihood());//Codigo de sustento
		
		try {
		message+= " Intenta guardar: ";
		listMessage.put("message", message);
		OBDal.getInstance().save(newInvoiceProvider);
		OBDal.getInstance().flush();
		OBDal.getInstance().save(accesApi);
		OBDal.getInstance().flush();
		message+= " guarda";
		listMessage.put("message", message);
		}catch(Exception e) {
			newmessage+=e.getMessage();
			listMessage.put("message", message + newmessage);
		}
		
		
		message+= " ############################### Lines Provider ################################";
		listMessage.put("message", message);
		
		Long lineNo = new Long(10);
			InvoiceLine line = OBProvider.getInstance().get(InvoiceLine.class);
			line.setInvoice(newInvoiceProvider);
			line.setClient(newInvoiceProvider.getClient());
			line.setOrganization(newInvoiceProvider.getOrganization());
			line.setLineNo(lineNo);
			line.setProduct(product);
			line.setSprliIdentifier(product.getSearchKey());
			line.setUOM(product.getUOM());
			line.setInvoicedQuantity(new BigDecimal("1"));
			
			OBCriteria<InvoiceLine> queryobjInvoiceLine = OBDal.getInstance().createCriteria(InvoiceLine.class);
			queryobjInvoiceLine.add(Restrictions.eq(InvoiceLine.PROPERTY_INVOICE, newInvoice));
	    	List<InvoiceLine> listobjInvoiceLine = queryobjInvoiceLine.list();
	    	InvoiceLine newobjInvoiceLine =listobjInvoiceLine.get(0);
			
			line.setUnitPrice(newobjInvoiceLine.getUnitPrice());
			line.setStandardPrice(newobjInvoiceLine.getUnitPrice());
			line.setLineNetAmount(newobjInvoiceLine.getUnitPrice());
			line.setListPrice(newobjInvoiceLine.getUnitPrice());
			line.setTaxAmount(newobjInvoiceLine.getUnitPrice());
			line.setTax(taxRate);
			line.setSsbodDiscountRate(BigDecimal.ZERO);
			line.setSseedDiscount(BigDecimal.ZERO);
			line.setCostcenter(newInvoiceProvider.getCostcenter());
			line.setStDimension(newInvoiceProvider.getStDimension());
			line.setExcludeforwithholding(true);

			try {
			OBDal.getInstance().save(line);
			OBDal.getInstance().flush();
			}catch(Exception e) {
				newmessage+=e.getMessage();
			}
			listMessage.put("message", message);
		
		return newInvoiceProvider;
		
	}
	
	



}
