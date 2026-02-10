package ec.com.sidesoft.happypay.web.services.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.service.web.WebService;

import java.io.PrintWriter;
import java.util.List;


public class Available_Quota implements WebService{

	@Override
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		String Identifier = null;
		Double quotaAvailable = new Double(0);
		JSONObject respuesta = new JSONObject();
		
		try {
			Identifier = request.getParameter("Identifier");
			if (Identifier != null && !(Identifier.equals(""))) {
				OBCriteria<BusinessPartner> querypartner = OBDal.getInstance().createCriteria(BusinessPartner.class);
				querypartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, Identifier));
				List<BusinessPartner> listPartner = querypartner.list();
				if (listPartner.size() > 0) {
					if (listPartner.size() == 1) {
						quotaAvailable = listPartner.get(0).getShppwsAvailableQuota().doubleValue();
						respuesta.put("Identifier", Identifier);
						respuesta.put("quotaAvailable", quotaAvailable);
						respuesta.put("message", "succes");
					}
				} else {
					throw new Exception("Usuario no encontrado con el identificador " + Identifier);
				}
			} else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//-->400
				throw new Exception("El parámetro Identifier es necesario");
			}
		} catch (Exception e) {
			respuesta.put("Identifier", Identifier);
			respuesta.put("quotaAvailable", quotaAvailable);
			respuesta.put("message", e.getMessage());
		}
		
		response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = respuesta.toString();
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.close();
	}

	@Override
	public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
