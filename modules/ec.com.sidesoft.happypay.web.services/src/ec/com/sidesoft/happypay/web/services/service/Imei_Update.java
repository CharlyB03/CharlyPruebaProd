package ec.com.sidesoft.happypay.web.services.service;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.web.WebService;
import ec.com.sidesoft.happypay.web.services.SHPPWS_AUDIT_IMEI;

public class Imei_Update implements WebService {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(Imei_Update.class);
    private static final List<String> ALLOWED_PADLOCK_TYPES = Arrays.asList("NUOVOPAY", "TRUSTONIC");
    private static final int IMEI_LENGTH = 15;
    private static final int NUOVOPAY_ID_LENGTH = 7;
    private static final int MAX_ERROR_LENGTH = 2000;

    @Override
    public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject result = new JSONObject();
        
        String noCredit = "";
        String imei = "";
        String padlockType = "";
        String deviceId = "";
        String usuario = "";
        String auditId = null;

        OBContext.setAdminMode(true);
        try {
            String body = readBody(request);
            JSONObject json = new JSONObject(body);

            noCredit = json.optString("noCredit", "").trim();
            padlockType = json.optString("padlockType", "").trim().toUpperCase();
            deviceId = json.optString("deviceId", "").trim();
            if ("null".equalsIgnoreCase(deviceId)) deviceId = "";

            imei = json.optString("imei", "").trim();
            usuario = json.optString("usuario", "").trim();

            log.info("IMEI Update Request. Credit: " + noCredit + " | User: " + usuario + " | Type: " + padlockType);
            if (noCredit.isEmpty()) throw new Exception("noCredit es obligatorio");
            if (imei.isEmpty()) throw new Exception("IMEI es obligatorio");
            if (padlockType.isEmpty()) throw new Exception("Tipo de Candado es obligatorio");
            if (usuario.isEmpty()) throw new Exception("Usuario es obligatorio");
            if (imei.length() != IMEI_LENGTH || !imei.matches("\\d+")) {
                throw new Exception("El IMEI debe tener exactamente 15 dígitos numéricos");
            }
            if (!ALLOWED_PADLOCK_TYPES.contains(padlockType)) {
                throw new Exception("Tipo de Candado no permitido. Valores: " + ALLOWED_PADLOCK_TYPES);
            }

            if ("NUOVOPAY".equals(padlockType)) {
                if (deviceId.isEmpty()) {
                    throw new Exception("Para NUOVOPAY, el ID Candado es obligatorio");
                }
                if (deviceId.length() != NUOVOPAY_ID_LENGTH) {
                    throw new Exception("NUOVOPAY requiere ID de exactamente 7 caracteres");
                }
            }

            Invoice invoice = findInvoice(noCredit);
            if (invoice == null) throw new Exception("Crédito no encontrado: " + noCredit);
            BigDecimal outstanding = invoice.getOutstandingAmount();
            if (outstanding == null || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("El crédito no tiene saldo pendiente");
            }

            SHPPWS_AUDIT_IMEI audit = OBProvider.getInstance().get(SHPPWS_AUDIT_IMEI.class);
            audit.setClient(invoice.getClient());
            audit.setOrganization(invoice.getOrganization());
            audit.setInvoice(invoice);
            audit.setProcessDate(new Date());
            audit.setAPIUser(usuario);
            audit.setOLDImei(invoice.getShpicImei()); 
            audit.setOLDPadlockType(invoice.getShppwsPadlock());
            audit.setOLDPadlock(invoice.getShppwsIdPadlock());
            invoice.setShpicImei(imei); 
            invoice.setShppwsPadlock(padlockType);
            invoice.setShppwsIdPadlock(deviceId.isEmpty() ? null : deviceId);

            User userOB = findUser(usuario);
            if (userOB != null) invoice.setUpdatedBy(userOB);
            OBDal.getInstance().save(invoice);
            audit.setNEWImei(imei); 
            audit.setNEWPadlockType(padlockType);
            audit.setNEWPadlock(deviceId.isEmpty() ? null : deviceId);
            audit.setAlertStatus("EXITO");
            audit.setErrorMsg("OK");
            OBDal.getInstance().save(audit);
            OBDal.getInstance().flush();
            OBDal.getInstance().commitAndClose();
            
            auditId = audit.getId();

            result.put("No_Opportunity", noCredit);
            result.put("Message", "Ok");
            result.put("AuditID", auditId);

        } catch (Exception e) {
            OBDal.getInstance().rollbackAndClose();
            log.error("Error Imei_Update", e);

            try {
                OBContext.setAdminMode(true);
                logErrorTransaction(noCredit, usuario, imei, padlockType, deviceId, e.getMessage());
            } catch (Exception exLog) {
                log.error("FATAL: No se pudo guardar log de error", exLog);
            }

            try {
                result.put("No_Opportunity", noCredit);
                result.put("Message", "Error: " + e.getMessage());
            } catch (Exception jsonEx) {}
            
        } finally {
            OBContext.restorePreviousMode();
        }

