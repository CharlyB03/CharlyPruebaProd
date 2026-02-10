package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang.StringUtils;

import org.openbravo.service.web.WebService;
import java.io.PrintWriter;

import org.codehaus.jettison.json.JSONException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.plm.AttributeValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.DalConnectionProvider;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.Location;
import java.lang.reflect.Type;
import java.util.HashMap;

public class CustomerFeeToCancel implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		String customerId = request.getParameter("customerId");
		
		JSONArray customer_data = new JSONArray();
		
	    ConnectionProvider conn = new DalConnectionProvider(false);
	    
	    try {
	    	Connection conndb = conn.getTransactionConnection();
	        String strSql = null, strSqlCustomer = null;
	        
	        strSql = "SELECT taxid FROM c_bpartner\n"
	                + "WHERE iscustomer = 'Y' AND taxid = '" +customerId+ "'";

	        strSqlCustomer = "SELECT \n" + 
        			"ci.c_invoice_id AS c_invoice_id, \n" + 
        			"ci.documentno AS documentno, \n" + 
        			"ci.description AS description, \n"+ 
        			"ci.EM_Shpic_Proxdues AS duedate, \n" + 
        			"SUM(ROUND(ci.OutstandingAmt, 2)) AS amount,\n" + 
        			"cb.taxid AS taxid, \n" + 
        			"cb.EM_Ssscrbp_Name || ' ' || cb.EM_Ssscrbp_Name2 AS namecustomer, \n" +
        			"cb.EM_Ssscrbp_Lastname || ' ' || cb.EM_Ssscrbp_Lastname2 AS surnamecustomer, \n" + 
        			"og.name AS nameorg, \n" +
        			"ROW_NUMBER() OVER (ORDER BY ci.EM_Shpic_Proxdues ASC, ci.DOCUMENTNO) AS priority, \n" +
        			"(ci.EM_Shpic_Mostover_Install - ci.EM_Shpic_Advancevalue) AS AmounToPay, \n" +
        			"(ci.EM_Shpic_Mostover_Install - ci.EM_Shpic_Advancevalue) AS MinimumAmount, \n" +
        			"(ci.EM_Shpic_Mostover_Install - ci.EM_Shpic_Advancevalue) AS SuggestedAmountToPay \n" +
        	"FROM c_invoice ci \n" +
        	"JOIN c_doctype dt ON dt.c_doctype_id = ci.c_doctype_id \n" +
        	"JOIN c_bpartner cb ON cb.c_bpartner_id = ci.c_bpartner_id \n" +
        	"JOIN ad_org og ON og.ad_org_id = ci.ad_org_id \n" +
        			"WHERE ci.outstandingamt > 0 \n" +
        			"AND ci.processed = 'Y' \n" +
        			"AND ci.IsSOTrx = 'Y' \n" +
        			"AND dt.docbasetype = 'ARI' \n" +
        			"AND dt.em_shpfr_iscreditope = 'Y' \n" +
        			"AND cb.taxid = '"+customerId+"' \n" +
        	"GROUP BY ci.c_invoice_id, ci.documentno, ci.description, cb.taxid, cb.EM_Ssscrbp_Name, cb.EM_Ssscrbp_Name2, cb.EM_Ssscrbp_Lastname, cb.EM_Ssscrbp_Lastname2, og.name, ci.EM_Shpic_Noduesmorover, ci.EM_Shpic_Advancevalue \n" +
        	"ORDER BY \n" +
        	"CASE \n" +
        	"WHEN EXTRACT(YEAR FROM ci.EM_Shpic_Proxdues) >= 2023 THEN 0 \n" +
        	"ELSE 1 \n" +
        	"END, \n" +
        	"ci.EM_Shpic_Proxdues ASC;";
        
	        
	        PreparedStatement st = null;

	        st = conndb.prepareStatement(strSql);
	        ResultSet rsConsulta = st.executeQuery();
	        
	        PreparedStatement stcust = null;

	        stcust = conndb.prepareStatement(strSqlCustomer);
	        ResultSet rsCustomer = stcust.executeQuery();
	        
	        if (rsConsulta.next()) {
	        	
		        if (rsCustomer.next()) {
		        	do {
		        		JSONObject credit = new JSONObject();
		        		

		        		credit.put("CreditOperationId",
		        				rsCustomer.getString("documentno") != null ? rsCustomer.getString("documentno")
		        	                : "");
		        		credit.put("Priority",
		        				rsCustomer.getString("priority") != null ? rsCustomer.getString("priority")
		        	                : "");
		        		credit.put("Description",
		        				rsCustomer.getString("description") != null ? rsCustomer.getString("description")
		        			             : "");
		                credit.put("DateProxQuote",
		                		rsCustomer.getString("duedate") != null ? rsCustomer.getString("duedate")		        						
		        	                : "");
		        		credit.put("AmounToPay",
		        				rsCustomer.getString("AmounToPay") != null ? rsCustomer.getString("AmounToPay")
		        	                : "");
		        		credit.put("MinimumAmountTopay",
		        				rsCustomer.getString("AmounToPay") != null ? rsCustomer.getString("AmounToPay")
		        	                : "");
		        		credit.put("SuggestedAmountTopay",
		        				rsCustomer.getString("AmounToPay") != null ? rsCustomer.getString("AmounToPay")
		        	                : "");
		        		credit.put("Customerld",
		        				rsCustomer.getString("taxid") != null ? rsCustomer.getString("taxid")
		        	                : "");
		        		credit.put("CustomerSurnames",
		        				rsCustomer.getString("surnamecustomer") != null ? rsCustomer.getString("surnamecustomer")
		        	                : "");
		        		credit.put("CustomerNames",
		        				rsCustomer.getString("namecustomer") != null ? rsCustomer.getString("namecustomer")
			        	                : "");
		        		credit.put("Company",
		        				rsCustomer.getString("nameorg") != null ? rsCustomer.getString("nameorg")
		        	                : "");
		        		customer_data.put(credit);

		        	}while(rsCustomer.next());
		        	
		        	
		        } else {
		        	JSONObject emptyData = new JSONObject();
			        emptyData.put("message", "No existe créditos abiertos para este cliente.");
			        customer_data.put(emptyData);
		        }
	        	
	        } else {
	        	JSONObject emptyData = new JSONObject();
		        emptyData.put("message", "El identificar del cliente no existe.");
		        customer_data.put(emptyData);
	        }
		        
	        
	     // Close ConnectionProvider
	        conndb.close();
	        // Close PreparedStatement
	        st.close();
	        stcust.close();
	        // Close ResultSet
	        rsConsulta.close();
	        rsCustomer.close();
	        
	    	
	    } catch (Exception e) {
            System.out.println("getCustomer: " + e.getMessage());
      }
	    
	    response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = customer_data.toString();
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.close();

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