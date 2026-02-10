package ec.com.sidesoft.happypay.web.services.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.web.WebService;
import org.hibernate.criterion.Restrictions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ec.com.sidesoft.credit.simulator.scsl_Product;
import ec.com.sidesoft.customer.exception.Ecsce_CustomerExcepSub;
import ec.com.sidesoft.customer.exception.Ecsce_CustomerException;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_Quotas;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_q_byproducts;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.ws.equifax.SweqxEquifax;
import it.openia.crm.Opcrmopportunities;
import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.credit.simulator.scsl_Byproducts;
import ec.com.sidesoft.credit.simulator.scsl_Creditservices;

import org.apache.log4j.Logger;

public class crsimulator implements WebService{
	private static final long serialVersionUID = 1L;
	private final Logger log4j = Logger.getLogger(crsimulator.class);
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		String Status = "OK";
		String noReference = "";
		JSONObject product = new JSONObject();
		
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
        String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "crsimulator", requestParameters);
		
		try {
		
		    String productID="";
		    String Interface = request.getParameter("Interface");
			String Channel = request.getParameter("Channel");
			String Trade_Code = request.getParameter("Trade_Code");
			String Code_Agency = request.getParameter("Code_Agency");
			String Code_Product = request.getParameter("Code_Product");
			String Identifier = request.getParameter("Identifier");
			String Cell_Phone = request.getParameter("Cell_Phone");
			String Mail = request.getParameter("Mail");
			String Amount_Finance = request.getParameter("Amount_Finance");
			Double amountFinance = new Double(Amount_Finance);
			String Input_Value = request.getParameter("Input_Value");
			String User = request.getParameter("User");
			String No_Opportunity = request.getParameter("No_Opportunity");
		    
		    Organization organization = accesApi.getOrganization();
		    Currency objcurrency = organization.getCurrency();
		    
		    OBCriteria<scsl_Product> queryprod = OBDal.getInstance().createCriteria(scsl_Product.class);
		    queryprod.add(Restrictions.eq(scsl_Product.PROPERTY_VALIDATIONCODE, Code_Product));
		    List<scsl_Product> products = queryprod.list();
		    for (scsl_Product prod : products) {
		    	 productID = prod.getId();
		    }
		    
		    scsl_Product productscsl = OBDal.getInstance().get(scsl_Product.class, productID);
	
		    OBCriteria<scsl_Byproducts> query = OBDal.getInstance().createCriteria(scsl_Byproducts.class);
		    query.add(Restrictions.eq(scsl_Byproducts.PROPERTY_SCSLPRODUCT, productscsl));
		    List<scsl_Byproducts> AllByProducts = query.list();
	
		    List<shppee_q_byproducts> listQuotaByProducts = new ArrayList<>();
		    
		    List<Ecsce_CustomerExcepSub> listExceptionByProducts = new ArrayList<>();
		    
		    if(Interface.equals("cotizacion")) {
		    	OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance().createCriteria(Opcrmopportunities.class);
		    	queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
		    	queryOpportunity.setMaxResults(1);
		    	if(queryOpportunity.list().size() <= 0) {
	    			throw new Exception("No se ha encontrado una oportunidad con el documento ingresado "+No_Opportunity);
		    	}
		    	
		    	Opcrmopportunities opportunity = queryOpportunity.list().get(0);
		    	scsl_Product codeProd=opportunity.getShppwsOpProductcode();
		    	String codeProduct=codeProd.getValidationCode();
		    	String storeGroup=opportunity.getShppwsOpShopgroup();
		    	String endSegment=opportunity.getShppwsOpEndsegment();
		    	
		    	Ecsce_CustomerException client = ifExistExceptionClient(opportunity);
		    	if(client == null) {
		    		OBCriteria<shppee_Quotas> matriz1= OBDal.getInstance().createCriteria(shppee_Quotas.class); 
		  		    List<shppee_Quotas> quotasScores = matriz1.list();
		  		    String aux="";
		  	        for (shppee_Quotas quotasScore : quotasScores) {
		  	        	scsl_Product objProduct = quotasScore.getScslProduct();
		  	        	String product_code=objProduct.getValidationCode();
		  	        	if(codeProduct.equals(product_code)) {
		  	        		if(storeGroup.equals(quotasScore.getAgencyCode())) {
		  			            if (endSegment.equals(quotasScore.getENDSegment())) {
		  			            	OBCriteria<shppee_q_byproducts> queryQbyProducts = OBDal.getInstance().createCriteria(shppee_q_byproducts.class);
		  			            	queryQbyProducts.add(Restrictions.eq(shppee_q_byproducts.PROPERTY_SHPPEEQUOTAS, quotasScore));
		  			            	listQuotaByProducts = queryQbyProducts.list();
		  			            }
		  	        		}
		  	        	}
		  	        }
		    	}else {
		    		OBCriteria<Ecsce_CustomerExcepSub> queryExceptionByProducts = OBDal.getInstance().createCriteria(Ecsce_CustomerExcepSub.class);
		    		queryExceptionByProducts.add(Restrictions.eq(Ecsce_CustomerExcepSub.PROPERTY_ECSCECUSTOMEREXCEPTION, client));
		    		queryExceptionByProducts.add(Restrictions.eq(Ecsce_CustomerExcepSub.PROPERTY_ACTIVE, true));
		    		listExceptionByProducts = queryExceptionByProducts.list();
		    		
		    		if(listExceptionByProducts.size() <= 0) {
		    			throw new Exception("No se encuentran subproductos parametrizados, registre el subproducto en la Matriz de Excepción");
		    		}
		    	}
			}
		    
		    int sizeListExceptionByProducts = listExceptionByProducts.size();//subproductos de Excepcion
		    int sizeListQuotaByProducts = listQuotaByProducts.size();//subproductos de cupos
		    int size = AllByProducts.size();//subproductos
		    
		    //JSONObject product = new JSONObject();
		    if (size > 0) {
				Collections.sort(AllByProducts, (e1, e2) -> e2.getDeadLine().compareTo(e1.getDeadLine()));
		       
		    	JSONArray subproducts = new JSONArray();
		    	
		    	OBCriteria<scsl_Creditservices> queryCreditServices = OBDal.getInstance().createCriteria(scsl_Creditservices.class);
			    queryCreditServices.add(Restrictions.eq(scsl_Creditservices.PROPERTY_SCSLPRODUCT, productscsl));
			    List<scsl_Creditservices> listobjCreditServices = queryCreditServices.list();
			    scsl_Creditservices objCreditServices = null;
			    scsl_Creditservices objCreditAsistance = null;
				for (scsl_Creditservices objCreditService : listobjCreditServices) {
					String auxname = objCreditService.getSwsslTypeprod();// getCommercialName
					if (auxname.equals("SEV")) {// Servicio
						objCreditServices = objCreditService;
					} else if (auxname.equals("ATT")) {// Asistencia
						objCreditAsistance = objCreditService;
					}
				}
		        
				for (scsl_Byproducts byproduct : AllByProducts) {
					Long deadLine = byproduct.getDeadLine();
					BigDecimal comisionFactor = byproduct.getCommissionFactor().divide(BigDecimal.valueOf(100));

					BigDecimal comision = comisionFactor.multiply(BigDecimal.valueOf(amountFinance));
					BigDecimal totalDeuda = (BigDecimal.valueOf(amountFinance).add(comision)).setScale(2,
							RoundingMode.HALF_UP);
					BigDecimal cuotaTotal = totalDeuda.divide(BigDecimal.valueOf(deadLine), RoundingMode.DOWN);

					JSONObject subproduct = new JSONObject();
					subproduct.put("Name", byproduct.getCommercialName());
					subproduct.put("Term_Code", byproduct.getValidationCode());
					subproduct.put("Term", byproduct.getDeadLine());
					subproduct.put("Total_Due", totalDeuda);
					subproduct.put("Quota_Value", cuotaTotal.setScale(2, RoundingMode.HALF_UP));
					subproduct.put("Currency_Symbol", objcurrency.getSymbol());
					subproduct.put("Currency_Code", objcurrency.getISOCode());

					if (objCreditAsistance != null && objCreditServices != null) {
						Double capital = amountFinance / deadLine;
						Double preAmount = amountFinance * comisionFactor.doubleValue();
						Double auxAmounts = preAmount / deadLine;
						Double assistance = auxAmounts * ((objCreditAsistance.getFactor().doubleValue()) / 100);
						Double service = auxAmounts * ((objCreditServices.getFactor().doubleValue()) / 100);

						BigDecimal value_capital = new BigDecimal(capital).setScale(2, RoundingMode.HALF_UP);
						BigDecimal value_assistance = new BigDecimal(assistance).setScale(2, RoundingMode.HALF_UP);
						BigDecimal value_service = new BigDecimal(service).setScale(2, RoundingMode.HALF_UP);
						cuotaTotal = value_capital.add(value_assistance).add(value_service);
						subproduct.put("Quota_Value", cuotaTotal);
					}

					if(sizeListExceptionByProducts > 0) {// Cotizacion Excepciones
						String byprod = byproduct.getValidationCode();
						for (Ecsce_CustomerExcepSub objExceptionByProducts : listExceptionByProducts) {
							scsl_Byproducts qbyprod = objExceptionByProducts.getScslByproducts();
							String codeQbyProd = qbyprod.getValidationCode();
							if (byprod.equals(codeQbyProd)) {
								subproducts.put(subproduct);// agrega solo validados
							}
						}
					} else if (sizeListQuotaByProducts > 0) {// Cotizacion Cupos
						String byprod = byproduct.getValidationCode();
						for (shppee_q_byproducts objQbyProducts : listQuotaByProducts) {
							scsl_Byproducts qbyprod = objQbyProducts.getScslByproducts();
							String codeQbyProd = qbyprod.getValidationCode();
							if (byprod.equals(codeQbyProd)) {
								subproducts.put(subproduct);// agrega solo validados
							}
						}
					} else if (Interface.equals("simulacion")) {// Simulacion
						subproducts.put(subproduct);// agrega todo
					}
				}
		        
		        product.put("Product", Code_Product); // Codigo del producto
		        product.put("Subproducts", subproducts);
		    }
	    
	    noReference = Identifier;
		}catch(Exception e) {
			product.put("Product","ERROR");
			product.put("message", e.getMessage());
			Error = "data error "+e.getMessage();
			Status = "ERROR";
		}


	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        JSONArray jsonArray = new JSONArray();
	        jsonArray.put(product);
	        String json = jsonArray.getJSONObject(0).toString();
	        PrintWriter writer = response.getWriter();
	        writer.write(json);
	        writer.close();
	        
	        String requestUrl = request.getRequestURL().toString();
	        
	        try {
		        String InterfaceLOG = "SHPPWS_NT";
			    String Process = "Simulacion/Cotizacion";
			    String idRegister = "";
			    logger.log_end_register(log, requestUrl, noReference, json, Status, "OUT", InterfaceLOG, Process, idRegister, Error);
			}catch(Exception e) {}
	}
	

	
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		String Error = "";
		String Status = "OK";
		String noReference = "";
		JSONObject product = new JSONObject();
		
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		JSONObject requestJson = getJSON(request);
        String requestStr =  requestJson.toString();
		Scactu_Log log = logger.log_start_register(accesApi, "crsimulator", requestStr);
		
		try {
		
		    String productID="";
		    String Interface = requestJson.getString("Interface");
			String Code_Product = requestJson.getString("Code_Product");
			String Identifier = requestJson.getString("Identifier");
			String Amount_Finance = requestJson.getString("Amount_Finance");
			Double amountFinance = new Double(Amount_Finance);
			String No_Opportunity = requestJson.getString("No_Opportunity");
		    
		    Organization organization = accesApi.getOrganization();
		    Currency objcurrency = organization.getCurrency();
		    
		    OBCriteria<scsl_Product> queryprod = OBDal.getInstance().createCriteria(scsl_Product.class);
		    queryprod.add(Restrictions.eq(scsl_Product.PROPERTY_VALIDATIONCODE, Code_Product));
		    List<scsl_Product> products = queryprod.list();
		    for (scsl_Product prod : products) {
		    	 productID = prod.getId();
		    }
		    
		    scsl_Product productscsl = OBDal.getInstance().get(scsl_Product.class, productID);
	
		    OBCriteria<scsl_Byproducts> query = OBDal.getInstance().createCriteria(scsl_Byproducts.class);
		    query.add(Restrictions.eq(scsl_Byproducts.PROPERTY_SCSLPRODUCT, productscsl));
		    List<scsl_Byproducts> AllByProducts = query.list();
	
		    List<shppee_q_byproducts> listQuotaByProducts = new ArrayList<>();
		    
		    List<Ecsce_CustomerExcepSub> listExceptionByProducts = new ArrayList<>();
		    
		    if(Interface.equals("cotizacion")) {
		    	OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance().createCriteria(Opcrmopportunities.class);
		    	queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
		    	queryOpportunity.setMaxResults(1);
		    	if(queryOpportunity.list().size() <= 0) {
	    			throw new Exception("No se ha encontrado una oportunidad con el documento ingresado "+No_Opportunity);
		    	}
		    	
		    	Opcrmopportunities opportunity = queryOpportunity.list().get(0);
		    	scsl_Product codeProd=opportunity.getShppwsOpProductcode();
		    	String codeProduct=codeProd.getValidationCode();
		    	String storeGroup=opportunity.getShppwsOpShopgroup();
		    	String endSegment=opportunity.getShppwsOpEndsegment();
		    	
		    	Ecsce_CustomerException client = ifExistExceptionClient(opportunity);
		    	if(client == null) {
		    		OBCriteria<shppee_Quotas> matriz1= OBDal.getInstance().createCriteria(shppee_Quotas.class); 
		  		    List<shppee_Quotas> quotasScores = matriz1.list();
		  		    String aux="";
		  	        for (shppee_Quotas quotasScore : quotasScores) {
		  	        	scsl_Product objProduct = quotasScore.getScslProduct();
		  	        	String product_code=objProduct.getValidationCode();
		  	        	if(codeProduct.equals(product_code)) {
		  	        		if(storeGroup.equals(quotasScore.getAgencyCode())) {
		  			            if (endSegment.equals(quotasScore.getENDSegment())) {
		  			            	OBCriteria<shppee_q_byproducts> queryQbyProducts = OBDal.getInstance().createCriteria(shppee_q_byproducts.class);
		  			            	queryQbyProducts.add(Restrictions.eq(shppee_q_byproducts.PROPERTY_SHPPEEQUOTAS, quotasScore));
		  			            	listQuotaByProducts = queryQbyProducts.list();
		  			            }
		  	        		}
		  	        	}
		  	        }
		    	}else {
		    		OBCriteria<Ecsce_CustomerExcepSub> queryExceptionByProducts = OBDal.getInstance().createCriteria(Ecsce_CustomerExcepSub.class);
		    		queryExceptionByProducts.add(Restrictions.eq(Ecsce_CustomerExcepSub.PROPERTY_ECSCECUSTOMEREXCEPTION, client));
		    		queryExceptionByProducts.add(Restrictions.eq(Ecsce_CustomerExcepSub.PROPERTY_ACTIVE, true));
		    		listExceptionByProducts = queryExceptionByProducts.list();
		    		
		    		if(listExceptionByProducts.size() <= 0) {
		    			throw new Exception("No se encuentran subproductos parametrizados, registre el subproducto en la Matriz de Excepción");
		    		}
		    	}
			}
		    
		    int sizeListExceptionByProducts = listExceptionByProducts.size();//subproductos de Excepcion
		    int sizeListQuotaByProducts = listQuotaByProducts.size();//subproductos de cupos
		    int size = AllByProducts.size();//subproductos
		    
		    //JSONObject product = new JSONObject();
		    if (size > 0) {
				Collections.sort(AllByProducts, (e1, e2) -> e2.getDeadLine().compareTo(e1.getDeadLine()));
		       
		    	JSONArray subproducts = new JSONArray();
		    	
		    	OBCriteria<scsl_Creditservices> queryCreditServices = OBDal.getInstance().createCriteria(scsl_Creditservices.class);
			    queryCreditServices.add(Restrictions.eq(scsl_Creditservices.PROPERTY_SCSLPRODUCT, productscsl));
			    List<scsl_Creditservices> listobjCreditServices = queryCreditServices.list();
			    scsl_Creditservices objCreditServices = null;
			    scsl_Creditservices objCreditAsistance = null;
				for (scsl_Creditservices objCreditService : listobjCreditServices) {
					String auxname = objCreditService.getSwsslTypeprod() != null ? objCreditService.getSwsslTypeprod(): "";// getCommercialName
					if (auxname.equals("SEV")) {// Servicio
						objCreditServices = objCreditService;
					} else if (auxname.equals("ATT")) {// Asistencia
						objCreditAsistance = objCreditService;
					}
				}
		        
				for (scsl_Byproducts byproduct : AllByProducts) {
					Long deadLine = byproduct.getDeadLine();
					BigDecimal comisionFactor = byproduct.getCommissionFactor().divide(BigDecimal.valueOf(100));

					BigDecimal comision = comisionFactor.multiply(BigDecimal.valueOf(amountFinance));
					BigDecimal totalDeuda = (BigDecimal.valueOf(amountFinance).add(comision)).setScale(2,
							RoundingMode.HALF_UP);
					BigDecimal cuotaTotal = totalDeuda.divide(BigDecimal.valueOf(deadLine), RoundingMode.DOWN);

					JSONObject subproduct = new JSONObject();
					subproduct.put("Name", byproduct.getCommercialName());
					subproduct.put("Term_Code", byproduct.getValidationCode());
					subproduct.put("Term", byproduct.getDeadLine());
					subproduct.put("Total_Due", totalDeuda);
					subproduct.put("Quota_Value", cuotaTotal.setScale(2, RoundingMode.HALF_UP));
					subproduct.put("Currency_Symbol", objcurrency.getSymbol());
					subproduct.put("Currency_Code", objcurrency.getISOCode());

					if (objCreditAsistance != null && objCreditServices != null) {
						Double capital = amountFinance / deadLine;
						Double preAmount = amountFinance * comisionFactor.doubleValue();
						Double auxAmounts = preAmount / deadLine;
						Double assistance = auxAmounts * ((objCreditAsistance.getFactor().doubleValue()) / 100);
						Double service = auxAmounts * ((objCreditServices.getFactor().doubleValue()) / 100);

						BigDecimal value_capital = new BigDecimal(capital).setScale(2, RoundingMode.HALF_UP);
						BigDecimal value_assistance = new BigDecimal(assistance).setScale(2, RoundingMode.HALF_UP);
						BigDecimal value_service = new BigDecimal(service).setScale(2, RoundingMode.HALF_UP);
						cuotaTotal = value_capital.add(value_assistance).add(value_service);
						subproduct.put("Quota_Value", cuotaTotal);
					}

					if(sizeListExceptionByProducts > 0) {// Cotizacion Excepciones
						String byprod = byproduct.getValidationCode();
						for (Ecsce_CustomerExcepSub objExceptionByProducts : listExceptionByProducts) {
							scsl_Byproducts qbyprod = objExceptionByProducts.getScslByproducts();
							String codeQbyProd = qbyprod.getValidationCode();
							if (byprod.equals(codeQbyProd)) {
								subproducts.put(subproduct);// agrega solo validados
							}
						}
					} else if (sizeListQuotaByProducts > 0) {// Cotizacion
						String byprod = byproduct.getValidationCode();
						for (shppee_q_byproducts objQbyProducts : listQuotaByProducts) {
							scsl_Byproducts qbyprod = objQbyProducts.getScslByproducts();
							String codeQbyProd = qbyprod.getValidationCode();
							if (byprod.equals(codeQbyProd)) {
								subproducts.put(subproduct);// agrega solo validados
							}
						}
					} else if (Interface.equals("simulacion")) {// Simulacion
						subproducts.put(subproduct);// agrega todo
					}
				}
		        
		        product.put("Product", Code_Product); // Codigo del producto
		        product.put("Subproducts", getSubProducts(productID, requestJson, subproducts));
		    }
	    
	    noReference = Identifier;
		}catch(Exception e) {
			product.put("Product","ERROR");
			product.put("message", e.getMessage());
			Error = "data error "+e.getMessage();
			Status = "ERROR";
		}


	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        JSONArray jsonArray = new JSONArray();
	        jsonArray.put(product);
	        String json = jsonArray.getJSONObject(0).toString();
	        PrintWriter writer = response.getWriter();
	        writer.write(json);
	        writer.close();
	        
	        String requestUrl = request.getRequestURL().toString();
	        
	        try {
		        String InterfaceLOG = "SHPPWS_NT";
			    String Process = "Simulacion/Cotizacion";
			    String idRegister = "";
			    logger.log_end_register(log, requestUrl, noReference, json, Status, "OUT", InterfaceLOG, Process, idRegister, Error);
			}catch(Exception e) {
				log4j.debug(e.getMessage()+" Error al guardar log final crsimulator: "+e);
			}
		
	}

	
	public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public JSONObject getJSON(HttpServletRequest request) throws Exception {
        StringBuilder requestBody = new StringBuilder();
        JSONObject requestJSON = new JSONObject();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            requestJSON = new JSONObject(requestBody.toString());
        }catch(Exception e) {
        	log4j.debug(e.getMessage()+" Error: "+e);
        }
		return requestJSON;
	}
	
	public Ecsce_CustomerException ifExistExceptionClient(Opcrmopportunities opportunity) throws Exception {
		Ecsce_CustomerException client = null;
		
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        currentDate = calendar.getTime();
		
		BusinessPartner BP = opportunity.getBusinessPartner();
		String taxID = BP.getTaxID();
		
		scsl_Product Product = opportunity.getShppwsOpProductcode();
		String storeGroup = opportunity.getShppwsOpShopgroup();
		String endSegment = opportunity.getShppwsOpEndsegment();
		
		OBCriteria<Ecsce_CustomerException> queryExceptionCli = OBDal.getInstance().createCriteria(Ecsce_CustomerException.class);
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_TAXID, taxID));
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_ACTIVE, true));
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_SCSLPRODUCT, Product));
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_STOREGROUP, storeGroup));
		queryExceptionCli.add(Restrictions.eq(Ecsce_CustomerException.PROPERTY_FINALSEGMENT, endSegment));
		
		queryExceptionCli.add(Restrictions.le(Ecsce_CustomerException.PROPERTY_STARTINGDATE, currentDate)); // startingDate <= currentDate
	    queryExceptionCli.add(Restrictions.ge(Ecsce_CustomerException.PROPERTY_DATEUNTIL, currentDate));   // dateUntil >= currentDate
		List<Ecsce_CustomerException> listClientException = queryExceptionCli.list();
		
		
		if(listClientException.size() > 0) {
			client = listClientException.get(0);
		}
		return client;
	}
	
	public JSONArray getSubProducts(String prodID, JSONObject requestJSON, JSONArray SubProducts) throws Exception {
		
		if(requestJSON.has("Servicios_Adicionales")) {
			JSONArray Servicios_Adicionales = requestJSON.getJSONArray("Servicios_Adicionales");
			BigDecimal valorAdicional = BigDecimal.ZERO; 
	        Set<String> seenNames = new HashSet<>();
			for (int i = 0; i < Servicios_Adicionales.length(); i++) {
		        JSONObject servicioJSON = Servicios_Adicionales.getJSONObject(i);
		        validateStructure(servicioJSON);
		        String  Name = servicioJSON.getString("Name");
		        BigDecimal Amount = new BigDecimal(servicioJSON.getString("Amount"));
		        serviceInBDD(prodID, Name);
		        valorAdicional = valorAdicional.add(Amount);
		        
		        if (seenNames.contains(Name)) {
	                throw new Exception("Nombre duplicado en servicios adicionales: " + Name);
	            }
	            seenNames.add(Name);
		    }
			
			if (valorAdicional.compareTo(BigDecimal.ZERO) > 0) {
				for (int i = 0; i < SubProducts.length(); i++) {
			        JSONObject subProduct = SubProducts.getJSONObject(i);
			        BigDecimal Term = new BigDecimal(subProduct.getString("Term"));
			        BigDecimal Quota_Value = new BigDecimal(subProduct.getString("Quota_Value"));
			        BigDecimal Total_Due = new BigDecimal(subProduct.getString("Total_Due"));
			        
			        Quota_Value = Quota_Value.add(valorAdicional);
			        Total_Due = Quota_Value.multiply(Term);
			        
			        subProduct.put("Quota_Value", Quota_Value);
			        subProduct.put("Total_Due", Total_Due);
			    }
			}
		}
		
		return SubProducts;
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
	
	public Boolean serviceInBDD(String prodID, String code) throws Exception {
		Boolean validator = false;
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
			validator = true;
		}else {
			throw new Exception("Error en simulacion/cotizacion. No se encuentra parametrizado el Servicio Adicional "+code);
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
}
