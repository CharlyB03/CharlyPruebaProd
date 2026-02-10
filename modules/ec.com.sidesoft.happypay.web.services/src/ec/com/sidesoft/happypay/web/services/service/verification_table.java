package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
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
import ec.com.sidesoft.credit.factory.SscfArtboard;
import ec.com.sidesoft.credit.factory.SscfArtboardUser;
import ec.com.sidesoft.credit.factory.SscfCallCenterTask;
import ec.com.sidesoft.credit.factory.SscfCom1;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.credit.factory.SscfTaskCallCenter;
import ec.com.sidesoft.fast.quotation.ECSFQ_Quotation;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_Quotas;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.ad_process.new_binnacle_opcredit;
import ec.com.sidesoft.happypay.web.services.monitor.MonitorManager;
import it.openia.crm.Opcrmopportunities;
import org.openbravo.model.ad.access.User;

import java.util.Base64;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException.Reason;
import java.time.LocalDateTime;

public class verification_table implements WebService{
	private static final long serialVersionUID = 1L;
	
	public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String Error = "";
		JSONObject jsonMonitor = new JSONObject();
		jsonMonitor.put("SHPPWS_SideSoft_VTable", "Service"+0);
	    jsonMonitor.put("startSHPPWS_SideSoft_VTable", LocalDateTime.now());
	    jsonMonitor.put("typeSHPPWS_SideSoft_VTable", "Creación del Crédito");
		
		OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
		shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
		log_records logger = new log_records();
		String requestParameters =  request.getQueryString();
		Scactu_Log log = logger.log_start_register(accesApi, "verification_table", requestParameters);
		String No_Opportunity = request.getParameter("No_Opportunity");
		
