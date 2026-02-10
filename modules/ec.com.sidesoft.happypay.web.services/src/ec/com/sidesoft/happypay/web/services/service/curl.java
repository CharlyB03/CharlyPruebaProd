package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.financialmgmt.tax.TaxCategory;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.web.WebService;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.credit.factory.SscfCallCenterTask;
import ec.com.sidesoft.credit.factory.SscfCom1;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.credit.factory.SscfExpense;
import ec.com.sidesoft.credit.factory.SscfIncome;
import ec.com.sidesoft.credit.factory.SscfPersonalReference;
import ec.com.sidesoft.credit.factory.maintenance.CivilStatus;
import ec.com.sidesoft.credit.factory.maintenance.Profession;
import ec.com.sidesoft.credit.factory.maintenance.Relationship;
import ec.com.sidesoft.credit.factory.maintenance.SSCFMNSex;
import ec.com.sidesoft.credit.simulator.AdditionalServicesOP;
import ec.com.sidesoft.credit.simulator.scsl_Byproducts;
import ec.com.sidesoft.credit.simulator.scsl_Creditservices;
import ec.com.sidesoft.credit.simulator.scsl_Product;
import ec.com.sidesoft.customer.exception.Ecsce_CustomerExcepSub;
import ec.com.sidesoft.customer.exception.Ecsce_CustomerException;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.fast.quotation.EcsfqAmortization;
import ec.com.sidesoft.fast.quotation.QuotationLine;
import ec.com.sidesoft.happypay.customizations.shpctBinnacle;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.pev.credit.Shppec_ExpActual;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_Quotas;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_Verification;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_q_byproducts;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.shppws_monitor;
import ec.com.sidesoft.happypay.web.services.monitor.MonitorManager;
import ec.com.sidesoft.localization.geography.secpm_canton;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class curl implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
	}

	
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		//OBContext.setAdminMode(true);
		JSONObject jsonMonitor = new JSONObject();
		jsonMonitor.put("SHPPWS_SideSoft_Curl", "Service"+0);
	    jsonMonitor.put("startSHPPWS_SideSoft_Curl", LocalDateTime.now());
	    jsonMonitor.put("typeSHPPWS_SideSoft_Curl", "Enrolamiento");
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		
		// POST cuerpo de la solicitud
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        JSONObject requestJSON = new JSONObject(requestBody.toString());
        String requestParameters = requestJSON.toString();
	Scactu_Log log = logger.log_start_register(accesApi, "curl", requestParameters);
        
        
        String No_Opportunity = requestJSON.getString("No_Opportunity");
		String Identifier = requestJSON.getString("Identifier");
		String Amount_Finance = requestJSON.getString("Amount_Finance");
		String Input_Value = requestJSON.getString("Input_Value");
		String PVP_Value = requestJSON.getString("PVP_Value");
		//String Term = requestJSON.getString("Term");
		//String Code_Product = requestJSON.getString("Code_Product");
		String Term_Code = requestJSON.getString("Term_Code");
		String Birthdate = requestJSON.getString("Birthdate");
		String Surnames = requestJSON.getString("Surnames");
		String Names = requestJSON.getString("Names");
		String Gender = requestJSON.getString("Gender");
		String Civil_Status = requestJSON.getString("Civil_Status");
		String String_Profession = requestJSON.getString("Profession");
		String Province = requestJSON.getString("Province");
		String Main_Street_domic = requestJSON.getString("Main_Street_domic");
		String No_Home = requestJSON.getString("No_Home");
		String Cross_Street = requestJSON.getString("Cross_Street");
		String Cell_Phone = requestJSON.getString("Cell_Phone");
		
		// Campos opcionales para actualización de información del crédito (#20834)
		String Code_Commerce = optStringWithTypeValidation(requestJSON, "Code_Commerce", "");
		String Code_Agency = optStringWithTypeValidation(requestJSON, "Code_Agency", "");
		String City_store_group = optStringWithTypeValidation(requestJSON, "City_store_group", "");
		String Province_store_group = optStringWithTypeValidation(requestJSON, "Province_store_group", "");

		String Arrangement_Ref_Personal = requestJSON.getString("Arrangement_Ref_Personal");
		String Economic_Activity = requestJSON.getString("Economic_Activity");
		String Type_Of_Dependent = requestJSON.getString("Type_Of_Dependent");
		String Type_Of_Independent = requestJSON.getString("Type_Of_Independent");
		String Company = requestJSON.getString("Company");
		String RUC = requestJSON.getString("RUC");
		String Address = requestJSON.getString("Address");
		String Income = requestJSON.getString("Income");
		String Other_Income = requestJSON.getString("Other_Income");
		String Expenses = requestJSON.getString("Expenses");
		String Other_Expenses = requestJSON.getString("Other_Expenses");
		String Mail = requestJSON.getString("Mail");
		String User = requestJSON.getString("User");
		String City = requestJSON.getString("City");
		Boolean Venta_a_Domicilio = requestJSON.has("Venta_a_Domicilio") ?  requestJSON.getBoolean("Venta_a_Domicilio") : false;
		String Ubicacion_Maps = requestJSON.has("Ubicacion_Maps")? requestJSON.getString("Ubicacion_Maps"):"";
		
		String message = "Error";
		String fastquota_id ="";
		Long score_quotas = new Long(0);
		
		Opcrmopportunities objOpportunity = null;
		String Identifier2 = "";
		
		try {

	    OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance().createCriteria(Opcrmopportunities.class);
    	queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
    	List<Opcrmopportunities> listOpportunity =queryOpportunity.list();
    	
    	if (listOpportunity.size() > 0) {
    		objOpportunity = listOpportunity.get(0);
    		
    		OBCriteria<ECSFQ_Quotation> queryQuotation = OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
    		queryQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_DOCUMENTNO, No_Opportunity));
    		List<ECSFQ_Quotation> listAmortization = queryQuotation.list();
    		
			if (listAmortization.size() == 0) {
			try {
				updateCreditInfoInEnrollment(objOpportunity, Code_Commerce, Code_Agency, City_store_group, Province_store_group, User, RUC);
			} catch (Exception e) {
				saveBinnacleUpdate(objOpportunity, e.getMessage());
				
				JSONArray ArrayData = new JSONArray();
				JSONObject JSONObj = new JSONObject();
				JSONObj.put("Status", "Error");
				JSONObj.put("message", "Error en actualización de información del crédito: " + e.getMessage());
				JSONObj.put("Identifier", Identifier);
				ArrayData.put(JSONObj);
				
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				String json = ArrayData.toString();
				PrintWriter writer = response.getWriter();
				writer.write(json);
				writer.close();
				
				return;
			}
		}
    	}

		JSONObject jsonResponse = generateJsonResponse(Identifier, No_Opportunity);

		// Verificamos si "data" contiene un array
		if (jsonResponse.has("data") && jsonResponse.get("data") instanceof JSONArray) {
			JSONArray dataArray = jsonResponse.getJSONArray("data");

			// Verificamos si el array no está vacío
			if (dataArray.length() > 0) {
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResponse.toString()); // Enviamos el objeto JSON con el array bajo "data"
				writer.close();
				return;
			}
		}
		
	    	

		if (listOpportunity.size() > 0) {
			objOpportunity = listOpportunity.get(0);
			String op_store_Code = objOpportunity.getShppwsOpShopgroup();
			String op_end_segment = objOpportunity.getShppwsOpEndsegment();
			BusinessPartner partner = objOpportunity.getBusinessPartner();
			Identifier2 = objOpportunity.getTAXBpartner();
			Map<String, Object> listMessage = new HashMap<>();
	
			OBCriteria<ECSFQ_Quotation> qExistQuotation= OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
			qExistQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_DOCUMENTNO, No_Opportunity));
	    	List<ECSFQ_Quotation> listquotation =qExistQuotation.list();
	    	if(listquotation.size()>0) {
		        throw new Exception("Duplicated ECSFQ_Quotation");
	    	}
			  //#################################
			 // Creación de la Cotización rápida
			//###################################
			ECSFQ_Quotation objFastQuotation = OBProvider.getInstance().get(ECSFQ_Quotation.class);
			String newNumber = "";
			try {
				objFastQuotation.setOrganization(accesApi.getOrganization());
				objFastQuotation.setActive(accesApi.isActive());
				objFastQuotation.setCreatedBy(accesApi.getCreatedBy());
				objFastQuotation.setUpdatedBy(accesApi.getUpdatedBy());
				objFastQuotation.setOpcrmOpportunities(objOpportunity);
				objFastQuotation.setBusinessPartner(partner);
				objFastQuotation.setTransactionDocument(accesApi.getOPDocumentType());
				OBCriteria<Location> queryLocation = OBDal.getInstance().createCriteria(Location.class);
				queryLocation.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, partner));
				List<Location> listLocation = queryLocation.list();
	
				if (!listLocation.isEmpty()) {// Update Address
					Location objLocation = listLocation.get(0);
					objLocation.setName(Main_Street_domic + " - " + No_Home + " " + Cross_Street + " - " + Province);
					org.openbravo.model.common.geography.Location auxLocation = org.openbravo.base.provider.OBProvider
							.getInstance().get(org.openbravo.model.common.geography.Location.class);
					secpm_canton objCanton = new secpm_canton();// Ciudad
					Region objProvincia = new Region();// Provincia
					Country objPais = new Country();// País
					try {
						// Cantón-Ciudad
						OBCriteria<secpm_canton> queryCanton = OBDal.getInstance().createCriteria(secpm_canton.class);
						queryCanton.add(Restrictions.eq(secpm_canton.PROPERTY_NAME, City));
						List<secpm_canton> listCanton = queryCanton.list();
						objCanton = listCanton.get(0);
						objProvincia = objCanton.getRegion();
						objPais = objProvincia.getCountry();
					} catch (Exception e) {
						OBCriteria<secpm_canton> queryCanton = OBDal.getInstance().createCriteria(secpm_canton.class);
						queryCanton.add(Restrictions.eq(secpm_canton.PROPERTY_NAME, "QUITO"));
						List<secpm_canton> listCanton = queryCanton.list();
						objCanton = listCanton.get(0);
						objProvincia = objCanton.getRegion();
						objPais = objProvincia.getCountry();
					}
	
					String auxAddressLine1 = Main_Street_domic + " - " + No_Home + " - " + Cross_Street;
	
					OBCriteria<org.openbravo.model.common.geography.Location> queryVerifyLocation = OBDal.getInstance().createCriteria(org.openbravo.model.common.geography.Location.class);
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_CLIENT, accesApi.getClient()));
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_ORGANIZATION,accesApi.getOrganization()));
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_ADDRESSLINE1, auxAddressLine1));
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_CITYNAME, objProvincia.getName()));
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_COUNTRY, objPais));
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_REGION, objProvincia));
					queryVerifyLocation.add(Restrictions.eq(org.openbravo.model.common.geography.Location.PROPERTY_SCRMACTCANTON, objCanton));
					List<org.openbravo.model.common.geography.Location> listVerifyLocation = queryVerifyLocation.list();
	
					if (listVerifyLocation.size() <= 0) {
						auxLocation.setClient(accesApi.getClient());
						auxLocation.setOrganization(accesApi.getOrganization());
						auxLocation.setAddressLine1(auxAddressLine1);
						auxLocation.setCityName(objProvincia.getName());
						auxLocation.setCountry(objPais);
						auxLocation.setRegion(objProvincia);
						auxLocation.setScrmactCanton(objCanton);
	
						OBDal.getInstance().save(auxLocation);
						OBDal.getInstance().flush();
					} else {
						auxLocation = listVerifyLocation.get(0);
					}
	
					objLocation.setLocationAddress(auxLocation);
	
					OBDal.getInstance().save(objLocation);
					OBDal.getInstance().flush();
					message += "objLocation";
	
					objFastQuotation.setPartnerAddress(objLocation);
				}
	
				objFastQuotation.setDocumentNo(objOpportunity.getShppwsOpDocumentno());
	
				OBDal.getInstance().save(objFastQuotation);
				OBDal.getInstance().flush();
				fastquota_id = objFastQuotation.getId();
				// OBDal.getInstance().commitAndClose();
			} catch (Exception e) {
				throw new Exception("Hubo un error al generar la Cotización rápida "+ e.getMessage());
			}
	
			  //######################################################## 
			 // Creación de amortización  Creación del plan de pagos
			//#########################################################
			List<scsl_Byproducts> byProducts = new ArrayList<>();
			
			if(!ifExistExceptionClient(Identifier, objOpportunity)) {
				List<shppee_q_byproducts> listQbyProducts = new ArrayList<>();
				try { // Preparo los subproductos de crédito
					if (!No_Opportunity.isEmpty()) {
						scsl_Product codeProd = objOpportunity.getShppwsOpProductcode();
						String productID = codeProd.getId();
						codeProd = OBDal.getInstance().get(scsl_Product.class, productID);
						String codeProduct = codeProd.getValidationCode();
						String storeGroup = objOpportunity.getShppwsOpShopgroup();
						String endSegment = objOpportunity.getShppwsOpEndsegment();
						OBCriteria<shppee_Quotas> matriz1 = OBDal.getInstance().createCriteria(shppee_Quotas.class);
						List<shppee_Quotas> quotasScores = matriz1.list();
						String aux = "";
						for (shppee_Quotas quotasScore : quotasScores) {
							scsl_Product objProduct = quotasScore.getScslProduct();
							String product = objProduct.getValidationCode();
							if (codeProduct.equals(product)) {
								if (storeGroup.equals(quotasScore.getAgencyCode())) {
									if (endSegment.equals(quotasScore.getENDSegment())) {
										score_quotas = quotasScore.getQuota();
										OBCriteria<shppee_q_byproducts> queryQbyProducts = OBDal.getInstance()
												.createCriteria(shppee_q_byproducts.class);
										queryQbyProducts.add(
												Restrictions.eq(shppee_q_byproducts.PROPERTY_SHPPEEQUOTAS, quotasScore));
										listQbyProducts = queryQbyProducts.list();
									}
								}
							}
						}
					}
				} catch (Exception e) {
					Error = e.getMessage();
				}
		
				int size_qbyproducts = listQbyProducts.size();
				if (size_qbyproducts > 0) {
					for (shppee_q_byproducts QbyProduct : listQbyProducts) {
						byProducts.add(QbyProduct.getScslByproducts());
					}
				}
			} else {
				byProducts = byProdsExceptionClient(Identifier, objOpportunity, Term_Code);
			}
			
			int size_byproducts = byProducts.size();
	
			List<EcsfqAmortization> listAmortization = new ArrayList<>();
			BigDecimal totalDeuda = BigDecimal.ZERO;
			try {
				if (size_byproducts > 0) {
					Double amountFinance = new Double(Amount_Finance);
					int noQuota = 0;
					for (scsl_Byproducts byProduct : byProducts) {
						if (Term_Code.equals(byProduct.getValidationCode())) {
							// old calculated total amount
							Long deadLine = byProduct.getDeadLine();
							BigDecimal comisionFactor = byProduct.getCommissionFactor().divide(BigDecimal.valueOf(100));
							BigDecimal comision = comisionFactor.multiply(BigDecimal.valueOf(amountFinance));
							totalDeuda = BigDecimal.valueOf(amountFinance).add(comision);
	
							scsl_Product productscsl = OBDal.getInstance().get(scsl_Product.class,byProduct.getScslProduct().getId());
	
							OBCriteria<scsl_Creditservices> queryCreditServices = OBDal.getInstance().createCriteria(scsl_Creditservices.class);
							queryCreditServices.add(Restrictions.eq(scsl_Creditservices.PROPERTY_SCSLPRODUCT, productscsl));
							List<scsl_Creditservices> listobjCreditServices = queryCreditServices.list();
							scsl_Creditservices objCreditServices = listobjCreditServices.get(0);
							scsl_Creditservices objCreditAsistance = listobjCreditServices.get(0);
	
							for (scsl_Creditservices objCreditService : listobjCreditServices) {
								String auxname = objCreditService.getSwsslTypeprod() != null ? objCreditService.getSwsslTypeprod(): "";;// getCommercialName
								if (auxname.equals("SEV")) {// Servicio
									objCreditServices = objCreditService;
								} else if (auxname.equals("ATT")) {// Asistencia
									objCreditAsistance = objCreditService;
								}
							}
	
							// Preparing values
							Double netbyentrance = new Double(Input_Value); // entrada
							Double netbyAmountFinance = amountFinance; // monto a financiar
							Double byproduct_deadline = new Double(byProduct.getDeadLine()); // subProducto plazo
							Double factorAsistance_productCredit = objCreditAsistance.getFactor().doubleValue(); 
							Double factorService_productCredit = objCreditServices.getFactor().doubleValue(); 
							Double factorComision_byproduct = byProduct.getCommissionFactor().doubleValue();
	
							// new total amount calculated and fee
							Double preAmount = netbyAmountFinance * (factorComision_byproduct / 100);
							Double assistance = preAmount * (factorAsistance_productCredit / 100);
							Double service = preAmount * (factorService_productCredit / 100);
							BigDecimal value_capital = new BigDecimal(netbyAmountFinance).setScale(2,RoundingMode.HALF_UP); // RoundingMode.HALF_UP
							BigDecimal value_assistance = new BigDecimal(assistance).setScale(2, RoundingMode.HALF_UP);// RoundingMode.HALF_UP
							BigDecimal value_service = new BigDecimal(service).setScale(2, RoundingMode.HALF_UP);// RoundingMode.HALF_UP
							totalDeuda = value_capital.add(value_assistance).add(value_service);
							BigDecimal cuotaTotal = totalDeuda.divide(BigDecimal.valueOf(deadLine),RoundingMode.HALF_UP);
							listMessage.put("newAssistanceTotal", assistance);
							listMessage.put("newServiceTotal", service);
	
							// Date
							String Frequencytime = productscsl.getFrequency();
							Date paymentDate = new Date();
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(paymentDate);
							if(requestJSON.has("Servicios_Adicionales")) {
								JSONArray Servicios_Adicionales = requestJSON.getJSONArray("Servicios_Adicionales");
								validateadditionalServ(Servicios_Adicionales);
							}
							listMessage.put("additionalService", false);
							listMessage.put("additionalServiceAmt", BigDecimal.ZERO);
							
							for (int i = 0; i < deadLine; i++) {
								noQuota += 1;
								if (Frequencytime.equals("WK")) {
									calendar.add(Calendar.DAY_OF_MONTH, 7); // Sumar 7 días a la fecha
								} else if (Frequencytime.equals("MT")) {
									calendar.add(Calendar.DAY_OF_MONTH, 30); // Sumar 30 días a la fecha
								} else if (Frequencytime.equals("BK")) {
									calendar.add(Calendar.DAY_OF_MONTH, 15); // Sumar 15 días a la fecha
								}
	
								Date newPaymentDate = calendar.getTime();
								EcsfqAmortization objAmortization = CreatePaymentPlan(productscsl,requestJSON, noQuota, newPaymentDate,
										netbyentrance, cuotaTotal, netbyAmountFinance, byproduct_deadline,
										factorAsistance_productCredit, factorService_productCredit,
										factorComision_byproduct, objFastQuotation, listMessage);
								listAmortization.add(objAmortization);
							}
							
							if((boolean) listMessage.get("additionalService")) {
								BigDecimal additionalServiceAmt = (BigDecimal) listMessage.get("additionalServiceAmt");
								totalDeuda = totalDeuda.add(additionalServiceAmt);
							}
	
							if (listAmortization.size() > 0) {
								Collections.sort(listAmortization,
										(e1, e2) -> e2.getNROCuota().compareTo(e1.getNROCuota()));
								EcsfqAmortization currentRegister = listAmortization.get(0);
								balanceAmortization(listAmortization, netbyAmountFinance, listMessage, totalDeuda);
							}
	
							try {
								objOpportunity.setShppwsOpByproduct(byProduct);
								objOpportunity.setSHPPWSUser(User);
								OBDal.getInstance().save(objOpportunity);
								OBDal.getInstance().flush();
							} catch (Exception e) {
							}
						}
					}
					// OBDal.getInstance().commitAndClose();
				}else {
					throw new Exception("No aplica a ningun subproducto. ");
				}
	
			} catch (Exception e) {
		        throw new Exception("No crea Plan de pagos "+e.getMessage());
			}
	
			  //#####################################
			 // Creación de las líneas
			//######################################
			QuotationLine objLine = OBProvider.getInstance().get(QuotationLine.class);
			try {
				scsl_Product products = objOpportunity.getShppwsOpProductcode();
	
				objLine.setClient(accesApi.getClient());
				objLine.setOrganization(accesApi.getOrganization());
				objLine.setActive(accesApi.isActive());
				objLine.setCreatedBy(accesApi.getCreatedBy());
				objLine.setUpdatedBy(accesApi.getUpdatedBy());
	
				objLine.setEcsfqOrder(objFastQuotation);
				objLine.setBusinessPartner(partner);
				Long valLong = new Long(10);
				objLine.setLineNo(valLong);
	
				objLine.setProduct(products.getProduct());//
				objLine.setOrderedQuantity(accesApi.getOrderedQuantity());
				objLine.setUOM(accesApi.getUOM());
				Product pCategory = products.getProduct();
	
				// Problema Tax
				OBCriteria<Product> queryProduct = OBDal.getInstance().createCriteria(Product.class);
				queryProduct.add(Restrictions.eq(Product.PROPERTY_ID, pCategory.getId()));
				Product newpCategory = (Product) queryProduct.uniqueResult();
				TaxCategory idCategory = newpCategory.getTaxCategory();
				OBCriteria<TaxRate> queryTax = OBDal.getInstance().createCriteria(TaxRate.class);
				queryTax.add(Restrictions.eq(TaxRate.PROPERTY_TAXCATEGORY, idCategory));
				List<TaxRate> idTaxs = queryTax.list();
				for (TaxRate idTax : idTaxs) {
					TaxRate c_tax = OBDal.getInstance().get(TaxRate.class, idTax.getId());
					objLine.setTax(c_tax);
				}
				Double amount = new Double(PVP_Value); // precio unitario
				Double entrance = new Double(Input_Value); // entrada
				Double finalValue = amount - entrance;
				BigDecimal bigValue = new BigDecimal(finalValue);
				objLine.setUnitPrice(bigValue);
	
				// Problema setLineNetAmount
				objLine.setLineNetAmount(bigValue);
				OBDal.getInstance().save(objLine);
				OBDal.getInstance().flush();
			} catch (Exception e) {
		        throw new Exception("Hubo un error al crear las líneas "+e.getMessage());
			}
	
			  //###############################################
			 // Creación de la Operación de Crédito
			//################################################
			// OBDal.getInstance().commitAndClose();
			SscfCreditOperation objCreditOperations = OBProvider.getInstance().get(SscfCreditOperation.class);
			try {
				objCreditOperations.setClient(accesApi.getClient());
				objCreditOperations.setOrganization(accesApi.getOrganization());
				objCreditOperations.setActive(accesApi.isActive());
				objCreditOperations.setCreatedBy(accesApi.getCreatedBy());
				objCreditOperations.setUpdatedBy(accesApi.getUpdatedBy());
	
				objCreditOperations.setBusinessPartner(partner);
				BigDecimal financedValue = new BigDecimal(Amount_Finance);
				objCreditOperations.setFinancedValue(financedValue);
				objCreditOperations.setProfile("ECSFQ_Quotation");
				objCreditOperations.setCustomerType("R");
				scsl_Product product = objOpportunity.getShppwsOpProductcode();
				Product originProduct = product.getProduct();
				ProductCategory productCategory = originProduct.getProductCategory();
				objCreditOperations.setProductCategory(productCategory);
				Date currentDate = new Date();
				objCreditOperations.setProcessStartDate(currentDate);
				objCreditOperations.setCom1Status("Y");
				objCreditOperations.setDocumentStatus("IP");
				objCreditOperations.setAnalysisType(accesApi.getSscfAnalysisType());
				objCreditOperations.setCom1User(accesApi.getCreatedBy());
				objCreditOperations.setArtboardStatus("COM1");				
				objFastQuotation = OBDal.getInstance().get(ECSFQ_Quotation.class, fastquota_id);
				objCreditOperations.setSscorOrder(objFastQuotation);
				objCreditOperations.setDocumentNo(objFastQuotation.getDocumentNo());
				objCreditOperations.setShppwsSegment(objOpportunity.getShppwsOpEndsegment());
				OBCriteria<SweqxEquifax> queryequifax = OBDal.getInstance().createCriteria(SweqxEquifax.class);
				queryequifax.add(Restrictions.eq(SweqxEquifax.PROPERTY_BUSINESSPARTNER, partner));
				List<SweqxEquifax> equifaxs = queryequifax.list();
				int sizequifax = equifaxs.size();
				if (sizequifax > 0) {
					Collections.sort(equifaxs, (e1, e2) -> e2.getCreationDate().compareTo(e1.getCreationDate()));
					SweqxEquifax equifaxMasActual = equifaxs.get(0);
					objCreditOperations.setShppwsInclusionScore(equifaxMasActual.getEvaluation());
				}
				objCreditOperations.setShppwsScore(score_quotas);
				BigDecimal Input_ValueDecimal = new BigDecimal(Input_Value);
				objCreditOperations.setInputValue(Input_ValueDecimal);
				BigDecimal Amount_FinanceDecimal = new BigDecimal(Amount_Finance);
				objCreditOperations.setFinancedValue(Amount_FinanceDecimal);
	
				if (listAmortization.size() > 0) {
					Collections.sort(listAmortization, (e1, e2) -> e1.getNROCuota().compareTo(e2.getNROCuota()));
					BigDecimal firstQuota = listAmortization.get(0).getAmount();
					objCreditOperations.setQuotaValue(firstQuota);
				}
	
				OBCriteria<scsl_Byproducts> newquerybyproduct = OBDal.getInstance().createCriteria(scsl_Byproducts.class);
				newquerybyproduct.add(Restrictions.eq(scsl_Byproducts.PROPERTY_VALIDATIONCODE, Term_Code));
				newquerybyproduct.add(Restrictions.eq(scsl_Byproducts.PROPERTY_SCSLPRODUCT, objOpportunity.getShppwsOpProductcode()));
				scsl_Byproducts newObjbyproducts = (scsl_Byproducts) newquerybyproduct.uniqueResult();
				objCreditOperations.setTermMonths(newObjbyproducts.getDeadLine());
				objCreditOperations.setShpctHomedelivery(Venta_a_Domicilio);
				if (Ubicacion_Maps != null && !Ubicacion_Maps.isEmpty()) {
					objCreditOperations.setShpctMaps(Ubicacion_Maps);
				}
	
				if (listAmortization.size() > 0) {
					BigDecimal valImport = BigDecimal.ZERO;
					for (EcsfqAmortization objAmortization : listAmortization) {
						valImport = valImport.add(objAmortization.getAmount());
					}
					objCreditOperations.setValueInvoice(valImport);
				}
				OBDal.getInstance().save(objCreditOperations);
				OBDal.getInstance().flush();
	
			} catch (Exception e) {
		        throw new Exception("NO crea Operación de Crédito "+ e.getMessage());
			}
			
			  //########################
			 // COM-S
			//#########################
			SscfCom1 objComS = OBProvider.getInstance().get(SscfCom1.class);
			CreateCOM_S(requestJSON.getString("No_Opportunity"),objFastQuotation, objComS, accesApi, objCreditOperations, Identifier, Surnames, Names,
					String_Profession, Province, Birthdate, No_Home, Main_Street_domic, Cross_Street, Gender,
					Civil_Status, Cell_Phone, Economic_Activity, Mail, Type_Of_Dependent, Type_Of_Independent, Company,
					RUC, Address, listMessage, Venta_a_Domicilio);
			  //########################
			 // UPDATE PHONE AND EMAIL
			//#########################
			//Partner
			OBCriteria<BusinessPartner> queryBP = OBDal.getInstance().createCriteria(BusinessPartner.class);
			queryBP.add(Restrictions.eq(BusinessPartner.PROPERTY_TAXID, Identifier));
		    BusinessPartner bPartner = queryBP.list().get(0);
		    //location
		    OBCriteria<Location> queryLP = OBDal.getInstance().createCriteria(Location.class);
			queryLP.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER+".id", bPartner.getId()));
			Location lPartner = queryLP.list().get(0);
			updatePartnerInfo(Cell_Phone,Mail,bPartner,lPartner);
			// REF
			JSONArray filesArray = requestJSON.getJSONArray("Arrangement_Ref_Personal");
			// Recorre el Array del JSON
			for (int i = 0; i < filesArray.length(); i++) {
				JSONObject file = filesArray.getJSONObject(i);
				String Ref_Surnames = file.getString("Surnames");
				String Ref_Names = file.getString("Names");
				String Ref_RelationShip = file.getString("RelationShip");
				String Ref_CellPhone = file.getString("CellPhone");
				SscfPersonalReference objPersonalReference = OBProvider.getInstance().get(SscfPersonalReference.class);
				CreatePersonalReference(objPersonalReference, accesApi, objComS, Ref_Surnames, Ref_Names,
						Ref_RelationShip, Ref_CellPhone, listMessage);
			}
			
			 // Income
			//##########################
			SscfIncome objIncome = OBProvider.getInstance().get(SscfIncome.class);
			BigDecimal Income_Salary = new BigDecimal(Income);
			BigDecimal Income_Others = new BigDecimal(Other_Income);
			CreateIncome(objIncome, accesApi, objComS, Income_Salary, Income_Others, listMessage);
			
			  //########################
			 // Expenses
			//##########################
			SscfExpense objExpense = OBProvider.getInstance().get(SscfExpense.class);
			BigDecimal Expense_Rent = new BigDecimal(Expenses);
			BigDecimal Expense_Others = new BigDecimal(Other_Expenses);
			CreateExpense(objExpense, accesApi, objComS, Expense_Rent, Expense_Others, listMessage);
			message = (String) listMessage.get("message");
		}else {
			throw new Exception("No se ha encontrado la oportunidad. Por favor verificar el numero de oportunidad.");
		}
    	}catch(Exception e) {
    		message = "Error";
    		Error = e.getMessage()+"";
    		if(objOpportunity != null) {
    			changeStateOpportunity(objOpportunity, Error);
    		}
    	}
		
		// |||||||||||||||||||||||||||||||||||
		// |||||||||||||RESULTADO|||||||||||||
		// |||||||||||||||||||||||||||||||||||
		ECSFQ_Quotation objFastQuotation = OBProvider.getInstance().get(ECSFQ_Quotation.class);
		objFastQuotation = OBDal.getInstance().get(ECSFQ_Quotation.class, fastquota_id);

		JSONArray ArrayData = new JSONArray();
		JSONObject JSONObj = new JSONObject();
		JSONObj.put("Status", message);
		if (message != null && message.equals("Error")) {
			JSONObj.put("message", Error);
		}
		JSONObj.put("Identifier", Identifier);
		if(objFastQuotation != null) {
			JSONObj.put("Verification_type",type_verify(objFastQuotation, Venta_a_Domicilio));
		}
		ArrayData.put(JSONObj);
			
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = ArrayData.toString();
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
		jsonMonitor.put("endSHPPWS_SideSoft_Curl", LocalDateTime.now());
		String requestUrl = request.getRequestURL().toString();
		

		try {
			String noReference = No_Opportunity;
			String Interface = "SHPPWS_NT";
			String Process = "Enrolamiento";
			String idRegister = objOpportunity != null ? objOpportunity.getId():No_Opportunity;
			logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT",
					Interface, Process, idRegister, Error);
		} catch (Exception e) {
		}

		if (message != null && message.equals("Ok")) {
			jsonMonitor.put("statusSHPPWS_SideSoft_Curl", "200");
		} else {
			jsonMonitor.put("statusSHPPWS_SideSoft_Curl", "500");
		}
		jsonMonitor.put("Identifier", No_Opportunity);
		jsonMonitor.put("Identifier2", Identifier2);

		MonitorManager newMonitor = new MonitorManager();
		newMonitor.sendMonitorData(jsonMonitor, accesApi, true, null);
	        
	}

	
	public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	public EcsfqAmortization CreatePaymentPlan(scsl_Product productscsl, JSONObject requestJSON, int noQuota, Date paymentDate, Double netbyentrance, BigDecimal cuotaTotal, Double netbyAmountFinance, Double byproduct_deadline, Double factorAsistance_productCredit, Double factorService_productCredit, Double factorComision_byproduct, ECSFQ_Quotation objFastQuotation, Map<String, Object> listMessage) throws Exception {
		String message=noQuota+" Instancia EcsfqAmortization ";
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
	    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
	    
	    EcsfqAmortization objAmortization = OBProvider.getInstance().get(EcsfqAmortization.class);
	    //shpop_payment_plan objPaymentPlan = OBProvider.getInstance().get(shpop_payment_plan.class);
	    objAmortization.setClient(accesApi.getClient());
	    objAmortization.setOrganization(accesApi.getOrganization());
	    objAmortization.setActive(accesApi.isActive());
	    objAmortization.setCreatedBy(accesApi.getCreatedBy());
	    objAmortization.setUpdatedBy(accesApi.getUpdatedBy());
	    objAmortization.setEcsfqOrder(objFastQuotation);
	    objAmortization.setShppwsPaymentDate(paymentDate);
	    
		Long value_noQuota = new Long(noQuota); // N0 de cuota
		BigDecimal value_netbyentrance = new BigDecimal(netbyentrance); // Entrada
		BigDecimal value_fee = cuotaTotal; //Cuota //RoundingMode.HALF_UP
			Double capital = netbyAmountFinance / byproduct_deadline; //CAPITAL
			Double preAmount = netbyAmountFinance * (factorComision_byproduct/100);
			Double auxAmounts = preAmount / byproduct_deadline;
			Double assistance = auxAmounts * (factorAsistance_productCredit/100);
			Double service = auxAmounts * (factorService_productCredit/100);
		BigDecimal value_capital=new BigDecimal(capital).setScale(2, RoundingMode.HALF_UP);	//RoundingMode.HALF_UP
		BigDecimal value_assistance = new BigDecimal(assistance).setScale(2, RoundingMode.HALF_UP);//RoundingMode.HALF_UP
		BigDecimal value_service= new BigDecimal(service).setScale(2, RoundingMode.HALF_UP);//RoundingMode.HALF_UP
		
		objAmortization.setNROCuota(value_noQuota);
		objAmortization.setEntrada(value_netbyentrance);
		objAmortization.setAmount(value_capital.add(value_assistance).add(value_service));//value_calculatedAmount
		objAmortization.setCapital(value_capital);
		objAmortization.setShppwsAssistance(value_assistance);
		objAmortization.setShppwsService(value_service);
		
		OBDal.getInstance().save(objAmortization);
		OBDal.getInstance().flush();
		
		newAdditionalServices(objAmortization, requestJSON, productscsl.getId(), listMessage);
			
		listMessage.put("message", message );
		    
		return objAmortization;
	}
	
	public void newAdditionalServices(EcsfqAmortization objAmortization, JSONObject requestJSON, String prodID, Map<String, Object> listMessage) throws Exception {
		if(requestJSON.has("Servicios_Adicionales")) {
			JSONArray Servicios_Adicionales = requestJSON.getJSONArray("Servicios_Adicionales");
			BigDecimal valorAdicional = BigDecimal.ZERO; 
			for (int i = 0; i < Servicios_Adicionales.length(); i++) {
		        JSONObject servicioJSON = Servicios_Adicionales.getJSONObject(i);
		        validateStructure(servicioJSON);
		        String  Name = servicioJSON.getString("Name");
		        BigDecimal Amount = new BigDecimal(servicioJSON.getString("Amount"));
		        scsl_Creditservices objNewServ = serviceInBDD(prodID, Name);
		        valorAdicional = valorAdicional.add(Amount);
		        
		        AdditionalServicesOP newService = OBProvider.getInstance().get(AdditionalServicesOP.class);
				newService.setClient(objAmortization.getClient());
				newService.setOrganization(objAmortization.getOrganization());
				newService.setAmortization(objAmortization);
				newService.setAdditionalservice(objNewServ);
				newService.setSearchKey(Amount);
				
				OBDal.getInstance().save(newService);
				OBDal.getInstance().flush();
		    }
			
			if(valorAdicional.compareTo(BigDecimal.ZERO) > 0) {
				listMessage.put("additionalService", true);
				BigDecimal additionalServiceAmt = (BigDecimal) listMessage.get("additionalServiceAmt");
				listMessage.put("additionalServiceAmt", additionalServiceAmt.add(valorAdicional));
				BigDecimal totalQuotaAmt = objAmortization.getAmount();
				totalQuotaAmt = totalQuotaAmt.add(valorAdicional);
				objAmortization.setAmount(totalQuotaAmt);
				objAmortization.setScslAddservices(valorAdicional);
				OBDal.getInstance().save(objAmortization);
				OBDal.getInstance().flush();
			}
		}
	}
	
	public void balanceAmortization(List<EcsfqAmortization> listAmortization,  Double netbyAmountFinance, Map<String, Object> listMessage, BigDecimal totalDeuda) {
		
		BigDecimal valueAmountFinance = new BigDecimal(netbyAmountFinance);//obtiene capital total
		Double doubleValue = (Double) listMessage.get("newAssistanceTotal");
		BigDecimal valueAmountAssistance = BigDecimal.valueOf(doubleValue).setScale(2, RoundingMode.HALF_UP);//obtiene asistencia total before --> (2, RoundingMode.DOWN)
		Double doubleValue2 = (Double) listMessage.get("newServiceTotal");
		BigDecimal valueAmountService = BigDecimal.valueOf(doubleValue2).setScale(2, RoundingMode.HALF_UP);//obtiene servicio total before --> (2, RoundingMode.DOWN)
		BigDecimal valueTotalDeuda = totalDeuda;//valueAmountFinance.add(valueAmountService).add(valueAmountAssistance);
		
		BigDecimal sumCapital = BigDecimal.ZERO;
		BigDecimal sumAssitance = BigDecimal.ZERO;
		BigDecimal sumService = BigDecimal.ZERO;
		BigDecimal sumImport = BigDecimal.ZERO;

		for (EcsfqAmortization objAmortization : listAmortization) {
			sumCapital = sumCapital.add(objAmortization.getCapital());
			sumAssitance = sumAssitance.add(objAmortization.getShppwsAssistance());
			sumService = sumService.add(objAmortization.getShppwsService());
			sumImport = sumImport.add(objAmortization.getAmount());
		}
		
		BigDecimal adjustmentAmountCapital = valueAmountFinance.subtract(sumCapital);
		BigDecimal adjustmentAmountAssistance = valueAmountAssistance.subtract(sumAssitance);
		BigDecimal adjustmentAmountService = valueAmountService.subtract(sumService);
		BigDecimal adjustmentAmountImport = valueTotalDeuda.subtract(sumImport);
		
		EcsfqAmortization objAmortization=listAmortization.get(0);
		BigDecimal valueAmountAmount = objAmortization.getAmount();
		//CAPITAL
		BigDecimal auxCapital= objAmortization.getCapital();
		if (adjustmentAmountCapital.compareTo(BigDecimal.ZERO) > 0) {
			auxCapital = auxCapital.add(adjustmentAmountCapital);
		} else if (adjustmentAmountCapital.compareTo(BigDecimal.ZERO) < 0) {
			auxCapital = auxCapital.subtract(adjustmentAmountCapital.abs());
		}
		objAmortization.setCapital(auxCapital);
		//ASISTENCIA
		BigDecimal auxAssistance= objAmortization.getShppwsAssistance();
		if (adjustmentAmountAssistance.compareTo(BigDecimal.ZERO) > 0) {
			auxAssistance = auxAssistance.add(adjustmentAmountAssistance);
		} else if (adjustmentAmountAssistance.compareTo(BigDecimal.ZERO) < 0) {
			auxAssistance = auxAssistance.subtract(adjustmentAmountAssistance.abs());
		}
		objAmortization.setShppwsAssistance(auxAssistance);
		//SERVICIO
		BigDecimal auxService= objAmortization.getShppwsService();
		if (adjustmentAmountService.compareTo(BigDecimal.ZERO) > 0) {
			auxService = auxService.add(adjustmentAmountService);
		} else if (adjustmentAmountService.compareTo(BigDecimal.ZERO) < 0) {
			auxService = auxService.subtract(adjustmentAmountService.abs());
		}
		objAmortization.setShppwsService(auxService);
		//IMPORTE
		BigDecimal auxImport= objAmortization.getAmount();
		if (adjustmentAmountImport.compareTo(BigDecimal.ZERO) > 0) {
			auxImport = auxImport.add(adjustmentAmountImport);
		} else if (adjustmentAmountImport.compareTo(BigDecimal.ZERO) < 0) {
			auxImport = auxImport.subtract(adjustmentAmountImport.abs());
		}
		objAmortization.setAmount(auxImport);
		
		OBDal.getInstance().save(objAmortization);
		OBDal.getInstance().flush();
	}
	
	public void checkAdditionalService() {
		
	}
	
	public void updatePartnerInfo(String Cell_Phone,String Mail,BusinessPartner partner, Location location) {
		partner.setEEIEmail(Mail);
		location.setPhone(Cell_Phone);
		location.setScactuCellphoneNumber(Cell_Phone);
	}
	
	public String type_verify(ECSFQ_Quotation objFastQuotation, Boolean Venta_a_Domicilio) {
		String type_verification = "null";
		Opcrmopportunities op = objFastQuotation.getOpcrmOpportunities();
		String endSeg = op.getShppwsOpEndsegment();
		String shopGroup = op.getShppwsOpShopgroup();
		shppee_Verification objVerifiction = null;
		
		if(Venta_a_Domicilio) {
			OBCriteria<shppee_Verification> queryVerification= OBDal.getInstance().createCriteria(shppee_Verification.class);
			queryVerification.add(Restrictions.eq(shppee_Verification.PROPERTY_SHPCTHOMEDELIVERY, true));
			queryVerification.setMaxResults(1);
			objVerifiction = (shppee_Verification)queryVerification.uniqueResult();
		}
		
		if(objVerifiction == null){
			OBCriteria<shppee_Verification> queryVerification= OBDal.getInstance().createCriteria(shppee_Verification.class);
			queryVerification.add(Restrictions.eq(shppee_Verification.PROPERTY_ENDSEGMENT, endSeg));
			queryVerification.add(Restrictions.eq(shppee_Verification.PROPERTY_SHOPGROUP, shopGroup));
			queryVerification.setMaxResults(1);
			objVerifiction = (shppee_Verification)queryVerification.uniqueResult();
		}
		
		if(objVerifiction == null) {
			type_verification = "No se ha encontrado coincidencias en la matriz de Verificación";
		}else {
			type_verification = objVerifiction.getMessage();
		}
		
		return type_verification;
	}
	
	public void CreateCOM_S(String oportunityDoc, ECSFQ_Quotation objFastQuotation, SscfCom1 objComS, shppws_config accesApi, SscfCreditOperation objCreditOperations, String Identifier, String Surnames, String Names, String String_Profession, String Province, String Birthdate, String No_Home, String Main_Street_domic, String Cross_Street, String Gender, String Civil_Status, String Cell_Phone, String Economic_Activity, String Mail, String Type_Of_Dependent, String Type_Of_Independent, String Company, String RUC, String Address, Map<String, Object> listMessage, Boolean Venta_a_Domicilio) {
		String message5="objComS";
		try {
	  		objComS.setClient(accesApi.getClient());
	  		objComS.setOrganization(accesApi.getOrganization());
	  		objComS.setActive(accesApi.isActive());
	  		objComS.setCreatedBy(accesApi.getCreatedBy());
	  		objComS.setUpdatedBy(accesApi.getUpdatedBy());
	  		objComS.setSscfCreditOperation(objCreditOperations);
	  		
	  		objComS.setTaxID(Identifier);
	  		objComS.setLastName(Surnames);
	  		objComS.setLastname2(".");
	  		objComS.setName(Names);
	  		objComS.setName2(".");
	  		objComS.setCell(Cell_Phone);
	  		objComS.setProductDestination(".");
	  		
		  		OBCriteria<Profession> queryProfession= OBDal.getInstance().createCriteria(Profession.class);
		  		queryProfession.add(Restrictions.eq(Profession.PROPERTY_NAME, String_Profession));
				List<Profession> listProfessions = queryProfession.list();
				int sizeprofession=listProfessions.size();
				if(sizeprofession > 0) {
					Profession objProfession = listProfessions.get(0);
					objComS.setProfession(objProfession);
				}else {
					Profession objProfession = OBProvider.getInstance().get(Profession.class);
					objProfession.setClient(accesApi.getClient());
					objProfession.setOrganization(accesApi.getOrganization());
					objProfession.setActive(accesApi.isActive());
					objProfession.setCreatedBy(accesApi.getCreatedBy());
					objProfession.setUpdatedBy(accesApi.getUpdatedBy());
					objProfession.setValue(String_Profession);
					objProfession.setName(String_Profession);
					OBDal.getInstance().save(objProfession);
					OBDal.getInstance().flush();
					objComS.setProfession(objProfession);
				}
				
				OBCriteria<Country> queryCountry= OBDal.getInstance().createCriteria(Country.class);
				queryCountry.add(Restrictions.eq(Country.PROPERTY_ISOCOUNTRYCODE, "EC"));
				Country objNationality = (Country)queryCountry.uniqueResult();
				objComS.setNationality(objNationality);
				
				OBCriteria<Region> queryRegion= OBDal.getInstance().createCriteria(Region.class);
				queryRegion.add(Restrictions.eq(Region.PROPERTY_NAME, Province));
		  		Region objSscfmnRegion = (Region)queryRegion.uniqueResult();
				objComS.setSscfmnRegion(objSscfmnRegion);
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				LocalDate localDate = LocalDate.parse(Birthdate, formatter);
				Date objDate = java.sql.Date.valueOf(localDate);
				objComS.setDateBirth(objDate);
				
				OBCriteria<SSCFMNSex> querySSCFMNSex= OBDal.getInstance().createCriteria(SSCFMNSex.class);
		  		querySSCFMNSex.add(Restrictions.eq(SSCFMNSex.PROPERTY_COMMERCIALNAME, Gender));
		  		SSCFMNSex objSSCFMNSex = (SSCFMNSex)querySSCFMNSex.uniqueResult();
				objComS.setSscfmnSex(objSSCFMNSex);
	  		
				objComS.setAddress(No_Home+" - "+Main_Street_domic+" - "+Cross_Street);
				objComS.setPhone(Cell_Phone);
				objComS.setEmail(Mail);
				
				OBCriteria<CivilStatus> queryCivilStatus= OBDal.getInstance().createCriteria(CivilStatus.class);
		  		queryCivilStatus.add(Restrictions.eq(CivilStatus.PROPERTY_VALUE, Civil_Status));
		  		CivilStatus objCivilStatus = (CivilStatus)queryCivilStatus.uniqueResult();
				objComS.setCivilStatus(objCivilStatus);
				
				//Economic Activity
				objComS.setShppwsHomeowner(Economic_Activity);
					if(Type_Of_Dependent.equals("PRIVADO")) {
						objComS.setShppwsDependent("PV");
					}else{
						objComS.setShppwsDependent("PB");
					}
					if(Type_Of_Independent.equals("FORMAL")) {
						objComS.setShppwsIndependent("FM");
					}else{
						objComS.setShppwsIndependent("IFM");
					}
				objComS.setShppwsCompany(Company);
				objComS.setShppwsRuc(RUC);
				objComS.setShppwsAddres(Address);
				
				if(objFastQuotation != null) {
					Opcrmopportunities op = objFastQuotation.getOpcrmOpportunities();
					String endSeg = op.getShppwsOpEndsegment();
					String shopGroup = op.getShppwsOpShopgroup();
					if((Venta_a_Domicilio) || (endSeg != null && !endSeg.equals("") && shopGroup != null && !shopGroup.equals(""))) {
						shppee_Verification objVerifiction = null;
						
						if(Venta_a_Domicilio) {
							OBCriteria<shppee_Verification> queryVerification= OBDal.getInstance().createCriteria(shppee_Verification.class);
							queryVerification.add(Restrictions.eq(shppee_Verification.PROPERTY_SHPCTHOMEDELIVERY, true));
							queryVerification.setMaxResults(1);
							objVerifiction = (shppee_Verification)queryVerification.uniqueResult();
						}
						
						if(objVerifiction == null){
							OBCriteria<shppee_Verification> queryVerification= OBDal.getInstance().createCriteria(shppee_Verification.class);
							queryVerification.add(Restrictions.eq(shppee_Verification.PROPERTY_ENDSEGMENT, endSeg));
							queryVerification.add(Restrictions.eq(shppee_Verification.PROPERTY_SHOPGROUP, shopGroup));
							queryVerification.setMaxResults(1);
							objVerifiction = (shppee_Verification)queryVerification.uniqueResult();
						}
						
						BusinessPartner BP = objCreditOperations.getBusinessPartner();
						
						OBCriteria<SweqxEquifax> queryEquifax= OBDal.getInstance().createCriteria(SweqxEquifax.class);
						queryEquifax.add(Restrictions.eq(SweqxEquifax.PROPERTY_BUSINESSPARTNER, BP));
					    queryEquifax.addOrder(Order.desc(SweqxEquifax.PROPERTY_CREATIONDATE));
					    queryEquifax.setMaxResults(1);
					    SweqxEquifax mostRecentRecord = (SweqxEquifax) queryEquifax.uniqueResult();

						if(objVerifiction != null) {
							String messageVerification = objVerifiction.getMessage() != null ? objVerifiction.getMessage() : "";
							objComS.setShpctTypeverification(messageVerification);
							objCreditOperations.setShppetVerifytype(messageVerification);
							objCreditOperations.setShppetEquifax(mostRecentRecord);
						}
					}
				}
	  		
	  		OBDal.getInstance().save(objComS);
			OBDal.getInstance().flush();
			message5=" SI crea Com-S"+"-----"+ objComS.getId();
			listMessage.put("message", message5);
	  	}catch(Exception e) {
	  		message5+=" NO crea Com-S"+"-----"+ e.getMessage();
	  		listMessage.put("message", message5);
	  	}

	}

	public void CreatePersonalReference(SscfPersonalReference objPersonalReference, shppws_config accesApi, SscfCom1 objComS, String Ref_Surnames,String Ref_Names, String Ref_RelationShip, String Ref_CellPhone, Map<String, Object> listMessage) {
		String message6="";
		try {
			objPersonalReference.setClient(accesApi.getClient());
	  		objPersonalReference.setOrganization(accesApi.getOrganization());
	  		objPersonalReference.setActive(accesApi.isActive());
	  		objPersonalReference.setCreatedBy(accesApi.getCreatedBy());
	  		objPersonalReference.setUpdatedBy(accesApi.getUpdatedBy());
	  		objPersonalReference.setSscfCom1(objComS);
	  		
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
			message6=" SI crea Referencias"+"-----"+ objPersonalReference.getId();
			listMessage.put("message", message6);
		}catch(Exception e) {
			message6+=" NO crea Referencias"+"-----"+ e.getMessage();
	  		listMessage.put("message", message6);
		}
	}
	
	public void CreateIncome(SscfIncome objIncome, shppws_config accesApi, SscfCom1 objComS, BigDecimal Income_Salary, BigDecimal Income_Others, Map<String, Object> listMessage) {
		String message7="CreateIncome";
		try {
			objIncome.setClient(accesApi.getClient());
			objIncome.setOrganization(accesApi.getOrganization());
	  		objIncome.setActive(accesApi.isActive());
	  		objIncome.setCreatedBy(accesApi.getCreatedBy());
	  		objIncome.setUpdatedBy(accesApi.getUpdatedBy());
	  		objIncome.setSscfCom1(objComS);
	  		
	  		objIncome.setSubject("C");
	  		objIncome.setSalary(Income_Salary);
	  		objIncome.setOthers(Income_Others);
	  		BigDecimal suma = Income_Salary.add(Income_Others);
	  		objIncome.setTotal(suma);
	  	
	  		OBDal.getInstance().save(objIncome);
			OBDal.getInstance().flush();
			message7=" SI crea Incomes"+"-----"+ objIncome.getId();
			listMessage.put("message", message7);
		}catch(Exception e) {
			message7+=" NO crea Incomes"+"-----"+ e.getMessage();
	  		listMessage.put("message", message7);
		}
	}
	
	public void CreateExpense(SscfExpense objExpense, shppws_config accesApi, SscfCom1 objComS, BigDecimal Expense_Rent, BigDecimal Expense_Others,  Map<String, Object> listMessage) {
		String message8="objExpense";
		try {
			objExpense.setClient(accesApi.getClient());
			objExpense.setOrganization(accesApi.getOrganization());
			objExpense.setActive(accesApi.isActive());
	  		objExpense.setCreatedBy(accesApi.getCreatedBy());
	  		objExpense.setUpdatedBy(accesApi.getUpdatedBy());
	  		objExpense.setSscfCom1(objComS);
	  		
	  		objExpense.setRent(Expense_Rent);
	  		objExpense.setPaymentCredits(Expense_Others);
	  		BigDecimal suma = Expense_Rent.add(Expense_Others);
	  		objExpense.setTotal(suma);
	  	
	  		OBDal.getInstance().save(objExpense);
			OBDal.getInstance().flush();
			message8=" SI crea Expenses"+"-----"+ objExpense.getId();
			listMessage.put("message", "Ok");
		}catch(Exception e) {
			message8+=" NO crea Expenses"+"-----"+ e.getMessage();
	  		listMessage.put("message", "Error");
		}
	}
	
	/**
	 * Verifica si existe una excepción activa para un cliente específico basado en su identificador fiscal.
	 * Compara el identificador proporcionado con el asociado a la oportunidad y realiza una búsqueda 
	 * de excepciones activas dentro del rango de fechas válido.
	 * 
	 * @param Identifier El identificador fiscal del cliente.
	 * @param objOpportunity La oportunidad que contiene los datos del cliente y de la organización.
	 * @return true si existe al menos una excepción activa para el cliente, false en caso contrario.
	 * @throws Exception Si el identificador del cliente no coincide con el de la oportunidad o si ocurre un error durante la validación.
	 */
	public boolean ifExistExceptionClient(String Identifier, Opcrmopportunities objOpportunity) throws Exception {
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        currentDate = calendar.getTime();
		
		BusinessPartner BP = objOpportunity.getBusinessPartner();
		String taxID = BP.getTaxID();
		
		if(!taxID.equals(Identifier)) {
			throw new Exception("No coinciden los datos del tercero en la oportunidad generada");
		}
		
		OBCriteria<Ecsce_CustomerException> queryExceptionCli = OBDal.getInstance().createCriteria(Ecsce_CustomerException.class);
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_TAXID, Identifier));
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_ACTIVE, true));
		queryExceptionCli.add(Restrictions.le(Ecsce_CustomerException.PROPERTY_STARTINGDATE, currentDate)); // startingDate <= currentDate
	    queryExceptionCli.add(Restrictions.ge(Ecsce_CustomerException.PROPERTY_DATEUNTIL, currentDate));   // dateUntil >= currentDate
		List<Ecsce_CustomerException> listClientException = queryExceptionCli.list();
		
		if(listClientException.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Obtiene una lista de subproductos (byproducts) asociados a una excepción de cliente para un cliente específico.
	 * Valida las excepciones del cliente basándose en criterios como identificador, rango de fechas, 
	 * grupo de tienda y segmento final. Si no se encuentran subproductos válidos, se registra un mensaje
	 * de la excepción en la bitácora y se lanza una excepción.
	 * 
	 * @param Identifier  El identificador fiscal del cliente.
	 * @param objOpportunity La oportunidad que contiene datos del cliente y la organización.
	 * @param Term_Code El código de validación del subproducto.
	 * @return Una lista de subproductos que cumplen con los criterios de excepción.
	 * @throws Exception Si no se encuentran subproductos que cumplan los criterios o si ocurre un error durante el procesamiento.
	 */
	public List<scsl_Byproducts> byProdsExceptionClient(String Identifier, Opcrmopportunities objOpportunity, String Term_Code) throws Exception {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        currentDate = calendar.getTime();
        
        String storeGroup = objOpportunity.getShppwsOpShopgroup();
		String endSegment = objOpportunity.getShppwsOpEndsegment();
        
        List<scsl_Byproducts> byProds = new ArrayList<>();
		
		OBCriteria<Ecsce_CustomerException> queryExceptionCli = OBDal.getInstance().createCriteria(Ecsce_CustomerException.class);
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_TAXID, Identifier));
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_ACTIVE, true));
		queryExceptionCli.add(Restrictions.le(Ecsce_CustomerException.PROPERTY_STARTINGDATE, currentDate)); // startingDate <= currentDate
	    queryExceptionCli.add(Restrictions.ge(Ecsce_CustomerException.PROPERTY_DATEUNTIL, currentDate));   // dateUntil >= currentDate
	    queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_FINALSEGMENT, endSegment));
	    queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_STOREGROUP, storeGroup));
		List<Ecsce_CustomerException> listClientException = queryExceptionCli.list();
		
		Ecsce_CustomerException objClient = listClientException.get(0);
		
		OBCriteria<Ecsce_CustomerExcepSub> queryExceptionClibyProd = OBDal.getInstance().createCriteria(Ecsce_CustomerExcepSub.class);
		queryExceptionClibyProd.add(Restrictions.eq(Ecsce_CustomerExcepSub.PROPERTY_ECSCECUSTOMEREXCEPTION, objClient));
		queryExceptionClibyProd.add(Restrictions.eq(Ecsce_CustomerExcepSub.PROPERTY_ACTIVE, true));
		
		List<Ecsce_CustomerExcepSub> listbyProdClientException = queryExceptionClibyProd.list();
		if(listbyProdClientException.size()>0) {
			for(Ecsce_CustomerExcepSub objByProd : listbyProdClientException) {
				if(objByProd.getScslByproducts().getValidationCode().equals(Term_Code)) {
					byProds.add(objByProd.getScslByproducts());
				}
			}
			if(byProds.size()>0) { 
				updateCustomerException(objClient, false );
				return byProds;
			} else {
				updateCustomerException(objClient, false);
				saveBinnacle(objClient, objOpportunity);
				throw new Exception(""+objClient.getMessage());
			}
		} else {
			saveBinnacle(objClient, objOpportunity);
			throw new Exception(""+objClient.getMessage());
		}
	}

	/**
	 * Método que actualiza las propiedades de Ecsce_CustomerException.
	 * 
	 * @param objClient El objeto Ecsce_CustomerException que se va a actualizar.
	 * @param newValue El nuevo valor que se va a establecer en el campo.
	 */
	public void updateCustomerException(Ecsce_CustomerException objClient, Boolean newValue) {
	    objClient.setActive(newValue); 
	    OBDal.getInstance().save(objClient);
	}

	
	/**
	 * Registra una entrada en la bitácora para una oportunidad que no cumple con ningún subproducto.
	 * Incluye detalles como el cliente, la organización y un mensaje de la excepción del cliente.
	 * 
	 * @param objClient El objeto de excepción del cliente que contiene el mensaje de error.
	 * @param objOpportunity La oportunidad para la cual se crea la entrada en la bitácora.
	 * @throws Exception Si ocurre un error al guardar la entrada en la bitácora.
	 */
	public void saveBinnacle(Ecsce_CustomerException objClient, Opcrmopportunities objOpportunity) throws Exception {
		try {
		      //Se crea una nueva instancia de Bitácora   
			  shpctBinnacle objBinnacle = OBProvider.getInstance().get(shpctBinnacle.class);
			  objBinnacle.setClient(objOpportunity.getClient());
			  objBinnacle.setOrganization(objOpportunity.getOrganization());
			  objBinnacle.setOpcrmOpportunities(objOpportunity);
			  
			  objBinnacle.setNameMatrix("MATRIZ EXCEPCION");
			  objBinnacle.setMessages(objClient.getMessage()+"");
			  objBinnacle.setResults("R");
			  objBinnacle.setComments("No se encuentra el Subproducto");
			  OBDal.getInstance().save(objBinnacle);
			  OBDal.getInstance().flush();
			  OBDal.getInstance().getConnection().commit();
	  }catch(Exception e) {
		  throw new Exception("Hubo un error al generar la Bitácora de la oportunidad ");
	  }
	}

	private JSONObject generateJsonResponse(String identifier, String no_opportunity) throws Exception {
		JSONObject jsonResponse = new JSONObject();

		// Construcción del valor concatenado
		String concatenatedValue = no_opportunity + " - " + identifier;
		String endpoint = "curl";
		String result = "OK";
		String type = "OUT";

		// Consulta a la base de datos
		OBCriteria<Scactu_Log> query = OBDal.getInstance().createCriteria(Scactu_Log.class);
		query.add(Restrictions.eq(Scactu_Log.PROPERTY_REFERENCENO, concatenatedValue));
		query.add(Restrictions.eq(Scactu_Log.PROPERTY_ENDPOINT, endpoint));
		query.add(Restrictions.eq(Scactu_Log.PROPERTY_RESULT, result));
		query.add(Restrictions.isNotNull(Scactu_Log.PROPERTY_RECORDID));
		query.add(Restrictions.eq(Scactu_Log.PROPERTY_TYPE, type));

		List<Scactu_Log> logs = query.list();

		if (!logs.isEmpty()) {
			Scactu_Log log = logs.get(0);
			String jsonResponseString = log.getJsonResponse().trim(); // Obtener la cadena JSON

			// Solo procesamos si es un array JSON
			if (jsonResponseString.startsWith("[")) {
				JSONArray jsonArray = new JSONArray(jsonResponseString); // Convertir a JSONArray
				jsonResponse.put("data", jsonArray); // Agregar el array bajo la clave "data"
			}
		}

		return jsonResponse;
	}
	
	public void validateStructure(JSONObject servicioJSON) throws Exception {
		if(!servicioJSON.has("Name")) {
			throw new Exception("Verificar la estructura de los servicios adicionales en el tag Name");
		}
		if(servicioJSON.getString("Name").equals("")) {
			throw new Exception("Verificar la estructura de los servicios adicionales en el tag Name");
		}
		if(!servicioJSON.has("Amount")) {
			throw new Exception("Verificar la estructura de los servicios adicionales en el tag Amount");
		}
		BigDecimal Amount = new BigDecimal(servicioJSON.getString("Amount"));
		if(Amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Verificar la estructura de los servicios adicionales en el tag Amount");
		}
	}
	
	public scsl_Creditservices serviceInBDD(String prodID, String code) throws Exception {
		scsl_Creditservices validator = null;
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(" WHERE ");
		whereClause.append(scsl_Creditservices.PROPERTY_ACTIVE + " = 'Y' ");
		whereClause.append(" AND ");
		whereClause.append(scsl_Creditservices.PROPERTY_VALIDATIONCODE + " = :code ");
		whereClause.append(" AND ");
		whereClause.append(scsl_Creditservices.PROPERTY_SCSLPRODUCT + ".id = :prodID ");
		whereClause.append(" AND ");
		whereClause.append(scsl_Creditservices.PROPERTY_SWSSLTYPEPROD + " = 'SCSL_AddServ' ");
		OBQuery<scsl_Creditservices> qServices = OBDal.getInstance().createQuery(scsl_Creditservices.class, whereClause.toString());
		qServices.setNamedParameter("code", code);
		qServices.setNamedParameter("prodID", prodID);
		
		List<scsl_Creditservices> listServices = qServices.list();
		if(listServices.size()>0) {
			validator = listServices.get(0);
		}else {
			throw new Exception("Error en enrolamiento. No se encuentra parametrizado el Servicio Adicional "+code);
		}
		
		return validator;
	}
	
	public void validateadditionalServ(JSONArray addServicesArray) throws Exception {
		Set<String> seenNames = new HashSet<>();
	    for (int i = 0; i < addServicesArray.length(); i++) {
	        JSONObject obj = addServicesArray.getJSONObject(i);
	        if (obj.has("Name")) {
	            String name = obj.getString("Name");
	            if (seenNames.contains(name)) {
	                throw new Exception("Nombre duplicado en servicios adicionales: " + name);
	            }
	            seenNames.add(name);
	        }
	    }
	}
	
	public void changeStateOpportunity(Opcrmopportunities objOpportunity, String message) {
		try {
			// Se crea una nueva instancia de Bitácora y se asigna como perdida la OP.
			shpctBinnacle objBinnacle = OBProvider.getInstance().get(shpctBinnacle.class);
			objBinnacle.setClient(objOpportunity.getClient());
			objBinnacle.setOrganization(objOpportunity.getOrganization());
			objBinnacle.setOpcrmOpportunities(objOpportunity);

			objBinnacle.setNameMatrix("Enrolamiento");
			objBinnacle.setMessages("Error en enrolamiento");
			objBinnacle.setResults("R");
			if (message != null && message.length() > 60) {
				message = message.substring(0, 60);
			}
			objBinnacle.setComments(message);
			OBDal.getInstance().save(objBinnacle);
			OBDal.getInstance().flush();

			objOpportunity.setOpportstatus("LOST");//
			OBDal.getInstance().save(objOpportunity);
			OBDal.getInstance().flush();

			//OBDal.getInstance().getConnection().commit();
		} catch (Exception e) {
		}
	}


	private String optStringWithTypeValidation(JSONObject json, String key, String defaultValue) throws Exception {
		if (!json.has(key)) {
			return defaultValue;
		}
		
		Object value = json.opt(key);
		
		if (value == null || value == JSONObject.NULL) {
			return defaultValue;
		}
		
		if (value instanceof String) {
			return (String) value;
		}
		
		String valueType = value.getClass().getSimpleName();
		if (value instanceof Number) {
			valueType = "Number";
		} else if (value instanceof Boolean) {
			valueType = "Boolean";
		} else if (value instanceof JSONArray) {
			valueType = "Array";
		} else if (value instanceof JSONObject) {
			valueType = "Object";
		}
		
		throw new Exception("El campo '" + key + "' debe ser de tipo String, pero se recibió tipo " + valueType + ". Valor recibido: " + value.toString());
	}


	private BusinessPartner validateCommerce(String codeCommerce, String ruc) throws Exception {
		String trimmedCodeCommerce = (codeCommerce != null) ? codeCommerce.trim() : "";
		boolean hasCodeCommerce = !trimmedCodeCommerce.isEmpty();
		
		if (!hasCodeCommerce) {
			return null;
		}
		
		return findActiveCommerce(trimmedCodeCommerce);
	}
	

	private BusinessPartner findActiveCommerce(String searchKey) throws Exception {
		if (searchKey.length() > 32) {
			throw new Exception("El código/RUC del comercio excede la longitud máxima permitida (32 caracteres).");
		}
		
		OBCriteria<BusinessPartner> queryBusinessPartner = OBDal.getInstance().createCriteria(BusinessPartner.class);
		queryBusinessPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_TAXID, searchKey));
		queryBusinessPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_ACTIVE, true));
		List<BusinessPartner> listObjBusinessPartner = queryBusinessPartner.list();
		
		if (listObjBusinessPartner == null || listObjBusinessPartner.isEmpty()) {
			throw new Exception("El comercio con código/RUC " + searchKey + " no existe o no está activo en el maestro de Terceros. Por favor, verifique que el comercio esté creado y activo antes de continuar con el enrolamiento.");
		}
		
		BusinessPartner objCommercialcode = listObjBusinessPartner.get(0);
		
		if (!objCommercialcode.isActive()) {
			throw new Exception("El comercio con código/RUC " + searchKey + " no está activo en el maestro de Terceros.");
		}
		
		return objCommercialcode;
	}

	private void updateCreditInfoInEnrollment(Opcrmopportunities objOpportunity, String codeCommerce, 
			String codeAgency, String cityStoreGroup, String provinceStoreGroup, String user, String ruc) throws Exception {
		
		if (codeCommerce != null && !codeCommerce.trim().isEmpty()) {
			BusinessPartner objCommercialcode = validateCommerce(codeCommerce, ruc);
			if (objCommercialcode != null) {
				objOpportunity.setShppwsOpCodecommercial(objCommercialcode);
			}
		}

		if (codeAgency != null && !codeAgency.trim().isEmpty()) {
			String trimmedAgency = codeAgency.trim();
			if (trimmedAgency.length() > 60) {
				throw new Exception("El código de agencia excede la longitud máxima permitida (60 caracteres). Valor recibido: " + trimmedAgency.length() + " caracteres.");
			}
			objOpportunity.setShppwsOpAgencycode(trimmedAgency);
		}

		if (cityStoreGroup != null && !cityStoreGroup.trim().isEmpty()) {
			String trimmedCity = cityStoreGroup.trim();
			if (trimmedCity.length() > 32) {
				throw new Exception("La ciudad del grupo de tienda excede la longitud máxima permitida (32 caracteres). Valor recibido: " + trimmedCity.length() + " caracteres.");
			}
			objOpportunity.setShppwsCityStoreGroup(trimmedCity);
		}

		if (provinceStoreGroup != null && !provinceStoreGroup.trim().isEmpty()) {
			String trimmedProvince = provinceStoreGroup.trim();
			if (trimmedProvince.length() > 32) {
				throw new Exception("La provincia del grupo de tienda excede la longitud máxima permitida (32 caracteres). Valor recibido: " + trimmedProvince.length() + " caracteres.");
			}
			objOpportunity.setShppwsProvinceStoreGroup(trimmedProvince);
		}

		if (user != null && !user.trim().isEmpty()) {
			objOpportunity.setSHPPWSUser(user.trim());
		} 

		OBDal.getInstance().save(objOpportunity);
		OBDal.getInstance().flush();
	}


	public void saveBinnacleUpdate(Opcrmopportunities objOpportunity, String errorMessage) {
		try {
			shpctBinnacle objBinnacle = OBProvider.getInstance().get(shpctBinnacle.class);
			objBinnacle.setClient(objOpportunity.getClient());
			objBinnacle.setOrganization(objOpportunity.getOrganization());
			objBinnacle.setOpcrmOpportunities(objOpportunity);
			
			objBinnacle.setNameMatrix("Actualización Crédito Enrolamiento");
			objBinnacle.setMessages("Error en actualización de información del crédito");
			objBinnacle.setResults("R");
			if (errorMessage != null && errorMessage.length() > 60) {
				errorMessage = errorMessage.substring(0, 60);
			}
			objBinnacle.setComments(errorMessage);
			OBDal.getInstance().save(objBinnacle);
			OBDal.getInstance().flush();
			OBDal.getInstance().getConnection().commit();
		} catch(Exception e) {
			System.err.println("Error al guardar en bitácora: " + e.getMessage());
		}
	}

}
