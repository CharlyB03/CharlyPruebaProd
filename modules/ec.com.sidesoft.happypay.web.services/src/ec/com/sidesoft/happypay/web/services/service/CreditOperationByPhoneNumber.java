package ec.com.sidesoft.happypay.web.services.service;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.web.WebService;
import org.hibernate.criterion.Restrictions;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.credit.factory.SscfCom1;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.credit.factory.SscfPersonalReference;
import ec.com.sidesoft.happypay.web.services.shppws_config;

public class CreditOperationByPhoneNumber implements WebService {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String Error = "";

        OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
        shppws_config accesApi = (shppws_config) queryApi.uniqueResult();

        log_records logger = new log_records();
        String requestParameters = request.getQueryString();
        Scactu_Log log = logger.log_start_register(accesApi, "Credit_Operation", requestParameters);

        String noPhone = request.getParameter("nophone");

        JSONArray arrayParentOperation = new JSONArray();
        List<Invoice> listCredits = new ArrayList<>();

        try {

            OBCriteria<DocumentType> queryDocumenType = OBDal.getInstance().createCriteria(DocumentType.class);
            queryDocumenType.add(Restrictions.eq(DocumentType.PROPERTY_SHPFRISCREDITOPE, true));
            List<DocumentType> listDocType = queryDocumenType.list();

            String[] docTypeIds = new String[listDocType.size()];
            int index = 0;
            for (DocumentType docType : listDocType) {
                docTypeIds[index] = docType.getId();
                index++;
            }

            OBCriteria<Location> location = OBDal.getInstance().createCriteria(Location.class);
            location.add(Restrictions.eq(Location.PROPERTY_PHONE, noPhone));

            List<Location> listLocation = location.list();
            String[] locIds = new String[listLocation.size()];
            int indx = 0;
            for (Location loc : listLocation) {
                locIds[indx] = loc.getBusinessPartner().getId();
                indx++;
            }

            OBCriteria<Invoice> queryCredit = OBDal.getInstance().createCriteria(Invoice.class);
            queryCredit.add(Restrictions.in(Invoice.PROPERTY_BUSINESSPARTNER + ".id", locIds));
            queryCredit.add(Restrictions.in(Invoice.PROPERTY_TRANSACTIONDOCUMENT + ".id", docTypeIds));

            listCredits = queryCredit.list();

            if (listCredits.size() > 0) {

                for (Invoice objInvoice : listCredits) {

                    JSONObject objParentOperation = new JSONObject();

                    String TypeStatus = objInvoice.getShpicOperationState();
                    if (TypeStatus == null ||
                            !(TypeStatus.equals("02") || TypeStatus.equals("03"))) {
                        continue;
                    }

                    switch (TypeStatus) {
                        case "02": TypeStatus = "VIGENTE"; break;
                        case "03": TypeStatus = "VENCIDO"; break;
                    }

                    JSONArray arrayPersonalReferences = new JSONArray();

                    try {
                        OBCriteria<SscfCreditOperation> queryOp = OBDal.getInstance()
                                .createCriteria(SscfCreditOperation.class);
                        queryOp.add(Restrictions.eq(
                                SscfCreditOperation.PROPERTY_DOCUMENTNO,
                                objInvoice.getDocumentNo()
                        ));

                        SscfCreditOperation creditOp = (SscfCreditOperation) queryOp.uniqueResult();

                        if (creditOp != null) {

                            OBCriteria<SscfCom1> queryCom = OBDal.getInstance()
                                    .createCriteria(SscfCom1.class);
                            queryCom.add(Restrictions.eq(
                                    SscfCom1.PROPERTY_SSCFCREDITOPERATION,
                                    creditOp
                            ));

                            List<SscfCom1> listCom = queryCom.list();

                            for (SscfCom1 com1 : listCom) {

                                OBCriteria<SscfPersonalReference> queryRef = OBDal.getInstance()
                                        .createCriteria(SscfPersonalReference.class);
                                queryRef.add(Restrictions.eq(
                                        SscfPersonalReference.PROPERTY_SSCFCOM1,
                                        com1
                                ));

                                List<SscfPersonalReference> personalRefs = queryRef.list();

                                int cont = 1;
                                for (SscfPersonalReference ref : personalRefs) {
                                    JSONObject objRef = new JSONObject();
                                    objRef.put("noPhoneRef" + cont, ref.getPhone());
                                    arrayPersonalReferences.put(objRef);
                                    cont++;
                                }
                            }
                        }

                    } catch (Exception ex) {
                        JSONObject errRef = new JSONObject();
                        errRef.put("error", ex.getMessage());
                        arrayPersonalReferences.put(errRef);
                    }

                    // *******************************************************
                    // VALIDACIÓN: SOLO ACEPTAR SI UNA REFERENCIA COINCIDE
                    // *******************************************************
                    boolean hasMatchingReference = false;

                    for (int i = 0; i < arrayPersonalReferences.length(); i++) {

                        JSONObject refObj = arrayPersonalReferences.getJSONObject(i);

                        Iterator<String> it = refObj.keys();
                        while (it.hasNext()) {
                            String key = it.next();
                            String value = refObj.getString(key);

                            if (value.equals(noPhone)) {
                                hasMatchingReference = true;
                                break;
                            }
                        }

                        if (hasMatchingReference) break;
                    }

                    if (!hasMatchingReference) {
                        continue;
                    }

                    objParentOperation.put("noPhone", noPhone);
                    objParentOperation.put("noCredit", objInvoice.getDocumentNo());
                    objParentOperation.put("identifier", objInvoice.getBusinessPartner().getTaxID());
                    objParentOperation.put("email", objInvoice.getBusinessPartner().getEEIEmail());

                    BigDecimal grandTotal = objInvoice.getGrandTotalAmount();
                    if (grandTotal == null) grandTotal = BigDecimal.ZERO;

                    objParentOperation.put("creditAmount", grandTotal.setScale(2, RoundingMode.HALF_UP));
                    objParentOperation.put("statusOperation", TypeStatus);

                    objParentOperation.put("references", arrayPersonalReferences);

                    arrayParentOperation.put(objParentOperation);
                }
            }

        } catch (Exception e) {

            JSONObject objParentOperationNull = new JSONObject();
            objParentOperationNull.put("noPhone", noPhone);
            objParentOperationNull.put("noCredit", 0);

            Error = e.getMessage();
            arrayParentOperation.put(objParentOperationNull);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = arrayParentOperation.toString();
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.close();

        String requestUrl = request.getRequestURL().toString();

        try {
            String Interface = "SHPPWS_NT";
            String Process = "Operación Crédito - Telefono";
            logger.log_end_register(log, requestUrl, noPhone, json, "OK", "OUT", Interface, Process, null, Error);
        } catch (Exception e) {
        }
    }

    @Override public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {}
    @Override public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {}
    @Override public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {}
}