        writeResponse(response, result);
    }

    private void logErrorTransaction(String noCredit, String usuario, String imei, String padlock, String idPadlock, String errorMsg) {
        try {
            Invoice inv = null;
            if (noCredit != null && !noCredit.isEmpty()) {
                inv = findInvoice(noCredit);
            }

            SHPPWS_AUDIT_IMEI errLog = OBProvider.getInstance().get(SHPPWS_AUDIT_IMEI.class);
            
            if (inv != null) {
                errLog.setClient(inv.getClient());
                errLog.setOrganization(inv.getOrganization());
                errLog.setInvoice(inv);
                errLog.setOLDImei(inv.getShpicImei());
                errLog.setOLDPadlockType(inv.getShppwsPadlock());
                errLog.setOLDPadlock(inv.getShppwsIdPadlock());
            } else {
                errLog.setClient(OBContext.getOBContext().getCurrentClient());
                errLog.setOrganization(OBContext.getOBContext().getCurrentOrganization());
            }

            errLog.setProcessDate(new Date());
            errLog.setAPIUser(usuario);
            
            if (imei != null && !imei.isEmpty()) {
                errLog.setNEWImei(imei);
            }
            errLog.setNEWPadlockType(padlock);
            errLog.setNEWPadlock(idPadlock);

            errLog.setAlertStatus("ERROR");
            errLog.setErrorMsg(limitString(errorMsg, MAX_ERROR_LENGTH));

            OBDal.getInstance().save(errLog);
            OBDal.getInstance().flush();
            OBDal.getInstance().commitAndClose();

        } catch (Exception ex) {
            log.error("Error guardando auditoría de fallo", ex);
        }
    }

    private String readBody(HttpServletRequest req) throws Exception {
        try (BufferedReader r = req.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private Invoice findInvoice(String doc) {
        OBCriteria<Invoice> q = OBDal.getInstance().createCriteria(Invoice.class);
        q.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTNO, doc));
        q.add(Restrictions.eq(Invoice.PROPERTY_CLIENT, OBContext.getOBContext().getCurrentClient()));
        q.setMaxResults(1);
        return (Invoice) q.uniqueResult();
    }

    private User findUser(String name) {
        OBCriteria<User> q = OBDal.getInstance().createCriteria(User.class);
        q.add(Restrictions.eq(User.PROPERTY_USERNAME, name));
        q.add(Restrictions.eq(User.PROPERTY_CLIENT, OBContext.getOBContext().getCurrentClient()));
        q.setMaxResults(1);
        return (User) q.uniqueResult();
    }

    private void writeResponse(HttpServletResponse res, JSONObject obj) throws Exception {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        JSONArray arr = new JSONArray();
        arr.put(obj);
        try (PrintWriter w = res.getWriter()) {
            w.write(arr.toString());
        }
    }

    private String limitString(String str, int max) {
        if (str == null) return "";
        return str.length() > max ? str.substring(0, max) : str;
    }
    
    public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {}
    public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {}
    public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {}
}