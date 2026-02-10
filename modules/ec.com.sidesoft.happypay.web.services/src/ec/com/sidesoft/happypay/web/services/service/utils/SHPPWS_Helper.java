package ec.com.sidesoft.happypay.web.services.service.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.soap.SOAPMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.module.Module;
import org.openbravo.service.db.DbUtility;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;

public abstract class SHPPWS_Helper {

  static public JSONObject getResponse(JSONObject data) throws Exception {
    JSONObject response = new JSONObject();
    response.put("status", 0);
    response.put("data", data);
    response.put("message", "");
    return response;
  }

  static public JSONObject getErrorResponse(String message) throws Exception {
    JSONObject response = new JSONObject();
    response.put("status", -1);
    response.put("data", new JSONObject());
    response.put("message", message);
    return response;
  }

  static public String post(String endpoint, JSONObject body, String token) throws Exception {
    URL url = new URL(endpoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Authorization", token);

    String jsonInputString = body.toString();
    OutputStream os = conn.getOutputStream();
    byte[] input = jsonInputString.getBytes("utf-8");
    os.write(input, 0, input.length);

    int status = conn.getResponseCode();
    if (status != 200 && status != 204) {
      throw new OBException(conn.getResponseMessage());
    }

    InputStream responseStream = conn.getInputStream();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line.trim());
      }
      return response.toString();
    }
  }

  static public String postActuaparToken(String endpoint, String username, String password)
      throws Exception {
    JSONObject body = new JSONObject();
    body.put("usuario", username);
    body.put("password", password);

    URL url = new URL(endpoint + "login");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");

    String jsonInputString = body.toString();
    OutputStream os = conn.getOutputStream();
    byte[] input = jsonInputString.getBytes("utf-8");
    os.write(input, 0, input.length);

    int status = conn.getResponseCode();
    if (status != 200 && status != 204) {
      throw new OBException(conn.getResponseMessage());
    }

    InputStream responseStream = conn.getInputStream();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line.trim());
      }
      return response.toString();
    }
  }

  static public String getErrorMessage(Logger logger, Exception e) {
    Throwable throwable = DbUtility.getUnderlyingSQLException(e);
    String message = OBMessageUtils.translateError(throwable.getMessage()).getMessage();
    logger.error(message);
    return message;
  }

  static public JSONObject readAllIntoJSONObject(InputStream content) throws Exception {
    BufferedReader rd = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));
    String jsonText = IOUtils.toString(rd);
    return new JSONObject(jsonText);
  }

  static public String getString(JSONObject json, String key, boolean required) throws Exception {
    String value = json.has(key) ? json.getString(key) : null;
    if (value == null || value.trim().isEmpty()) {
      if (required) {
        throw new OBException(key + " is required");
      }
      return null;
    }
    return value.trim();
  }

  static public Long getLong(JSONObject json, String key, boolean required) throws Exception {
    String value = json.has(key) ? json.getString(key) : null;
    if (value == null || value.trim().isEmpty()) {
      if (required) {
        throw new OBException(key + " is required");
      }
      return null;
    }
    value = value.trim();
    try {
      return new Long(value);
    } catch (Exception e) {
      throw new OBException("Wrong integer format for" + key);
    }
  }

  static public BigDecimal getBigDecimal(JSONObject json, String key, boolean required)
      throws Exception {
    String value = json.has(key) ? json.getString(key) : null;
    if (value == null || value.trim().isEmpty()) {
      if (required) {
        throw new OBException(key + " is required");
      }
      return null;
    }
    value = value.trim();
    try {
      return new BigDecimal(value);
    } catch (Exception e) {
      throw new OBException("Wrong double format for" + key);
    }
  }

  static public Boolean getBoolean(JSONObject json, String key, boolean required) throws Exception {
    String value = json.has(key) ? json.getString(key) : null;
    if (value == null || value.trim().isEmpty()) {
      if (required) {
        throw new OBException(key + " is required");
      }
      return null;
    }
    value = value.trim();
    if (!(value.equals("true") || value.equals("false"))) {
      throw new OBException("Wrong data type for" + key);
    }
    return value.equals("true");
  }

  static public Date getDate(JSONObject json, String key, boolean required) throws Exception {
    String value = json.has(key) ? json.getString(key) : null;
    if (value == null || value.trim().isEmpty()) {
      if (required) {
        throw new OBException(key + " is required");
      }
      return null;
    }
    try {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      return df.parse(value.trim());
    } catch (Exception e) {
      throw new OBException("Wrong date format for" + key);
    }
  }

  static public String getDateStringFormat(Date date, String format) throws IOException {
    if (date == null) {
      return null;
    } else {
      SimpleDateFormat df = new SimpleDateFormat(format);
      return df.format(date);
    }
  }

  static public String getDateString(Date date) throws IOException {
    if (date == null) {
      return null;
    } else {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      return df.format(date);
    }
  }

  static public String getTimestampString(Date date) throws IOException {
    if (date == null) {
      return null;
    } else {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      return df.format(date);
    }

  }

  static public String xmlToString(SOAPMessage soapMessage) {
    String xml = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      soapMessage.writeTo(out);
      xml = new String(out.toByteArray());
    } catch (Exception e) {
      throw new OBException(e.getMessage());
    }
    return xml;
  }

  static public Boolean existModule(String javaPackage) {

    try {
      OBContext.setAdminMode();

      OBCriteria<Module> moduleCrt = OBDal.getInstance().createCriteria(Module.class);
      moduleCrt.add(Restrictions.eq(Module.PROPERTY_JAVAPACKAGE, javaPackage));

      return moduleCrt.count() > 0;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  static public Boolean hasParam(JSONObject json, String key) throws Exception {
    return json.has(key);
  }

  static public Scactu_Log createLog(JSONObject body, String endpoint, String type)
      throws SQLException {

    try {
      OBContext.setAdminMode();

      Scactu_Log log = OBProvider.getInstance().get(Scactu_Log.class);
      log.setEndpoint(endpoint);
      log.setJsonRequest(body.toString());
      log.setType(type);
      OBDal.getInstance().save(log);
      OBDal.getInstance().flush();
      OBDal.getInstance().getConnection().commit();

      return log;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  static public JSONObject createObject(String id, String name) throws JSONException {
    JSONObject object = new JSONObject();
    object.put("id", StringUtils.isBlank(id) ? "" : id);
    object.put("name", StringUtils.isBlank(name) ? "" : name);
    return object;
  }

}
