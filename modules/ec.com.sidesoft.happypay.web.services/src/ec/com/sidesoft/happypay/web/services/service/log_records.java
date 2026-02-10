package ec.com.sidesoft.happypay.web.services.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.web.services.shppws_config;

public class log_records {
  String jsonresponse = "";

  public Scactu_Log log_start_register(shppws_config accesApi, String apiEndPoint,
      String requestJSON) throws SQLException {
    OBDal.getInstance().getSession().beginTransaction();
    // 1715857908
    Scactu_Log log = OBProvider.getInstance().get(Scactu_Log.class);
    log.setClient(accesApi.getClient());
    log.setOrganization(accesApi.getOrganization());
    log.setEndpoint(apiEndPoint);
    if (requestJSON != null)
      log.setJsonRequest(requestJSON);
    if (apiEndPoint.equals("profiling")) {
      String start = "ID=";
      String end = "&";
      String separador = " - ";
      int startIndex = requestJSON.indexOf(start) + start.length();
      int endIndex = requestJSON.indexOf(end, startIndex);
      jsonresponse = separador + ((endIndex != -1) ? requestJSON.substring(startIndex, endIndex)
          : requestJSON.substring(startIndex));
    }
    if (apiEndPoint.equals("curl")) {
      JSONObject json = new JSONObject(requestJSON);
      String identifier = json.has("Identifier") ? json.getString("Identifier") : "";
      jsonresponse = " - " + identifier;
    }
    Date date = new Date();
    log.setShppwsStartTime(date);
    OBDal.getInstance().save(log);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(log);
    OBDal.getInstance().getConnection().commit();

    // OBDal.getInstance().getSession().getTransaction().commit();

    return log;
  }

  public Scactu_Log log_setValues(Scactu_Log log, String requestJSON) {
    log.setJsonRequest(requestJSON);
    OBDal.getInstance().save(log);
    OBDal.getInstance().flush();
    return log;
  }

  public void log_end_register(Scactu_Log log, String apiUrl, String Identifier,
      String responseJSON, String result, String type, String Interface, String Process,
      String idRegister, String Error) {
    try {
      Date date = new Date();
      // log.setEndpoint(apiEndPoint);
      // log.setJsonRequest(requestJSON);
      log.setJsonResponse(responseJSON);
      log.setResult(result);
      log.setType(type);
      log.setReferenceNo(Identifier + jsonresponse);
      log.setInterface(Interface);
      log.setShppwsProcess(Process);
      log.setRecordID(idRegister);
      log.setError(Error);
      log.setShppwsEndTime(date);
      OBDal.getInstance().save(log);
      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(log);
      log_total_time(log);
      OBDal.getInstance().getConnection().commit();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void log_total_time(Scactu_Log log) {
    try {
      long diferenciaMillis = log.getShppwsEndTime().getTime() - log.getShppwsStartTime().getTime();
      // long diferenciaSeg = TimeUnit.MILLISECONDS.toSeconds(diferenciaMillis);
      log.setShppwsTotalTime(new BigDecimal(diferenciaMillis));
      OBDal.getInstance().save(log);
      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(log);
    } catch (Exception e) {
      log.setResult("ERROR");
      log.setJsonResponse("ERROR LOG");
      OBDal.getInstance().save(log);
      OBDal.getInstance().flush();
    }

  }

}
