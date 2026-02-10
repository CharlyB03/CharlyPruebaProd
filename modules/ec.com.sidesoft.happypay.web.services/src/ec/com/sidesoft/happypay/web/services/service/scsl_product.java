package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.common.actionhandler.GetConvertedQtyActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
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
import ec.com.sidesoft.credit.simulator.scsl_Byproducts;
import ec.com.sidesoft.credit.simulator.scsl_Product;
import ec.com.sidesoft.happypay.web.services.shppws_config;

public class scsl_product implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
        String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "scsl_product", requestParameters);
		
	    OBCriteria<scsl_Product> queryproducts = OBDal.getInstance().createCriteria(scsl_Product.class);
	    queryproducts.add(Restrictions.eq(scsl_Product.PROPERTY_ACTIVE,true));
	    List<scsl_Product> listProducts = queryproducts.list();
	    
	    int size = listProducts.size();
	    
	    JSONArray products = new JSONArray();
	    if (size > 0) {
	        for (scsl_Product prod : listProducts) {

	            JSONObject product = new JSONObject();
	            product.put("creationDate", prod.getCreationDate());
	            product.put("updated", prod.getUpdated());
	            product.put("name", prod.getCommercialName());
	            product.put("description", prod.getDescription());
	            product.put("refinancing", prod.isRefinancing());
	             String frequency = prod.getFrequency();
		            if(frequency.equals("WK")) {
		            	frequency = "Semanal";
		            } else if (frequency.equals("BK")) {
		            	frequency = "Quincenal";
		            }else if (frequency.equals("MT")) {
		            	frequency = "Mensual";
		            }
	            product.put("frequency", frequency);
	            product.put("conversionRate", prod.getConversionRate());
	            product.put("typeGuarantee", prod.getTypeGuarantee());
	            product.put("collectionExpenses", prod.isCollectionExpenses());
	            product.put("observations", prod.getObservations());
	            product.put("message", prod.getMessage());
	            product.put("code", prod.getValidationCode());
	            product.put("Active", prod.isActive());
	            

	            products.put(product);
	        }
	    }else {
	    	JSONObject emptyData = new JSONObject();
	        emptyData.put("message", "Data is empty");
	        products.put(emptyData);
	    }
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	       
	        String json = products.toString();
	        PrintWriter writer = response.getWriter();
	        writer.write(json);
	        writer.close();

	        String requestUrl = request.getRequestURL().toString();
	        
	        try {
	        	String noReference = "";
		        String Interface = "SHPPWS_NT";
			    String Process = "Productos Crédito";
			    String idRegister = "";
			    String Error = "";
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