		JSONArray records = new JSONArray();
		JSONObject record = new JSONObject();
		try {
			OBCriteria<Opcrmopportunities> queryOpportunity = OBDal.getInstance()
					.createCriteria(Opcrmopportunities.class);
			queryOpportunity.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, No_Opportunity));
			List<Opcrmopportunities> listobjOpportunity = queryOpportunity.list();
			if(listobjOpportunity.size() == 0) {
				throw new Exception("No se ha encontrado la Oportunidad");
			}
			Opcrmopportunities objOpportunity = listobjOpportunity.get(0);

			OBCriteria<ECSFQ_Quotation> queryobjFastQuotation = OBDal.getInstance().createCriteria(ECSFQ_Quotation.class);
			queryobjFastQuotation.add(Restrictions.eq(ECSFQ_Quotation.PROPERTY_OPCRMOPPORTUNITIES, objOpportunity));
			List<ECSFQ_Quotation> listFastQuotation = queryobjFastQuotation.list();
			ECSFQ_Quotation objFastQuotation = listFastQuotation.get(0);

			int sizeFQ = listFastQuotation.size();

			if (sizeFQ > 0) {
				OBCriteria<SscfCreditOperation> querySscfCreditOperation = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
				querySscfCreditOperation.add(Restrictions.eq(SscfCreditOperation.PROPERTY_SSCORORDER, objFastQuotation));
				List<SscfCreditOperation> listobjCreditOperation = querySscfCreditOperation.list();
				SscfCreditOperation objCreditOperation = listobjCreditOperation.get(0);

				try {
					String observation = "Mesa activada";
					String from_artboard = null;
					String to_artboard = "CC";
					if (to_artboard == null) {
						to_artboard = "CC";
					}
					String CallCenter = objCreditOperation.getCallCenterStatus();
					String SCom = objCreditOperation.getSComStatus();
					String COM2 = objCreditOperation.getCom2Status();
					String COM1 = objCreditOperation.getCom1Status();
					if (CallCenter != null && (CallCenter.equals("O") || CallCenter.equals("G")
							|| CallCenter.equals("Y") || CallCenter.equals("R"))) {
						from_artboard = "CC";
					} else if (SCom != null
							&& (SCom.equals("O") || SCom.equals("G") || SCom.equals("Y") || SCom.equals("R"))) {
						from_artboard = "S-COM";
					} else if (COM2 != null
							&& (COM2.equals("O") || COM2.equals("G") || COM2.equals("Y") || COM2.equals("R"))) {
						from_artboard = "COM2";
					} else if (COM1 != null
							&& (COM1.equals("O") || COM1.equals("G") || COM1.equals("Y") || COM1.equals("R"))) {
						from_artboard = "COM1";
					}

					objCreditOperation.setCom1Status("G");
					objCreditOperation.setCom2Status("G");
					objCreditOperation.setSComStatus("G");
					objCreditOperation.setCallCenterStatus("Y");
					objCreditOperation.setArtboardStatus("CC");

					objCreditOperation.setDocumentStatus("IP");// In Progress

					OBCriteria<SscfArtboard> querySscfArtboard = OBDal.getInstance().createCriteria(SscfArtboard.class);
					querySscfArtboard.add(Restrictions.eq(SscfArtboard.PROPERTY_NAME, "CC"));
					List<SscfArtboard> listobjfArtboard = querySscfArtboard.list();
					SscfArtboard objfArtboard = listobjfArtboard.get(0);

					OBCriteria<SscfArtboardUser> querySscfArtboardUser = OBDal.getInstance().createCriteria(SscfArtboardUser.class);
					querySscfArtboardUser.add(Restrictions.eq(SscfArtboardUser.PROPERTY_SSCFARTBOARD, objfArtboard));
					List<SscfArtboardUser> listobjArtboardUsers = querySscfArtboardUser.list();

					if (listobjArtboardUsers.size() > 0) {
						Map<User, Integer> userCreditCount = new HashMap<>();

						for (SscfArtboardUser artboardUser : listobjArtboardUsers) {
							User user = artboardUser.getUserContact();
							OBCriteria<SscfCreditOperation> queryCreditOps = OBDal.getInstance().createCriteria(SscfCreditOperation.class);
							queryCreditOps.add(Restrictions.eq(SscfCreditOperation.PROPERTY_CALLCENTERUSER, user));
							queryCreditOps.add(Restrictions.eq(SscfCreditOperation.PROPERTY_DOCUMENTSTATUS, "IP"));

							int creditCount = queryCreditOps.count();
							userCreditCount.put(user, creditCount);
						}

						List<Map.Entry<User, Integer>> entryList = new ArrayList<>(userCreditCount.entrySet());
						Collections.sort(entryList, Map.Entry.comparingByValue());

						if (!entryList.isEmpty()) {
							Map.Entry<User, Integer> userWithMinAssignments = entryList.get(0);
							User user = userWithMinAssignments.getKey();
							objCreditOperation.setCallCenterUser(user);
							// record.put("user",user.getName());
						} else {
							// record.put("user","No hay usuarios");
						}

					}

					// objCreditOperation.setCom1User(accesApi.getCreatedBy());
					OBDal.getInstance().save(objCreditOperation);
					OBDal.getInstance().flush();

					if (from_artboard != null && to_artboard != null) {
						new_binnacle_opcredit binnacle = new new_binnacle_opcredit();
						binnacle.createBinnacle(objCreditOperation, from_artboard, to_artboard,objCreditOperation.getDocumentStatus(), observation);
					}

					record.put("No_Opportunity", objOpportunity.getShppwsOpDocumentno());
					record.put("Verification", "Ok");
				} catch (Exception e) {
			        throw new Exception( e.getMessage());
				}

				try {
					OBCriteria<SscfCom1> queryCOM = OBDal.getInstance().createCriteria(SscfCom1.class);
					queryCOM.add(Restrictions.eq(SscfCom1.PROPERTY_SSCFCREDITOPERATION, objCreditOperation));
					List<SscfCom1> listqueryCOM = queryCOM.list();

					if (listqueryCOM.size() > 0) {
						SscfCom1 objCOM = listqueryCOM.get(0);

						OBCriteria<SscfCallCenterTask> queryCCTask = OBDal.getInstance().createCriteria(SscfCallCenterTask.class);
						queryCCTask.add(Restrictions.eq(SscfCallCenterTask.PROPERTY_SSCFCOM1ID, objCOM));
						List<SscfCallCenterTask> listCCTask = queryCCTask.list();

						if (listCCTask.size() <= 0) {

							OBCriteria<SscfTaskCallCenter> queryOriginTasks = OBDal.getInstance().createCriteria(SscfTaskCallCenter.class);
							queryOriginTasks.add(Restrictions.eq(SscfTaskCallCenter.PROPERTY_ANALYSISTYPE,objCreditOperation.getAnalysisType()));
							List<SscfTaskCallCenter> listOriginTasks = queryOriginTasks.list();
							if (listOriginTasks.size() > 0) {
								Long lineTask = new Long(10);
								Long lineequence = new Long(10);
								for (SscfTaskCallCenter objOriginTask : listOriginTasks) {
									SscfCallCenterTask objCCTask = OBProvider.getInstance().get(SscfCallCenterTask.class);

									objCCTask.setClient(accesApi.getClient());
									objCCTask.setOrganization(accesApi.getOrganization());
									objCCTask.setActive(accesApi.isActive());
									objCCTask.setCreatedBy(accesApi.getCreatedBy());
									objCCTask.setUpdatedBy(accesApi.getUpdatedBy());

									objCCTask.setSscfTaskCallCenter(objOriginTask);
									objCCTask.setSscfCom1ID(objCOM);
									objCCTask.setLineNo(lineTask);
									objCCTask.setLink(objOriginTask.getLink());
									lineTask = lineTask + lineequence;
									OBDal.getInstance().save(objCCTask);
									OBDal.getInstance().flush();
								}
							}
						}

						try {
							Boolean validate = true;
							objCOM.setVLastname(validate);
							objCOM.setVLastName2(validate);
							objCOM.setVName(validate);
							objCOM.setVName2(validate);
							objCOM.setVTaxID(validate);
							objCOM.setVCivilStatus(validate);
							objCOM.setVAddress(validate);
							objCOM.setVPhone(validate);
							objCOM.setVCell(validate);
							OBDal.getInstance().save(objCOM);
							OBDal.getInstance().flush();
						} catch (Exception e) {
							throw new Exception( e.getMessage());
						}

					}

				} catch (Exception e) {
					throw new Exception( e.getMessage());
				}
			}

		} catch (Exception e) {
			record.put("No_Opportunity", No_Opportunity);
			record.put("Verification", "Error");
			Error = "Error al activar la mesa de Verificación "+e.getMessage();
		}
		
		records.put(record);

		// |||||||||||||||||||||||||||||||||||//
		// |||||||||||||RESULTADO|||||||||||||//
		// |||||||||||||||||||||||||||||||||||//

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = records.toString();
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
		jsonMonitor.put("endSHPPWS_SideSoft_VTable", LocalDateTime.now());

		String requestUrl = request.getRequestURL().toString();
		
		try {
        	String noReference = No_Opportunity;
	        String Interface = "SHPPWS_NT";
		    String Process = "Activación Mesa Verificación";
		    String idRegister = "";
		    if(Error.equals("")) {
			    logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", Interface, Process, idRegister, Error);
            }else {
			    logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "OUT", Interface, Process, idRegister, Error);
            }
        }catch(Exception e){}
	if(record.has("Verification") && record.getString("Verification").equals("Ok")) {
		jsonMonitor.put("statusSHPPWS_SideSoft_VTable", "200");
	}else {
		jsonMonitor.put("statusSHPPWS_SideSoft_VTable", "400");
	}
        jsonMonitor.put("Identifier", No_Opportunity);
	MonitorManager newMonitor = new MonitorManager();
	newMonitor.sendMonitorData(jsonMonitor, accesApi, true, null);
	//OBDal.getInstance().getSession().close();
		
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
