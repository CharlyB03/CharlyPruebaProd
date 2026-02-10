package ec.com.sidesoft.happypay.web.services.service.utils;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.service.db.DalConnectionProvider;

import ec.com.sidesoft.credit.simulator.scsl_Product;
import ec.com.sidesoft.happypay.customizations.shpctBinnacle;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_NewCustomerScore;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_Quotas;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_RiskIndex;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.ws.equifax.SweqxEquifax;
import it.openia.crm.Opcrmopportunities;

public abstract class SHPPWS_Helper_Model {
  private static final Logger log4j = Logger.getLogger(SHPPWS_Helper_Model.class.getName());

  public static boolean validateBlackListcheckCedula() {
    OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
    return accesApi.isCHKLn1Novalidate();
  }

  public static boolean validateBlackListchecktelefono() {
    OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
    return accesApi.isCHKLn2Novalidate();
  }

  public static boolean validateBlackListcheckemail() {
    OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
    return accesApi.isCHKLn3Novalidate();
  }

  public static boolean validateSinergy() {
    OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
    return accesApi.isSynergyIsactivate();
  }

  public static shppee_Quotas getCuotabyEndSeg(String seg, String productCode, String agency) {
    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" AS cpm WHERE ");
    whereClause.append(" cpm." + shppee_Quotas.PROPERTY_ENDSEGMENT + " = TRIM('" + seg + "') ");
    whereClause.append(" AND cpm." + shppee_Quotas.PROPERTY_SCSLPRODUCT + "."
        + scsl_Product.PROPERTY_VALIDATIONCODE + " = TRIM('" + productCode + "') ");
    whereClause
        .append(" AND cpm." + shppee_Quotas.PROPERTY_AGENCYCODE + " = TRIM('" + agency + "') ");
    OBQuery<shppee_Quotas> crt = OBDal.getInstance().createQuery(shppee_Quotas.class,
        whereClause.toString());
    crt.setMaxResult(1);
    return crt.uniqueResult();

  }

  public static String getScoreNewClientDefault(shppws_config accesApi, boolean newClient,
      String codeInclusion, boolean isDefault) {
    OBCriteria<shppee_NewCustomerScore> queryScoreNewClient = OBDal.getInstance()
        .createCriteria(shppee_NewCustomerScore.class);
    if (!isDefault) {
      queryScoreNewClient.add(Restrictions.eq(shppee_NewCustomerScore.PROPERTY_EQUIFAXSEGMENT,
          newClient ? accesApi.getEquifaxProfNewclient() : accesApi.getEquifaxProfOldclient()));
      queryScoreNewClient.add(
          Restrictions.eq(shppee_NewCustomerScore.PROPERTY_SHPPWSSCOREINCLUSION, codeInclusion));
    } else {
      queryScoreNewClient
          .add(Restrictions.eq(shppee_NewCustomerScore.PROPERTY_SHPPWSDEFAULTFIELD, true));
    }
    shppee_NewCustomerScore accesScoreNewClient = (shppee_NewCustomerScore) queryScoreNewClient
        .uniqueResult();
    if (accesScoreNewClient == null) {
      return getScoreNewClientDefault(null, false, null, true);
    }
    return accesScoreNewClient.getENDSegment();
  }

  public static String getDefaultRiskIndiexEqError() {
    String DefaultRiskIndiexEQError = "";
    OBCriteria<shppee_RiskIndex> queryRiskIndexEqError = OBDal.getInstance()
        .createCriteria(shppee_RiskIndex.class);
    queryRiskIndexEqError.add(Restrictions.eq(shppee_RiskIndex.PROPERTY_SHPPWSDEFAULTFIELD, true));
    queryRiskIndexEqError.setMaxResults(1);
    shppee_RiskIndex accesScoreNewClient = (shppee_RiskIndex) queryRiskIndexEqError.uniqueResult();
    return accesScoreNewClient.getSegment();
  }

  public static void CreateRowLNIntDebtError(Map<String, Object> persona,
      shpctBinnacle obcBinnacleError, shppws_config accesApi, Opcrmopportunities objOpportunity) {
    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);
    if (persona.containsKey("LNIntDebtStatus")
        && persona.get("LNIntDebtStatus").equals("ErrorDeudaInterna")) {

      objBinnacleError.setClient(accesApi.getClient());
      objBinnacleError.setOrganization(accesApi.getOrganization());
      objBinnacleError.setActive(accesApi.isActive());
      objBinnacleError.setCreatedBy(accesApi.getCreatedBy());
      objBinnacleError.setUpdatedBy(accesApi.getUpdatedBy());

      // Relación obligatoria (NO puede ir null)
      objBinnacleError.setOpcrmOpportunities(objOpportunity);

      objBinnacleError.setNameMatrix("Deuda Interna");
      objBinnacleError.setResults("R");
      objBinnacleError.setMessages(String.valueOf(persona.get("LNIntDebtMotivo")));
      objBinnacleError.setComments(String.valueOf(persona.get("LNIntDebtAll")));

      OBDal.getInstance().save(objBinnacleError);
      OBDal.getInstance().flush();
    }

  }

  public static void CreateRowLNMailError(Map<String, Object> persona,
      shpctBinnacle obcBinnacleError, shppws_config accesApi, Opcrmopportunities objOpportunity) {

    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);
    if (persona.containsKey("LNMailStatus") && persona.get("LNMailStatus").equals("ErrorMail")) {

      objBinnacleError.setClient(accesApi.getClient());
      objBinnacleError.setOrganization(accesApi.getOrganization());
      objBinnacleError.setActive(accesApi.isActive());
      objBinnacleError.setCreatedBy(accesApi.getCreatedBy());
      objBinnacleError.setUpdatedBy(accesApi.getUpdatedBy());

      // Relación obligatoria (NO puede ir null)
      objBinnacleError.setOpcrmOpportunities(objOpportunity);

      objBinnacleError.setNameMatrix("Motivo de Correos");
      objBinnacleError.setResults("R");
      objBinnacleError.setMessages(String.valueOf(persona.get("LNMailMotivo")));
      objBinnacleError.setComments(String.valueOf(persona.get("LNMailAll")));

      OBDal.getInstance().save(objBinnacleError);
      OBDal.getInstance().flush();
    }

  }

  public static void CreateRowLNPhoneError(Map<String, Object> persona,
      shpctBinnacle obcBinnacleError, shppws_config accesApi, Opcrmopportunities objOpportunity) {
    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);
    if (persona.containsKey("LNPhoneStatus") && persona.get("LNPhoneStatus").equals("ErrorPhone")) {

      objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);

      objBinnacleError.setClient(accesApi.getClient());
      objBinnacleError.setOrganization(accesApi.getOrganization());
      objBinnacleError.setActive(accesApi.isActive());
      objBinnacleError.setCreatedBy(accesApi.getCreatedBy());
      objBinnacleError.setUpdatedBy(accesApi.getUpdatedBy());

      // Relación obligatoria (NO puede ir null)
      objBinnacleError.setOpcrmOpportunities(objOpportunity);

      objBinnacleError.setNameMatrix("Motivo de Teléfonos");
      objBinnacleError.setResults("R");
      objBinnacleError.setMessages(String.valueOf(persona.get("LNPhoneMotivo")));
      objBinnacleError.setComments(String.valueOf(persona.get("LNPhoneAll")));

      OBDal.getInstance().save(objBinnacleError);
      OBDal.getInstance().flush();
    }

  }

  public static void CreateRowSynergyError(Map<String, Object> persona,
      shpctBinnacle obCBinnacleError, shppws_config accesApi, Opcrmopportunities objOpportunity) {

    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);

    objBinnacleError.setClient(accesApi.getClient());
    objBinnacleError.setOrganization(accesApi.getOrganization());
    objBinnacleError.setActive(accesApi.isActive());
    objBinnacleError.setCreatedBy(accesApi.getCreatedBy());
    objBinnacleError.setUpdatedBy(accesApi.getUpdatedBy());

    // Relación obligatoria (NO puede ir null)
    objBinnacleError.setOpcrmOpportunities(objOpportunity);

    objBinnacleError.setNameMatrix("Rechazo por sinergia");
    objBinnacleError.setResults("R");
    objBinnacleError.setMessages("Rechazado en Sinergia");
    objBinnacleError.setComments(String.valueOf(persona.get("RCcedula")));

    OBDal.getInstance().save(objBinnacleError);
    OBDal.getInstance().flush();
  }

  public static void CreateRowLNIdError(Map<String, Object> persona, shpctBinnacle obcBinnacleError,
      shppws_config accesApi, Opcrmopportunities objOpportunity) {
    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);
    if (persona.containsKey("LNCedulaStatus")
        && persona.get("LNCedulaStatus").equals("ErrorCedula")) {

      objBinnacleError.setClient(accesApi.getClient());
      objBinnacleError.setOrganization(accesApi.getOrganization());
      objBinnacleError.setActive(accesApi.isActive());
      objBinnacleError.setCreatedBy(accesApi.getCreatedBy());
      objBinnacleError.setUpdatedBy(accesApi.getUpdatedBy());

      // Relación obligatoria (NO puede ir null)
      objBinnacleError.setOpcrmOpportunities(objOpportunity);

      objBinnacleError.setNameMatrix("Motivo Cedulas");
      objBinnacleError.setResults("R");
      objBinnacleError.setMessages(String.valueOf(persona.get("LNCedulaMotivo")));
      objBinnacleError.setComments(String.valueOf(persona.get("LNCedulaAll")));

      OBDal.getInstance().save(objBinnacleError);
      OBDal.getInstance().flush();
    }

  }

  public static SweqxEquifax lastRequestEquifax(String partnerId) {

    OBCriteria<SweqxEquifax> obc = OBDal.getInstance().createCriteria(SweqxEquifax.class);
    obc.add(Restrictions.eq(SweqxEquifax.PROPERTY_BUSINESSPARTNER + ".id", partnerId));
    obc.addOrderBy(SweqxEquifax.PROPERTY_CREATIONDATE, false);
    obc.setMaxResults(1);
    return (SweqxEquifax) obc.uniqueResult();
  }

  public static shpctBinnacle lastBinnacle(String opportunitiId) {

    OBCriteria<shpctBinnacle> obc = OBDal.getInstance().createCriteria(shpctBinnacle.class);
    obc.add(Restrictions.eq(shpctBinnacle.PROPERTY_OPCRMOPPORTUNITIES + ".id", opportunitiId));
    obc.add(Restrictions.eq(shpctBinnacle.PROPERTY_RESULTS, "R"));
    obc.addOrderBy(shpctBinnacle.PROPERTY_CREATIONDATE, false);
    obc.setMaxResults(1);
    return (shpctBinnacle) obc.uniqueResult();
  }

  public static String generateDocumentno(String documentId) throws NoConnectionAvailableException {

    ConnectionProvider conn = new DalConnectionProvider(false);
    org.openbravo.base.secureApp.VariablesSecureApp vars = RequestContext.get()
        .getVariablesSecureApp();

    return Utility.getDocumentNo(conn.getConnection(), conn, vars, "",
        Opcrmopportunities.ENTITY_NAME, documentId, documentId, false, true);
  }

  public static shppee_NewCustomerScore getCustomerScore(boolean isDefault, String segEquifax,
      String caseSeg, String hqlStr) {

    OBCriteria<shppee_NewCustomerScore> obc = OBDal.getInstance()
        .createCriteria(shppee_NewCustomerScore.class);

    if (isDefault) {
      obc.add(Restrictions.eq(shppee_NewCustomerScore.PROPERTY_SHPPWSDEFAULTFIELD, true));
    } else {
      obc.add(Restrictions.eq(shppee_NewCustomerScore.PROPERTY_EQUIFAXSEGMENT, segEquifax));
      // Case 1: Age
      // Case 2: Inclusion
      switch (caseSeg) {
      case "1":
        BigDecimal val = new BigDecimal(hqlStr);
        obc.add(Restrictions.le(shppee_NewCustomerScore.PROPERTY_SCOREINCLUSIONFROM, val));
        obc.add(Restrictions.ge(shppee_NewCustomerScore.PROPERTY_SCOREINCLUSIONUNTIL, val));
        break;
      case "2":
        obc.add(Restrictions.eq(shppee_NewCustomerScore.PROPERTY_SHPPWSSCOREINCLUSION, hqlStr));
        break;
      default:
        break;
      }
    }
    obc.setMaxResults(1);

    shppee_NewCustomerScore result = (shppee_NewCustomerScore) obc.uniqueResult();

    if (result == null) {
      return getCustomerScore(true, null, null, null);
    }

    return result;
  }

  public static void putSectionRejected(Map<String, Object> persona, String msgLN, String idLN,
      String matriz) {
    log4j.info(String.format("msgLN: %s,  idLN: %s, matriz: %s", msgLN, idLN, matriz));
    String msg = (String) ObjectUtils.defaultIfNull(msgLN, "");
    String idln = (String) ObjectUtils.defaultIfNull(idLN, "");
    if (persona.containsKey("haserror") && persona.get("haserror").equals("NA")) {
      persona.put("msgLN", msg);
      persona.put("idLN", idln);
      persona.put("matriz", matriz);
      persona.put("matrizReason", matriz);
    }

  }

}
