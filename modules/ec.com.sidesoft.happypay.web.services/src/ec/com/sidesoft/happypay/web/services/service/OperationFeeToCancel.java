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

public class OperationFeeToCancel implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		String creditOperationId = request.getParameter("creditOperationId");
		
		JSONArray customer_data = new JSONArray();
		
	    ConnectionProvider conn = new DalConnectionProvider(false);
	    
	    try {
	    	Connection conndb = conn.getTransactionConnection();
	        String strSql = null, strSqlOperation = null;
	       
	        
	        strSql = "SELECT documentno FROM c_invoice WHERE documentno = '" +creditOperationId+ "'";
	       
	        strSqlOperation = "SELECT \n" + 
        			"ci.c_invoice_id AS c_invoice_id, \n" + 
        			"ci.documentno AS documentno, \n" + 
        			"ci.description AS description, \n"+ 
        			"ci.EM_Shpic_Proxdues AS duedate, \n" + 
        			"SUM(ROUND(ci.OutstandingAmt, 2)) AS amount,\n" + 
        			"cb.taxid AS taxid, \n" + 
        			"cb.EM_Ssscrbp_Name || ' ' || cb.EM_Ssscrbp_Name2 AS namecustomer, \n" +
        			"cb.EM_Ssscrbp_Lastname || ' ' || cb.EM_Ssscrbp_Lastname2 AS surnamecustomer, \n" +
        			"og.name AS nameorg, \n" +
        			"ROW_NUMBER() OVER (ORDER BY ci.CREATED ASC, ci.DOCUMENTNO) AS priority, \n" +
        			"(ci.EM_Shpic_Mostover_Install - ci.EM_Shpic_Advancevalue) AS AmounToPay, \n" +
        			"(ci.EM_Shpic_Mostover_Install - ci.EM_Shpic_Advancevalue) AS MinimumAmount, \n" +
        			"(ci.EM_Shpic_Mostover_Install - ci.EM_Shpic_Advancevalue) AS SuggestedAmountToPay \n" +
        	"FROM c_invoice ci \n" +
        	"JOIN c_doctype dt ON dt.c_doctype_id = ci.c_doctype_id \n" +
        	"JOIN c_bpartner cb ON cb.c_bpartner_id = ci.c_bpartner_id \n" +
        	"JOIN ad_org og ON og.ad_org_id = ci.ad_org_id \n" +
        			"WHERE ci.OutstandingAmt > 0 \n" +
        			"AND ci.IsSOTrx = 'Y' \n" +
        			"AND dt.docbasetype = 'ARI' \n" +
        			"AND ci.documentno = '"+creditOperationId+"' \n" +
        	"GROUP BY ci.c_invoice_id, ci.documentno, ci.description, cb.taxid, EM_Ssscrbp_Name, EM_Ssscrbp_Name2, cb.EM_Ssscrbp_Lastname, cb.EM_Ssscrbp_Lastname2, og.name, ci.EM_Shpic_Noduesmorover, ci.EM_Shpic_Advancevalue \n" +
        	"ORDER BY duedate ASC";
	       
	       
	        
	        PreparedStatement st = null;

	        st = conndb.prepareStatement(strSql);
	        ResultSet rsConsulta = st.executeQuery();
	        
	        PreparedStatement stope = null;

	        stope = conndb.prepareStatement(strSqlOperation);
	        ResultSet rsOperation = stope.executeQuery();
	        
	        if (rsConsulta.next()) {
	        	
		        if (rsOperation.next()) {
		        	do {
		        		JSONObject credit = new JSONObject();
		        		
		        		credit.put("CreditOperationId",
		        				rsOperation.getString("documentno") != null ? rsOperation.getString("documentno")
		        	                : "");
		        		credit.put("Priority",
		        				rsOperation.getString("priority") != null ? rsOperation.getString("priority")
		        	                : "");
		        		credit.put("Description",
		        				rsOperation.getString("description") != null ? rsOperation.getString("description")
		        	                : "");
		        		credit.put("DateProxQuote",
		        				rsOperation.getString("duedate") != null ? rsOperation.getString("duedate")
		        					: "");
		        		credit.put("AmounToPay",
		        				rsOperation.getString("AmounToPay") != null ? rsOperation.getString("AmounToPay")
		        	                : "");
		        		credit.put("MinimumAmountTopay",
		        				rsOperation.getString("AmounToPay") != null ? rsOperation.getString("AmounToPay")
		        	                : "");
		        		credit.put("SuggestedAmountTopay",
		        				rsOperation.getString("AmounToPay") != null ? rsOperation.getString("AmounToPay")
		        	                : "");
		        		credit.put("Customerld",
		        				rsOperation.getString("taxid") != null ? rsOperation.getString("taxid")
		        	                : "");
		        		credit.put("CustomerSurnames",
		        				rsOperation.getString("surnamecustomer") != null ? rsOperation.getString("surnamecustomer")
		        	                : "");
		        		credit.put("CustomerNames",
		        				rsOperation.getString("namecustomer") != null ? rsOperation.getString("namecustomer")
			        	             : "");
		        		credit.put("Company",
		        				rsOperation.getString("nameorg") != null ? rsOperation.getString("nameorg")
		        	                : "");
		        		
		        		customer_data.put(credit);

		        	}while(rsOperation.next());
		        	
		        	
		        } else {
		        	JSONObject emptyData = new JSONObject();
			        emptyData.put("message", "No existe un valor pendiente.");
			        customer_data.put(emptyData);
		        }
	        	
	        } else {
	        	JSONObject emptyData = new JSONObject();
		        emptyData.put("message", "El identificador de la operación no existe.");
		        customer_data.put(emptyData);
	        }
		        
	        
	     // Close ConnectionProvider
	        conndb.close();
	        // Close PreparedStatement
	        st.close();
	        stope.close();
	        // Close ResultSet
	        rsConsulta.close();
	        rsOperation.close();
	        
	    	
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
