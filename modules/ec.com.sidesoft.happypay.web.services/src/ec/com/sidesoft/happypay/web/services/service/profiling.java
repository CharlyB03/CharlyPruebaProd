package ec.com.sidesoft.happypay.web.services.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.web.WebService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.credit.factory.maintenance.CivilStatus;
import ec.com.sidesoft.credit.simulator.scsl_Product;
import ec.com.sidesoft.customer.exception.Ecsce_CustomerException;
import ec.com.sidesoft.happypay.customizations.shpctBinnacle;
import ec.com.sidesoft.happypay.pev.shppev_age;
import ec.com.sidesoft.happypay.pev.shppev_emailReason;
import ec.com.sidesoft.happypay.pev.shppev_internalDebt;
import ec.com.sidesoft.happypay.pev.shppev_lifestyleReason;
import ec.com.sidesoft.happypay.pev.shppev_phoneReason;
import ec.com.sidesoft.happypay.pev.shppev_rIdentification;
import ec.com.sidesoft.happypay.pev.shppev_reasonProfession;
import ec.com.sidesoft.happypay.pev.credit.InstexceptionEqfx;
import ec.com.sidesoft.happypay.pev.credit.Shppec_CredCurr;
import ec.com.sidesoft.happypay.pev.credit.Shppec_CredExp;
import ec.com.sidesoft.happypay.pev.credit.Shppec_CredPen;
import ec.com.sidesoft.happypay.pev.credit.Shppec_ExpActual;
import ec.com.sidesoft.happypay.pev.credit.Shppec_Lawsuit;
import ec.com.sidesoft.happypay.pev.credit.Shppec_ParallelC;
import ec.com.sidesoft.happypay.pev.credit.shppec_portpen;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_NewCustomerScore;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_Quotas;
import ec.com.sidesoft.happypay.pev.evaluation.shppee_RiskIndex;
import ec.com.sidesoft.happypay.pev.reference.ShpperReferenceMatrix;
import ec.com.sidesoft.happypay.web.services.ShppwsRcLog;
import ec.com.sidesoft.happypay.web.services.shppwsPartnertype;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.shppws_detailDue;
import ec.com.sidesoft.happypay.web.services.monitor.BlacklistsEntry;
import ec.com.sidesoft.happypay.web.services.monitor.MonitorManager;
import ec.com.sidesoft.happypay.web.services.service.utils.SHPPWS_Helper;
import ec.com.sidesoft.happypay.web.services.service.utils.SHPPWS_Helper_Model;
import ec.com.sidesoft.ws.equifax.SweqxEquifax;
import it.openia.crm.Opcrmopportunities;

public class profiling implements WebService {
  String typeClientSynergy = "";
  boolean statusEquifax = true;

  // Variables para token equifax
  private String cachedToken = null;
  private long tokenExpiryTime = 0;
  private static final Logger log4j = Logger.getLogger(profiling.class.getName());

  private static final long serialVersionUID = 1L;

  public static Date truncateDate(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    String Error = "";
    boolean statusSinergia = true;
    boolean completeFlow = true;
    String log2 = "";
    String docOpp = "";
    //////////////////////////////////// AQUI EQUIFAX ESTADO
    JSONObject jsonMonitor = new JSONObject();
    jsonMonitor.put("SHPPWS_SideSoft_Profiling", "Service" + 0);
    jsonMonitor.put("startSHPPWS_SideSoft_Profiling", LocalDateTime.now());
    jsonMonitor.put("typeSHPPWS_SideSoft_Profiling", "Perfilamiento");

    OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
    shppws_config accesApi = (shppws_config) queryApi.uniqueResult();
    log_records logger = new log_records();
    String requestParameters = request.getQueryString();
    Scactu_Log log = logger.log_start_register(accesApi, "profiling", requestParameters);

    // input variables
    String Interface = request.getParameter("Interface");
    String Chanel = request.getParameter("Chanel");
    String Code_Commerce = request.getParameter("Code_Commerce");
    String Code_Agency = request.getParameter("Code_Agency");
    String Store_group = request.getParameter("Store_group");
    String Code_Product = request.getParameter("Code_Product");
    String ID = request.getParameter("ID");
    String CellPhone = request.getParameter("CellPhone");
    String email = request.getParameter("email");
    String Amount = request.getParameter("Amount");
    String Entrance = request.getParameter("Entrance");
    // String User = request.getParameter("User");
    String City_store_group = request.getParameter("City_store_group");
    String Province_store_group = request.getParameter("Province_store_group");
    String whiteList = request.getParameter("White_List");

    // Generate Documentno
    docOpp = SHPPWS_Helper_Model.generateDocumentno(accesApi.getOPDocumentType().getId());

    // Get Matriz Cuota by White List
    shppee_Quotas cot = SHPPWS_Helper_Model.getCuotabyEndSeg(whiteList, Code_Product, Store_group);

    // Validacion para aplicar flujo completo o flujo Directo (Lista blanca)
    // true flujo completo
    // false flujo directo a datos de la matriz de cuotas
    completeFlow = (cot == null);

    Map<String, Object> persona = new HashMap<>();

    // Generate Documentno
    persona.put("docnoTransaction", docOpp);
    persona.put("identifierLog", docOpp);

    ///////// Filters Variables//////////
    boolean CustomerExeption_result = true;
    Date fechaActual = new Date();
    String filter = "";

    String validatorRC = "";
    String filterRC = "-"; // Registro Civil
    String RCnames = "";
    String RClastName1 = "";
    String RClastName2 = "";
    String RCname1 = "";
    String RCname2 = "";
    String RCgender = "";
    String RCbirthdate = "";
    String RCprofession = "";
    String RCcivilStatus = "";
    String RCnationality = "";
    String RCnationalityAux = "";
    String filterEQ = ""; // Equifax
    Double CV = Double.valueOf(0);
    Double CC = Double.valueOf(0);
    Double DJ = Double.valueOf(0);
    String segment1 = "";
    String segment2 = "";
    String segment3 = "";
    String newClient_segment = "";
    String newClient_scoreInclusion = "";
    Double Quota_Available = Double.valueOf(0);

    //////////////////////////////////
    ////////// PRIMER FILTRO RC////////
    ////////////////////////////////// matrizStr);
    persona.put("CellPhone", CellPhone);
    persona.put("email", email);
    persona.put("interface", Interface);
    persona.put("channel", Chanel);
    persona.put("commercialcode", Code_Commerce);
    persona.put("agencycode", Code_Agency);
    persona.put("shopgroup", Store_group);
    persona.put("productcode", Code_Product);
    persona.put("Amount", Amount);
    persona.put("Identifier", ID);
    persona.put("City_store_group", City_store_group);
    persona.put("Province_store_group", Province_store_group);
    persona.put("haserror", "NA");

    // Fecha actual
    Date now = truncateDate(new Date());
    OBCriteria<Ecsce_CustomerException> customerexception = OBDal.getInstance()
        .createCriteria(Ecsce_CustomerException.class);
    customerexception
        .add(Restrictions.and(
            Restrictions.and(
                Restrictions.eq(Ecsce_CustomerException.PROPERTY_TAXID, persona.get("Identifier")),
                Restrictions.eq(Ecsce_CustomerException.PROPERTY_STOREGROUP,
                    persona.get("shopgroup"))),
            Restrictions.and(Restrictions.le(Ecsce_CustomerException.PROPERTY_STARTINGDATE, now),
                Restrictions.ge(Ecsce_CustomerException.PROPERTY_DATEUNTIL, now)) // dateUntil >=
                                                                                  // hoy
        ));
    customerexception.addOrder(Order.desc(Ecsce_CustomerException.PROPERTY_CREATIONDATE)); // más
                                                                                           // reciente
    customerexception.setMaxResults(1); // solo uno

    Ecsce_CustomerException accesApi_exception = (Ecsce_CustomerException) customerexception
        .uniqueResult();

    boolean CheckSinergia = SHPPWS_Helper_Model.validateSinergy();

    OBCriteria<shppev_age> query = OBDal.getInstance().createCriteria(shppev_age.class);
    List<shppev_age> reasonAges = query.list();

    if (!reasonAges.isEmpty()) {
      try {
        // String apiResponse = getApiResponse(accesApi, ID, 1, jsonMonitor, (String)
        // persona.get("identifierLog"));
        // Verifica la base de datos antes de llamar a la API
        String apiResponse = getRCDataWithCache(accesApi, ID, jsonMonitor,
            (String) persona.get("identifierLog"));
        procesarApiRC(apiResponse, reasonAges, Store_group, persona); // Matriz Edad
        RCnames = (String) persona.get("RCnames");
        RClastName1 = (String) persona.get("RClastName1");
        RClastName2 = (String) persona.get("RClastName2");
        RCname1 = (String) persona.get("RCname1");
        RCname2 = (String) persona.get("RCname2");
        RCgender = (String) persona.get("RCgender");
        RCbirthdate = (String) persona.get("RCbirthdate");
        RCprofession = (String) persona.get("RCprofession");
        RCcivilStatus = (String) persona.get("RCcivilStatus");
        RCnationality = (String) persona.get("RCnationality");
        RCnationalityAux = (String) persona.get("RCnationalityAux");
        filterRC = (String) persona.get("filterRC"); // resultado segmento Matriz Edad
        filter = filterRC;
        validatorRC = (String) persona.get("validatorRC") != null
            ? (String) persona.get("validatorRC")
            : "";
        Error = (String) persona.get("msgLN") != null ? (String) persona.get("msgLN") : "";
      } catch (Exception e) {
        filter = "R";

        SHPPWS_Helper_Model.putSectionRejected(persona, "Servicio de Recover fuera de línea", ID,
            "Recover");
        persona.put("haserror", "Recover");
        Error = e.getMessage();
      }
    }

    BusinessPartner partner = null;
    if (!validatorRC.equals("R")) {

      try {
        if (customerexception.list().size() > 0) {
          if (!accesApi_exception.getStartingDate().equals(null)
              && !accesApi_exception.getDateuntil().equals(null)) {
            Date fechaInicio = accesApi_exception.getStartingDate();
            Date fechaFin = accesApi_exception.getDateuntil();
            Days dinicio = Days.daysBetween(new DateTime(fechaInicio.getTime()),
                new DateTime(fechaActual.getTime()));
            Days dfin = Days.daysBetween(new DateTime(fechaFin.getTime()),
                new DateTime(fechaActual.getTime()));
            int daysInicio = dinicio.getDays();
            int daysFin = dfin.getDays();
            if (customerexception.list().size() > 0 && accesApi_exception.isActive()
                && daysInicio >= 0 && daysFin <= 0) {

              CustomerExeption_result = false;
            } else {
              CustomerExeption_result = true;
            }
          }
        } else {
          CustomerExeption_result = true;
        }
        partner = validatePartner(accesApi, ID, persona);
      } catch (Exception e) {
        filter = "R";

        SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID, "Recover");
        persona.put("haserror", "Recover");
        Error = e.getMessage();
      }
    }

    String validateCustomer_old_new_Synergy = "";
    String response_DI = "";
    Boolean clientnew = null;

    // Validacion Cliente Nuevo/Antiguo
    Map<String, Object> responseClienteNuevoAntiguo = codigoClienteNuevoAntiguo(CheckSinergia,
        statusSinergia, response_DI, validateCustomer_old_new_Synergy, accesApi, ID, 8, jsonMonitor,
        filter, persona, partner, CustomerExeption_result, Error);

    clientnew = (Boolean) responseClienteNuevoAntiguo.get("clientnew");
    response_DI = (String) responseClienteNuevoAntiguo.get("response_DI");
    validateCustomer_old_new_Synergy = (String) responseClienteNuevoAntiguo
        .get("validateCustomer_old_new_Synergy");
    Error = (String) responseClienteNuevoAntiguo.get("Error");
    statusSinergia = (Boolean) responseClienteNuevoAntiguo.get("statusSinergia");

    if (clientnew && partner != null && !customerNewOld(persona, partner)) {
      clientnew = false;
    }

    if ((!completeFlow && !clientnew)) {
      completeFlow = true;
    }

    if (completeFlow) {
      // Blacklist - Entry
      BlacklistsEntry BlackList = new BlacklistsEntry();

      try {
        BlackList.BlacklistEntryApi(accesApi, ID != null ? ID : "",
            CellPhone != null ? CellPhone : "", (String) persona.get("identifierLog"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    //////////////////////////////////
    ////////// FILTRO Equifax//////////
    //////////////////////////////////
    if (filter.equals("R")) {
      persona.put("Exception_Institucion", "false");
    }

    if (completeFlow) {

      filter = codigoListasNegras(validateCustomer_old_new_Synergy, Error, accesApi, ID, CellPhone,
          email, persona, jsonMonitor, CustomerExeption_result, CheckSinergia, statusSinergia,
          filter);

      if (filter.equals("C") && CustomerExeption_result) {
        String validateCustomer = null;
        // statusSinergia=false;

        // proceso de cliente nuevo
        String filterNewClient = null;

        // verifica que el check de sinergia este activo y el estado de sinergia sea diferente de R,
        // si da R se pasara a false
        if (CheckSinergia) {
          if (statusSinergia) {
            validateCustomer = validateStatusPartner(response_DI);
            // -------------------------------------------------------------------------------
            if (validateCustomer_old_new_Synergy.equals("CLIENTE APROBADO")) {
              Error = procesarApiEQ(partner, accesApi, ID, persona, jsonMonitor); // Aqui obtengo
                                                                                  // datos de
                                                                                  // Equifax
                                                                                  // y guardo en
                                                                                  // persona
              if (persona.get("Exception_Institucion").equals("true")) {
                filter = (String) persona.get("Segment");
              } else {
                filter = "SP";
              }
            } else {
              Error = procesarApiEQ(partner, accesApi, ID, persona, jsonMonitor); // Aqui obtengo
                                                                                  // datos de
                                                                                  // Equifax
                                                                                  // y guardo en
                                                                                  // persona
              String serviceValidate = (String) persona.get("ApiResponse");
              if (Error != null && !(Error.equals(""))) {
                filter = clientnew ? accesApi.getEquifaxProfNewclient()
                    : accesApi.getEquifaxProfOldclient();
                persona.put("EQ_segmentacion", filter);
                statusSinergia = false;
                SHPPWS_Helper_Model.putSectionRejected(persona, Error, ID, "Servicio Equifax");
                if (persona.containsKey("haserror") && persona.get("haserror").equals("NA")) {
                  persona.put("haserror", "Servicio Equifax");
                }
                inclusionbyDefault(persona, filter, filter);
                // validateEquifaxScoreNewClient(persona, accesApi, clientnew);
                // filter = (String) persona.get("EQ_scoreClient");
              }
              filterNewClient = validateNewClient(persona, clientnew, statusEquifax, ID,
                  Store_group, partner, accesApi);
              filter = filterNewClient;

            }
          } else {
            Error = procesarApiEQ(partner, accesApi, ID, persona, jsonMonitor); // Aqui obtengo
                                                                                // datos
                                                                                // de Equifax y
                                                                                // guardo
                                                                                // en persona
            String serviceValidate = (String) persona.get("ApiResponse");

            if (Error != null && !(Error.equals(""))) {
              filter = clientnew ? accesApi.getEquifaxProfNewclient()
                  : accesApi.getEquifaxProfOldclient();
              persona.put("EQ_segmentacion", filter);

              SHPPWS_Helper_Model.putSectionRejected(persona, Error, ID, "Servicio Equifax");
              if (persona.containsKey("haserror") && persona.get("haserror").equals("NA")) {
                persona.put("haserror", "Servicio Equifax");
              }
              inclusionbyDefault(persona, filter, filter);

            }
            filterNewClient = validateNewClient(persona, clientnew, statusEquifax, ID, Store_group,
                partner, accesApi);
            filter = filterNewClient;

            // proceso normal cuando el check de sinergia no sea activo
          }
        } else {
          Error = procesarApiEQ(partner, accesApi, ID, persona, jsonMonitor); // Aqui obtengo datos
                                                                              // de
                                                                              // Equifax y guardo en
                                                                              // persona
          String serviceValidate = (String) persona.get("ApiResponse");

          if (Error != null && !(Error.equals(""))) {
            filter = clientnew ? accesApi.getEquifaxProfNewclient()
                : accesApi.getEquifaxProfOldclient();
            persona.put("EQ_segmentacion", filter);

            SHPPWS_Helper_Model.putSectionRejected(persona, Error, ID, "Servicio Equifax");
            if (persona.containsKey("haserror") && persona.get("haserror").equals("NA")) {
              persona.put("haserror", "Servicio Equifax");
            }
            inclusionbyDefault(persona, filter, filter);

          }
          filterNewClient = validateNewClient(persona, clientnew, statusEquifax, ID, Store_group,
              partner, accesApi);
          filter = filterNewClient;

        }

      } else {
        if (!persona.get("msgLN").equals("Documento no encontrado en el Registro Civil")
            && !CustomerExeption_result) {
          validateReferencesException(filter, persona, now);
          filter = (String) persona.get("SegmentException");

          persona.put("Mensaje_Operacional", "");
          persona.put("Exception_Institucion", "false");
        } else {
          Error = "No se pudo completar el perfilamiento. No existe información válida en el Registro Civil para continuar con la evaluación del cliente. Por favor, solicite una revisión de los datos.";
        }

      }
    }
    //////////////////////////////// EQnewSearch
    /// Matriz CUPO y Oportunidad /////Finalmente se crea la oportunidad contrastando en la Matriz
    //////////////////////////////// cupos, solo si no es rechazado
    ////////////////////////////////

    // verifica que el check de sinergia este activo y el estado de sinergia sea diferente de R, si
    // da R se pasara a false
    log4j.info(filter);
    if (CheckSinergia) {
      if (statusSinergia) {

        String validateCustomer_old_new = validateStatusPartner(response_DI);
        typeClientSynergy = classificationClient(response_DI);
        if (CustomerExeption_result && validateCustomer_old_new.equals("CLIENTE APROBADO")
            && persona.containsKey("Exception_Institucion")
            && (persona.get("Exception_Institucion").equals("False")
                || persona.get("Exception_Institucion").equals("false"))) {

          filter = SearchClieSegm(response_DI);
          log4j.info("SearchClieSegm: " + filter);

        }
        validateEquifaxQuotas(filter, persona, completeFlow, whiteList);
        filter = EQnewSearch_filter(persona, clientnew);

        Quota_Available = (Double) persona.get("EQ_quota");
        String validateRecover = (String) persona.get("matrizReason");
        if (validateRecover.equals("Matriz Crédito Vigente Vencido")) {
          validatorRC = "R";
          statusSinergia = false;
          persona.put("message", "Filtro Equifax, Cliente rechazado.");
        }
        if (!(validateRecover.equals("Recover")) && !validatorRC.equals("R")

            && CustomerExeption_result) {

          Error = newOpportunity(filter, persona, accesApi, statusEquifax, CheckSinergia,
              statusSinergia);//////////////////////////////////// AQUI EQUIFAX ESTADO
        } else if (!(validateRecover.equals("Recover")) && validatorRC.equals("R")

            && CustomerExeption_result) {

          Error = newOpportunity(filter, persona, accesApi, statusEquifax, CheckSinergia,
              statusSinergia);//////////////////////////////////// AQUI EQUIFAX ESTADO
        }
        if (!(validateRecover.equals("Recover")) && !validatorRC.equals("R")

            && !CustomerExeption_result) {
          Error = newOpportunity_Exception(filter, persona, accesApi_exception, accesApi,
              statusEquifax, CheckSinergia, statusSinergia);//////////////////////////////////// AQUI
                                                            //////////////////////////////////// EQUIFAX
                                                            //////////////////////////////////// ESTADO
        }
      } else {
        validateEquifaxQuotas(filter, persona, completeFlow, whiteList);

        filter = EQnewSearch_filter(persona, clientnew);

        Quota_Available = (Double) persona.get("EQ_quota");
        String validateRecover = (String) persona.get("matrizReason");
        if (!(validateRecover.equals("Recover")) && !validatorRC.equals("R")

            && CustomerExeption_result) {
          Error = newOpportunity(filter, persona, accesApi, statusEquifax, CheckSinergia,
              statusSinergia);//////////////////////////////////// AQUI EQUIFAX ESTADO
        }
        if (!(validateRecover.equals("Recover")) && !validatorRC.equals("R")

            && !CustomerExeption_result) {
          Error = newOpportunity_Exception(filter, persona, accesApi_exception, accesApi,
              statusEquifax, CheckSinergia, statusSinergia);//////////////////////////////////// AQUI
                                                            //////////////////////////////////// EQUIFAX
                                                            //////////////////////////////////// ESTADO
        }
        // proceso normal cuando el check de sinergia no sea activo
      }
    } else if (!CheckSinergia) {
      validateEquifaxQuotas(filter, persona, completeFlow, whiteList);

      filter = EQnewSearch_filter(persona, clientnew);

      Quota_Available = (Double) persona.get("EQ_quota");
      String validateRecover = (String) persona.get("matrizReason");
      if (!(validateRecover.equals("Recover")) && !validatorRC.equals("R")
          && CustomerExeption_result) {
        Error = newOpportunity(filter, persona, accesApi, statusEquifax, CheckSinergia,
            statusSinergia);//////////////////////////////////// AQUI EQUIFAX ESTADO
      }
      if (!(validateRecover.equals("Recover")) && !validatorRC.equals("R")
          && !CustomerExeption_result) {
        Error = newOpportunity_Exception(filter, persona, accesApi_exception, accesApi,
            statusEquifax, CheckSinergia, statusSinergia);
      }
    }

    // |||||||||||||||||||||||||||||||||||//
    // |||||||||||||RESULTADO EXCEPCION DE CLIENTES|||||||||||||//
    // |||||||||||||||||||||||||||||||||||//

    Quota_Available = updateAvailableQuota(filter, (String) persona.get("OP_documentno"),
        Quota_Available);
    String message = Error;// (String) persona.get("message");
    String matriz = (String) persona.get("matriz");
    String oppId = persona.containsKey("OP_record_id") ? (String) persona.get("OP_record_id") : "";
    shpctBinnacle obcOpp = SHPPWS_Helper_Model.lastBinnacle(oppId);

    JSONObject respuesta = new JSONObject();
    String Ref1 = "";
    String Ref2 = "";
    String respuesta_segment = "";
    Double respuesta_Quota_Available = 0.0;
    Double respuesta_Entrance = 0.0;
    Double respuesta_Deadline = 0.0;
    String respuesta_Type_Ref1 = "";
    String respuesta_Type_Ref2 = "";
    Double respuesta_CV = 0.0;
    Double respuesta_CC = 0.0;
    Double respuesta_DJ = 0.0;
    String respuesta_segment1 = "";
    String respuesta_segment2 = "";
    String respuesta_segment3 = "";
    String respuesta_EQ_segmentacion = "";
    Double respuesta_EQ_score_inclusion = 0.0;
    String respuesta_message = "";
    String respuesta_matriz = "";
    String respuesta_type_Entrance = "";

    if (CustomerExeption_result) {
      validateReferences(filter, persona);
      Ref1 = (String) persona.get("Ref1");
      Ref2 = (String) persona.get("Ref2");
      respuesta_segment = filter;
      respuesta_Quota_Available = Quota_Available;
      respuesta_Entrance = (Double) persona.get("EQ_entrance");
      respuesta_Deadline = (Double) persona.get("EQ_deadline");
      respuesta_Type_Ref1 = Ref1;
      respuesta_Type_Ref2 = Ref2;
      respuesta_CV = CV;
      respuesta_CC = CC;
      respuesta_DJ = DJ;
      respuesta_segment1 = segment1;
      respuesta_segment2 = segment2;
      respuesta_segment3 = segment3;
      respuesta_EQ_segmentacion = (String) persona.get("EQ_segmentacion");
      respuesta_EQ_score_inclusion = (Double) persona.get("EQ_score_inclusion");
      respuesta_message = obcOpp == null ? message : obcOpp.getMessages();
      respuesta_matriz = obcOpp == null ? matriz : obcOpp.getNameMatrix();
      respuesta_type_Entrance = (String) persona.get("Type_Entrance");

    } else {
      validateReferencesException(filter, persona, now);
      Ref1 = (String) persona.get("Ref1");
      Ref2 = (String) persona.get("Ref2");
      respuesta_segment = accesApi_exception.getFinalsegment();
      respuesta_Quota_Available = accesApi_exception.getQuota() != null
          ? accesApi_exception.getQuota().doubleValue()
          : 0.0;
      respuesta_Entrance = accesApi_exception.getEntry() != null
          ? accesApi_exception.getEntry().doubleValue()
          : 0.0;
      respuesta_Deadline = accesApi_exception.getMaxterm() != null
          ? accesApi_exception.getMaxterm().doubleValue()
          : 0.0;
      respuesta_Type_Ref1 = Ref1;
      respuesta_Type_Ref2 = Ref2;
      respuesta_message = accesApi_exception.getMessage();
      respuesta_matriz = "Matriz excepcion de clientes";
      respuesta_type_Entrance = accesApi_exception.getTypeInput();
    }

    // |||||||||||||||||||||||||||||||||||//
    // |||||||||||||RESULTADO|||||||||||||//
    // |||||||||||||||||||||||||||||||||||//

    respuesta.put("ID", ID);
    respuesta.put("Segment", respuesta_segment);
    respuesta.put("Quota_Available", respuesta_Quota_Available);
    respuesta.put("Names", RCname1 + " " + RCname2);
    respuesta.put("Surnames", RClastName1 + " " + RClastName2);
    respuesta.put("Birthday", RCbirthdate);
    respuesta.put("Gender", RCgender);
    respuesta.put("Nacionality", RCnationalityAux);
    respuesta.put("Civil_Status", RCcivilStatus);
    respuesta.put("Profession", RCprofession);
    respuesta.put("Type_Entrance", respuesta_type_Entrance);
    respuesta.put("%Entrance", respuesta_Entrance);
    respuesta.put("Deadline", respuesta_Deadline);
    respuesta.put("Type_Ref1", respuesta_Type_Ref1);
    respuesta.put("Type_Ref2", respuesta_Type_Ref2);
    respuesta.put("No_Opportunity", (String) persona.get("OP_documentno"));
    respuesta.put("CV", respuesta_CV);
    respuesta.put("CC", respuesta_CC);
    respuesta.put("DJ", respuesta_DJ);
    respuesta.put("segment1", respuesta_segment1);
    respuesta.put("segment2", respuesta_segment2);
    respuesta.put("segment3", respuesta_segment3);

    respuesta.put("EQ_segmentacion", respuesta_EQ_segmentacion);
    respuesta.put("EQ_score_inclusion", respuesta_EQ_score_inclusion);

    respuesta.put("message", respuesta_message);
    respuesta.put("matriz", respuesta_matriz);

    if (!persona.containsKey("Mensaje_Operacional")) {
      respuesta.put("status_equifax", "OK");
    } else if (!persona.get("Mensaje_Operacional").equals("")) {
      respuesta.put("status_equifax", "Error");
    }
    if (persona.get("CheckDelivery").equals("true")) {
      respuesta.put("Venta_a_Domicilio", "Y");
    } else {
      respuesta.put("Venta_a_Domicilio", "N");
    }
    // respuesta.put("Aux", Aux);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    JSONArray jsonArray = new JSONArray();
    jsonArray.put(respuesta);
    String json = jsonArray.getJSONObject(0).toString();
    PrintWriter writer = response.getWriter();
    writer.write(json);
    writer.close();
    jsonMonitor.put("endSHPPWS_SideSoft_Profiling", LocalDateTime.now());

    String requestUrl = request.getRequestURL().toString();
    // OBContext baseURL = OBContext.getOBContext();
    String InterfaceLog = "SHPPWS_NT";
    String Process = "Perfilamiento";

    String noReference = (String) persona.get("OP_documentno");
    String idRegister = (String) persona.get("OP_record_id");
    if (noReference != null && idRegister != null) {
      logger.log_end_register(log, requestUrl, noReference, json, "OK", "OUT", InterfaceLog,
          Process, idRegister, Error);

    } else {
      noReference = "";
      idRegister = "";
      logger.log_end_register(log, requestUrl, noReference, json, "ERROR", "OUT", InterfaceLog,
          Process, idRegister, Error);
    }

    String OP_documentno = (String) persona.get("OP_documentno");
    if (OP_documentno != null && !OP_documentno.equals("")) {
      jsonMonitor.put("statusSHPPWS_SideSoft_Profiling", "200");
    } else {
      jsonMonitor.put("endSHPPWS_SideSoft_Profiling", "500");
    }
    jsonMonitor.put("Identifier", OP_documentno);
    jsonMonitor.put("Identifier2", ID);
    if (jsonMonitor != null) {
      MonitorManager newMonitor = new MonitorManager();
      newMonitor.sendMonitorData(jsonMonitor, accesApi, true, null);
    }

  }

  // Metodo para validar existencia y vigencia de datos antes de consultar la API
  private String getRCDataWithCache(shppws_config config, String taxId, JSONObject jsonMonitor,
      String identifierLog) throws Exception {

    long rcDaysValidity = 30;
    if (config.getDaysValidity() != null) {
      rcDaysValidity = config.getDaysValidity().longValue();
    }

    StringBuilder hql = new StringBuilder();
    hql.append(" as e where e.businessPartner.taxID = :taxId ");
    hql.append(" and e.alertStatus = 'OK' ");
    hql.append(" order by e.creationDate desc ");

    OBQuery<ShppwsRcLog> query = OBDal.getInstance().createQuery(ShppwsRcLog.class, hql.toString());
    query.setNamedParameter("taxId", taxId);
    query.setMaxResult(1);

    ShppwsRcLog latestLog = query.uniqueResult();

    if (latestLog != null) {
      Date creationDate = latestLog.getCreationDate();
      Date currentDate = new Date();

      long diffInMillies = Math.abs(currentDate.getTime() - creationDate.getTime());
      long elapsedDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

      if (elapsedDays <= rcDaysValidity) {
        log4j.info("Acierto de RC: Usando datos guardados para " + taxId + ". Antigüedad: "
            + elapsedDays + " días.");
        return latestLog.getResponseData();
      }
    }

    log4j.info("Los Datos del Registro Civil fallo o expiro: Solicitando API para " + taxId);
    String apiResponse = getApiResponse(config, taxId, 1, jsonMonitor, identifierLog);

    if (apiResponse != null && !apiResponse.isEmpty() && !apiResponse.contains("Error")) {
      saveRCLog(config, taxId, apiResponse);
    }

    return apiResponse;
  }

  // Almacena el log del Registro Civil y los guarda en la solapa de RC
  private void saveRCLog(shppws_config config, String taxId, String response) {
    try {
      OBCriteria<BusinessPartner> bpCrit = OBDal.getInstance()
          .createCriteria(BusinessPartner.class);
      bpCrit.add(Restrictions.eq(BusinessPartner.PROPERTY_TAXID, taxId));
      BusinessPartner partner = (BusinessPartner) bpCrit.uniqueResult();

      if (partner != null) {
        ShppwsRcLog newLog = OBProvider.getInstance().get(ShppwsRcLog.class);
        newLog.setOrganization(config.getOrganization());
        newLog.setBusinessPartner(partner);
        JSONObject jsonResponse = new JSONObject(response);
        newLog.setResponseData(jsonResponse.toString(4));

        if (jsonResponse.has("persona")) {
          JSONObject datos = jsonResponse.getJSONObject("persona").getJSONObject("datos");

          newLog.setShppwsCedula(datos.optString("cedula"));
          newLog.setShppwsNombresCompletos(datos.optString("nombres"));
          newLog.setShppwsApellidoPaterno(datos.optString("apellidopaterno"));
          newLog.setShppwsApellidoMaterno(datos.optString("apellidomaterno"));
          newLog.setShppwsPrimerNombre(datos.optString("nombreprimero"));
          newLog.setShppwsSegundoNombre(datos.optString("nombresegundo"));
          newLog.setShppwsGenero(datos.optString("cod_sexo"));
          newLog.setShppwsFechaNacimiento(datos.optString("fecha_nacimiento"));
          newLog.setShppwsProfesion(datos.optString("cod_profesion"));
          newLog.setShppwsEstadoCivil(datos.optString("cod_estado_civil"));
          newLog.setShppwsNacionalidad(datos.optString("nacionalidad"));
          newLog.setShppwsFechaFallecimiento(datos.optString("fecha_fallecimiento"));
        }

        newLog.setOrigen("Perfilamiento");
        newLog.setAlertStatus("OK");
        newLog.setResult("Datos procesados correctamente");

        OBDal.getInstance().save(newLog);
        OBDal.getInstance().flush();
      }
    } catch (Exception e) {
      log4j.error("Error al guardar detalles desglosados del RC: " + e.getMessage());
    }
  }

  public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    // TODO Auto-generated method stub

  }

  public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    // TODO Auto-generated method stub

  }

  public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    // TODO Auto-generated method stub
  }

  /*
   ** Se actualiza la cuota disponible para el usuario
   */
  public Double updateAvailableQuota(String filter, String numOportunity, Double Quota_Available) {
    if (numOportunity != null && !(numOportunity.equals(""))) {
      OBCriteria<Opcrmopportunities> queryOp = OBDal.getInstance()
          .createCriteria(Opcrmopportunities.class);
      queryOp.add(Restrictions.eq(Opcrmopportunities.PROPERTY_SHPPWSOPDOCUMENTNO, numOportunity));
      List<Opcrmopportunities> listOp = queryOp.list();
      if (listOp.size() > 0 && !(filter.equals("R"))) {

        BusinessPartner partner = listOp.get(0).getBusinessPartner();
        BigDecimal quotaProfiling = listOp.get(0).getOpportunityAmount();
        if (partner != null && quotaProfiling != null) {
          partner.setShppwsProfiledQuota(quotaProfiling);
          OBDal.getInstance().save(partner);
          OBDal.getInstance().flush();
          OBDal.getInstance().refresh(partner);
          return partner.getShppwsAvailableQuota().doubleValue();
        }
      }
    }
    return Quota_Available;
  }
  //
  // Se obtiene la respuesta de cada servicio Json
  //

  public String getApiResponse(shppws_config accesApi, String Identifier, int filternumber,
      JSONObject jsonMonitor, String referenceNo) throws Exception {
    log_records logger = new log_records();

    String apiUrl = "";
    String apiEndPoint = "";
    String apiTypeAuth = "";
    String apiUser = "";
    String apiPass = "";
    String apiToken = "";

    // Aplican para equifax
    String apiScope = "";
    String apiTokenURL = "";

    String Depurador = "";

    String Interface = "";
    String Process = "Externo";
    String idRegister = "";
    String Error = "";
    String nameService = "";
    String messageErrorService = "";
    String apiTokenPass = "";

    boolean statusSinergia = true;
    boolean statusEquifax = true;

    if (filternumber == 1) {
      Interface = "SHPPWS_RC";
      apiUrl = accesApi.getRCNamespace();
      apiEndPoint = accesApi.getRCReadEndpoint();
      apiTypeAuth = accesApi.getRCTypeAuth();
      apiUser = accesApi.getRCUser();
      apiPass = accesApi.getRCPass();
      apiToken = accesApi.getRCToken();
      nameService = "Registro Civil";
      messageErrorService = accesApi.getRecoverMessageError();
    } else if (filternumber == 2) {
      Interface = "SHPPWS_LN_CI";
      apiUrl = accesApi.getLN1Namespace();
      apiEndPoint = accesApi.getLN1ReadEndpoint();
      apiTypeAuth = accesApi.getLN1TypeAuth();
      apiUser = accesApi.getLN1User();
      apiPass = accesApi.getLN1Pass();
      apiToken = accesApi.getLN1Token();
      nameService = "Listas negras Cédula";
      messageErrorService = accesApi.getLN1MessageError();
    } else if (filternumber == 3) {
      Interface = "SHPPWS_LN_TLF";
      apiUrl = accesApi.getLN2Namespace();
      apiEndPoint = accesApi.getLN2ReadEndpoint();
      apiTypeAuth = accesApi.getLN2TypeAuth();
      apiUser = accesApi.getLN2User();
      apiPass = accesApi.getLN2Pass();
      apiToken = accesApi.getLN2Token();
      nameService = "Listas negras Teléfonos";
      messageErrorService = accesApi.getLN2MessageError();
    } else if (filternumber == 4) {
      Interface = "SHPPWS_LN_C";
      apiUrl = accesApi.getLN3Namespace();
      apiEndPoint = accesApi.getLN3ReadEndpoint();
      apiTypeAuth = accesApi.getLN3TypeAuth();
      apiUser = accesApi.getLN3User();
      apiPass = accesApi.getLN3Pass();
      apiToken = accesApi.getLN3Token();
      nameService = "Listas negras Correos";
      messageErrorService = accesApi.getLN3MessageError();
    } else if (filternumber == 5) {
      Interface = "SHPPWS_LN_DI";
      apiUrl = accesApi.getNamespace();
      apiEndPoint = accesApi.getReadEndpoint();
      apiTypeAuth = accesApi.getTypeAuth();
      apiUser = accesApi.getUser();
      apiPass = accesApi.getPass();
      apiToken = accesApi.getToken();
      nameService = "Listas negras Deudas Internas";
      messageErrorService = accesApi.getLndiMessageError();
    } else if (filternumber == 7) {
      Interface = "SHPPWS_EQ";
      apiUrl = accesApi.getEQNamespace();
      apiEndPoint = StringUtils.isNotBlank(accesApi.getEQReadEndpoint())
          ? accesApi.getEQReadEndpoint()
          : "NotEndpoint";
      apiTypeAuth = accesApi.getEQTypeAuth();
      apiUser = accesApi.getEQUser();
      apiPass = accesApi.getEQPass();
      apiTokenURL = accesApi.getEQToken(); // URL de autenticacion de Equifax
      apiScope = accesApi.getEQParams(); // Scopes de Equifax
      apiToken = getEquifaxToken(apiTokenURL, apiScope, apiUser, apiPass);
      nameService = "Equifax";
      messageErrorService = accesApi.getEquifaxMessageError();
      Process = "Equifax";
    } else if (filternumber == 8) {
      Interface = "SHPPWS_SN_HC";
      apiUrl = accesApi.getSynergyNameSpace();
      apiEndPoint = accesApi.getSynergyReadLastPoint();
      apiTypeAuth = accesApi.getSynergyAuthenticType();
      apiUser = accesApi.getSynergyUser();
      apiPass = accesApi.getSynergyKey();
      apiToken = accesApi.getSynergyToken();
      apiTokenPass = accesApi.getSynergyTokenPass();
      nameService = "Sinergia - Happycel";
      messageErrorService = accesApi.getSynergyMessageError();
      idRegister = "CEDULA";
    }
    Scactu_Log log = logger.log_start_register(accesApi, apiEndPoint, null);
    int responseCode = 500;
    HttpURLConnection connectionhttp = null;
    HttpsURLConnection connectionhttps = null;
    HttpURLConnection connection = null;
    JSONObject requestBody = new JSONObject();

    jsonMonitor.put(Interface, "Service" + filternumber);
    jsonMonitor.put("start" + Interface, LocalDateTime.now());
    jsonMonitor.put("type" + Interface, nameService);

    // BA -> Basic auth
    // TA -> Token auth
    if (apiTypeAuth.equals("BA")) {
      URL url = new URL(apiUrl + apiEndPoint);
      connectionhttp = (HttpURLConnection) url.openConnection();
      connectionhttp.setRequestMethod("GET");
      String username = apiUser;
      String password = apiPass;
      String authString = username + ":" + password;
      String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
      connectionhttp.setRequestProperty("Authorization", authHeaderValue);

      // Obtiene la respuesta de la API
      responseCode = connectionhttp.getResponseCode();
      connection = connectionhttp;
    } else if (apiTypeAuth.equals("AT")) {
      // Deshabilitar la validación de certificados SSL
      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
            String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
            String authType) {
        }
      } };

      // Configurar SSLContext con la configuración personalizada
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Obtener la conexión HTTPS y aplicar la configuración personalizada
      URL url = new URL(apiUrl + apiEndPoint);
      if (filternumber != 8) {
        connectionhttps = (HttpsURLConnection) url.openConnection();
        // Desactivar la verificación estricta del nombre del host
        connectionhttps.setHostnameVerifier((hostname, session) -> true);
        connectionhttps.setSSLSocketFactory(sslContext.getSocketFactory());
        String typeEndpoint = "POST";
        connectionhttps.setRequestMethod(typeEndpoint);

        // String token = apiToken;
        String token = apiToken;
        String authHeaderValue = "Bearer " + token;
        connectionhttps.setRequestProperty("Authorization", authHeaderValue);

        if (filternumber == 1) {
          requestBody.put("ci", Identifier);
          requestBody.put("apikey", apiToken);
          Depurador = Depurador + "Construye el body para RC";
        } else if (filternumber == 2) {
          requestBody.put("Cedula_Cliente", Identifier);
          requestBody.put("Key", apiToken);
          Depurador = Depurador + "Construye el body para LN1";
        } else if (filternumber == 3) {
          String tmpId = Identifier;
          try {
            String ultimosNueveDigitos = Identifier.substring(Identifier.length() - 9);
            String numeroCompleto = "0" + ultimosNueveDigitos;
            Identifier = Identifier + " - " + numeroCompleto;
            tmpId = numeroCompleto;
          } catch (Exception e) {
          }
          requestBody.put("Telefono_Cliente", tmpId);
          requestBody.put("Key", apiToken);
          Depurador = Depurador + "Construye el body para LN2";
        } else if (filternumber == 4) {
          requestBody.put("Correo_Cliente", Identifier);
          requestBody.put("Key", apiToken);
          Depurador = Depurador + "Construye el body para LN3";
        } else if (filternumber == 5) {
          requestBody.put("Cedula_Cliente", Identifier);
          requestBody.put("Key", apiToken);
          Depurador = Depurador + "Construye el body para DI";
        } else if (filternumber == 7) {
          JSONObject personalInformation = new JSONObject();
          personalInformation.put("tipoDocumento", "C");
          personalInformation.put("numeroDocumento", Identifier);

          JSONObject primaryConsumer = new JSONObject();
          primaryConsumer.put("personalInformation", personalInformation);

          JSONObject applicants = new JSONObject();
          applicants.put("primaryConsumer", primaryConsumer);

          // Sección nueva para productData
          JSONObject productData = new JSONObject();
          productData.put("billTo", accesApi.getBillTo());
          productData.put("shipTo", accesApi.getShipTo());
          productData.put("configuration", accesApi.getConfiguration());
          productData.put("customer", accesApi.getCustomerName());
          productData.put("model", accesApi.getModelName());
          requestBody.put("applicants", applicants);
          requestBody.put("productData", productData);

          Depurador = Depurador + " Construye el body para EQ " + requestBody.toString();
        }

        log = logger.log_setValues(log, requestBody.toString());
        connectionhttps.setRequestProperty("Content-Type", "application/json");

        connectionhttps.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connectionhttps.getOutputStream());
        writer.write(requestBody.toString());
        writer.flush();
        writer.close();

        Depurador = Depurador + "Obtiene la respuesta ";
        // Obtiene la respuesta de la API
        responseCode = connectionhttps.getResponseCode();
        connection = connectionhttps;
      } else {

        connectionhttp = (HttpURLConnection) url.openConnection();
        String typeEndpoint = "POST";
        // if(filternumber == 8) {
        url = new URL(url + "?Documento=" + Identifier);
        typeEndpoint = "GET";
        connectionhttp = (HttpURLConnection) url.openConnection();
        connectionhttp.setRequestMethod(typeEndpoint);

        // String token = apiToken;
        String apiUrlToken = apiToken;
        String basicHeaderToken = apiTokenPass;
        String token = GenerateToken(apiUrlToken, basicHeaderToken);
        String authHeaderValue = "Bearer " + token;
        connectionhttp.setRequestProperty("Bearer", token);
        connectionhttp.setRequestProperty("Accept", "application/json");
        requestBody.put("identificacion", Identifier);
        String RefernceNo = "Cedula: " + Identifier;
        Identifier = Identifier + " - " + RefernceNo;
        Process = "Consulta Información de Cliente";
        log = logger.log_setValues(log, requestBody.toString());

        Depurador = Depurador + "Obtiene la respuesta ";
        // Obtiene la respuesta de la API
        responseCode = connectionhttp.getResponseCode();
        connection = connectionhttp;
      }

    }
    jsonMonitor.put("status" + Interface, responseCode);
    jsonMonitor.put("end" + Interface, LocalDateTime.now());
    Identifier = referenceNo + " - " + Identifier;

    // if (filternumber == 7) {
    // responseCode = 400;
    // }

    if (responseCode == HttpURLConnection.HTTP_OK) {
      // S lee la respuesta de la API
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream()));
      StringBuilder responseBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        responseBuilder.append(line);
      }
      reader.close();

      // Se retorna el string de la api
      logger.log_end_register(log, apiUrl, Identifier, responseBuilder.toString(), "OK", "IN",
          Interface, Process, idRegister, Error);
      return responseBuilder.toString();
    } else {
      if (filternumber == 8) {
        statusSinergia = false;
        Error = "Error en la consulta a la API " + nameService + " Código de respuesta: "
            + responseCode;
        logger.log_end_register(log, apiUrl, Identifier, "Response Code " + responseCode, "ERROR",
            "IN", Interface, Process, idRegister, Error);
        return "Error sinergia";
      } else if (filternumber == 7) {
        statusSinergia = false;
        statusEquifax = false;
        Error = "Error en la consulta a la API " + nameService + " Código de respuesta: "
            + responseCode;
        logger.log_end_register(log, apiUrl, Identifier, "Response Code " + responseCode, "ERROR",
            "IN", Interface, Process, idRegister, Error);
        return "Error equifax";
      }
      if (filternumber != 7 && filternumber != 8) {
        Error = "Error en la consulta a la API " + nameService + " Código de respuesta: "
            + responseCode;
        logger.log_end_register(log, apiUrl, Identifier, "Response Code " + responseCode, "ERROR",
            "IN", Interface, Process, idRegister, Error);
        throw new Exception("Error " + responseCode + " " + messageErrorService);
      }
      return null;
    }
  }

  public static String GenerateToken(String apiUrl, String basicHeader) {
    HttpURLConnection connection = null;
    try {
      // Crear la URL y abrir la conexión
      URL url = new URL(apiUrl);
      connection = (HttpURLConnection) url.openConnection();

      // Configurar la conexión
      connection.setRequestMethod("POST");
      connection.setRequestProperty("basic", basicHeader);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      // Si no necesitas body, puedes omitir este bloque
      try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
        outputStream.writeBytes(""); // cuerpo vacío
        outputStream.flush();
      }

      // Leer la respuesta
      int responseCode = connection.getResponseCode();
      BufferedReader reader;
      if (responseCode >= 200 && responseCode < 300) {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      } else {
        reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
      }

      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      // Extraer el valor de "data" usando regex
      String responseText = response.toString();
      Pattern pattern = Pattern.compile("\"data\"\\s*:\\s*\"(.*?)\"");
      Matcher matcher = pattern.matcher(responseText);

      if (matcher.find()) {
        return matcher.group(1); // El token
      } else {
        return "No se encontró el campo 'data' en la respuesta: " + responseText;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  // Generacion de token para API Equifax
  public String getEquifaxToken(String tokenURL, String scope, String user, String password)
      throws Exception {
    // Reutilizar token si es válido
    if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
      return cachedToken;
    }

    // Construir cuerpo URL-encoded
    String body = "grant_type=client_credentials&scope=" + URLEncoder.encode(scope, "UTF-8");

    URL url = new URL(tokenURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    // Basic Auth
    String authString = user + ":" + password;
    String encodedAuth = Base64.getEncoder()
        .encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

    conn.setDoOutput(true);
    try (OutputStream os = conn.getOutputStream()) {
      os.write(body.getBytes(StandardCharsets.UTF_8));
    }

    int responseCode = conn.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      try (BufferedReader errorReader = new BufferedReader(
          new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
        StringBuilder errorResponse = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
          errorResponse.append(line.trim());
        }
        log4j.error(
            "Error al generar token Equifax. Código: " + responseCode + ", " + errorResponse);
        throw new RuntimeException(
            "Error al generar token Equifax. Código: " + responseCode + ", " + errorResponse);
      }
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line.trim());
      }
    }

    JSONObject jsonResponse = new JSONObject(response.toString());
    cachedToken = jsonResponse.getString("access_token");
    int expiresInSecs = jsonResponse.getInt("expires_in");

    tokenExpiryTime = System.currentTimeMillis() + (expiresInSecs * 1000L);

    return cachedToken;
  }

  //
  // Primer FILTRO
  //

  public void procesarApiRC(String apiResponse, List<shppev_age> reasonAges, String Store_group,
      Map<String, Object> personaRC) throws JSONException {
    // Converts the JSON string to a JSONObject
    JSONObject jsonResponse = new JSONObject(apiResponse);

    // Access the "persona" field
    JSONObject persona = jsonResponse.getJSONObject("persona");

    // Access the "datos" field
    JSONObject datos = persona.getJSONObject("datos");

    // Access the individual fields
    String name1 = datos.getString("nombreprimero");
    String name2 = datos.getString("nombresegundo");
    String name3 = datos.getString("nombretercero");
    String name4 = datos.getString("nombrecuarto");
    String name5 = datos.getString("nombrequinto");

    if (name2.isEmpty() && name3.isEmpty() && name4.isEmpty() && name5.isEmpty()) {
      personaRC.put("RCname1", name1);
      personaRC.put("RCname2", "");
    } else if (name3.isEmpty() && name4.isEmpty() && name5.isEmpty()) {
      personaRC.put("RCname1", name1);
      personaRC.put("RCname2", name2);
    } else if (name4.isEmpty() && name5.isEmpty()) {
      personaRC.put("RCname1", name1 + " " + name2);
      personaRC.put("RCname2", name3);
    } else if (name5.isEmpty()) {
      personaRC.put("RCname1", name1 + " " + name2);
      personaRC.put("RCname2", name3 + " " + name4);
    } else {
      personaRC.put("RCname1", name1 + " " + name2 + " " + name3);
      personaRC.put("RCname2", name4 + " " + name5);
    }

    personaRC.put("RCnames", datos.getString("nombres"));
    personaRC.put("RClastName1", datos.getString("apellidopaterno"));
    personaRC.put("RClastName2", datos.getString("apellidomaterno"));
    // personaRC.put("RCname1", datos.getString("nombreprimero"));
    // personaRC.put("RCname2", datos.getString("nombresegundo"));
    String RCgender = datos.getString("cod_sexo");
    if (RCgender.equals("HOMBRE")) {
      personaRC.put("RCgender", "MASCULINO");
    } else if (RCgender.equals("MUJER")) {
      personaRC.put("RCgender", "FEMENINO");
    } else {
      personaRC.put("RCgender", "OTRO");
    }
    personaRC.put("RCbirthdate", datos.getString("fecha_nacimiento"));
    personaRC.put("RCprofession", datos.getString("cod_profesion"));
    personaRC.put("RCcivilStatus", datos.getString("cod_estado_civil"));
    personaRC.put("RCnationality", datos.getString("nacionalidad"));
    personaRC.put("RCcedula", datos.getString("cedula"));
    String fecha_fallecimiento = datos.getString("fecha_fallecimiento");
    personaRC.put("RCfallecimiento", fecha_fallecimiento);

    if (StringUtils.isNotBlank(fecha_fallecimiento) && !fecha_fallecimiento.equals("null")) {
      personaRC.put("filterRC", "R");
      personaRC.put("matrizReason", "Matriz Edad");
      personaRC.put("idLN", "Matriz Edad");
      personaRC.put("msgLN", "No encontrado");
    }

    try {
      String RCcivilStatus = (String) personaRC.get("RCcivilStatus");
      OBCriteria<CivilStatus> queryCivilStatus = OBDal.getInstance()
          .createCriteria(CivilStatus.class);
      queryCivilStatus.add(Restrictions.eq(CivilStatus.PROPERTY_COMMERCIALNAME, RCcivilStatus));
      queryCivilStatus.setMaxResults(1);
      CivilStatus objCivilStatus = (CivilStatus) queryCivilStatus.uniqueResult();
      personaRC.put("RCcivilStatus", objCivilStatus.getValue());
    } catch (Exception e) {
    }

    LocalDate fechaActual = LocalDate.now();
    String nombres = datos.has("nombres") && datos.getString("nombres") != null
        ? datos.getString("nombres")
        : "";
    String fechaNacimientoTexto = datos.getString("fecha_nacimiento");
    if (!fechaNacimientoTexto.equals("ND") && !nombres.equals("Documento inexistente")) {
      // Define el formato de la fecha
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

      // Convert fecha de nacimiento en LocalDate
      LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoTexto, formatter);

      // Calcula la diferencia entre la fecha de nacimiento y la fecha actual
      Period diferencia = Period.between(fechaNacimiento, fechaActual);

      // Calcula la edad en años con parte decimal para meses y días
      double auxAge = diferencia.getYears() + ((double) diferencia.getMonths() / 12)
          + ((double) diferencia.getDays() / 365);
      BigDecimal auxAgeBigDecimal = new BigDecimal(auxAge);
      personaRC.put("auxAgeBigDecimal", auxAgeBigDecimal);
      // Comparación de campos "shop_group" y "id"
      String shopGroup = Store_group;
      Boolean validateDefaultRC = true;
      for (shppev_age reasonAge : reasonAges) {
        if (shopGroup.equals(reasonAge.getShopGroup())) {
          BigDecimal ageInitial = reasonAge.getInitialAge();
          BigDecimal ageFinal = reasonAge.getFinalAge();
          String answer = reasonAge.getAnswer();
          if (auxAgeBigDecimal.compareTo(ageInitial) > 0
              && auxAgeBigDecimal.compareTo(ageFinal) < 0) {
            validateDefaultRC = false;
            personaRC.put("matrizReason", "Matriz Edad");
            personaRC.put("idLN", auxAge + "");
            personaRC.put("msgLN", reasonAge.getMessage() + "");
            personaRC.put("filterRC", answer);
          }
          // break; // Si se encuentra una coincidencia, se detiene la iteración
        }
      }
      if (validateDefaultRC) {
        for (shppev_age deafultObj : reasonAges) {
          if (deafultObj.isShppwsDefaultField()) {
            personaRC.put("matrizReason", "Matriz Edad");
            personaRC.put("idLN", auxAge + "");
            personaRC.put("msgLN", deafultObj.getShppwsDefaultMessage() + "");
            personaRC.put("filterRC", deafultObj.getAnswer());
          }
        }
      }
      personaRC.put("message", " Filtro No 1, documento encontrado en el Registro Civil.");
      personaRC.put("matriz", " Motivo Edad");
      BigDecimal bigDecimalAge = BigDecimal.valueOf(auxAge);
      bigDecimalAge = bigDecimalAge.setScale(2, RoundingMode.HALF_UP);

      String auxFilter = (String) personaRC.get("filterRC");
      if (auxFilter != null && auxFilter.equals("C")) {
        // personaRC.put("matrizReason", "Motivos de Estado de Vida");

        // ESTADOS DE VIDA
        shppev_lifestyleReason objLifeStyle = new shppev_lifestyleReason();
        if (StringUtils.isNotBlank(fecha_fallecimiento) && !fecha_fallecimiento.equals("null")) {
          OBCriteria<shppev_lifestyleReason> queryLifeStyleReason = OBDal.getInstance()
              .createCriteria(shppev_lifestyleReason.class);// Estado de Vida
          queryLifeStyleReason.add(Restrictions.eq(shppev_lifestyleReason.PROPERTY_LIFESTYLE, "D"));
          List<shppev_lifestyleReason> listLifeStyleReason = queryLifeStyleReason.list();
          objLifeStyle = listLifeStyleReason.get(0);
          personaRC.put("matrizReason", "Motivos de Estado de Vida");
          personaRC.put("msgLN", objLifeStyle.getMessage() + "");
          personaRC.put("idLN", fecha_fallecimiento);
          personaRC.put("filterRC", objLifeStyle.getAnswer());
        }

      }

    } else {
      personaRC.put("message", " Filtro No 1, documento no encontrado en el Registro Civil.");
      personaRC.put("matriz", " Registro Civil");
      personaRC.put("matrizReason", "Documento no encontrado");
      personaRC.put("idLN", "Sin datos en Registro civil");
      personaRC.put("msgLN", "Documento no encontrado en el Registro Civil");
      personaRC.put("filterRC", "R");
      personaRC.put("validatorRC", "R");
    }

    String RCnationality = (String) personaRC.get("RCnationality");
    if (!RCnationality.equals("ECUATORIANA")) {
      personaRC.put("RCnationalityAux", "EXTRANJERO");
    } else {
      personaRC.put("RCnationalityAux", "ECUATORIANA");
    }
  }

  //
  // Verifica existencia de tercero
  //
  public BusinessPartner validatePartner(shppws_config accesApi, String ID,
      Map<String, Object> persona) throws Exception {
    // Se verifica existencia de un Tercero
    BusinessPartner partner = OBProvider.getInstance().get(BusinessPartner.class);
    String partnerID = "";
    try {
      OBCriteria<BusinessPartner> querypartner = OBDal.getInstance()
          .createCriteria(BusinessPartner.class);
      querypartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, ID));
      BusinessPartner alreadypartner = (BusinessPartner) querypartner.uniqueResult();
      partnerID = alreadypartner.getId();
      partner = alreadypartner;
    } catch (Exception e) {
      System.err.println("Error en EQnewSearch: " + e.getMessage());
    }

    try {
      if (partnerID.equals("")) {// Me crea un nuevo tercero
        partner.setClient(accesApi.getClient());
        partner.setOrganization(accesApi.getOrganization());
        partner.setActive(accesApi.isActive());
        partner.setCreatedBy(accesApi.getCreatedBy());
        partner.setUpdatedBy(accesApi.getUpdatedBy());

        partner.setSearchKey(ID); // identificador
        partner.setName((String) persona.get("RCname1")); // FiscalName
        partner.setName2((String) persona.get("RCname2"));
        partner.setTaxID(ID);
        BigDecimal bd = new BigDecimal("0");
        partner.setCreditLimit(bd);
        partner.setSsscrbpName((String) persona.get("RCname1"));
        partner.setSsscrbpName2((String) persona.get("RCname2"));
        partner.setSSSCRBPLastname((String) persona.get("RClastName1"));
        partner.setSsscrbpLastname2((String) persona.get("RClastName2"));

        partner.setBusinessPartnerCategory(accesApi.getBusinessPartnerCategory());

        partner.setSsscrbpTypeOfTaxpayer(accesApi.getTaxpayer());
        partner.setSswhTaxidtype(accesApi.getTypeID());
        partner.setSSWHTaxpayer(accesApi.getTaxpayerType());

        // cliente
        partner.setCustomer(accesApi.isCustomer());
        partner.setPriceList(accesApi.getPriceList());
        partner.setPaymentMethod(accesApi.getPaymentMethod());
        partner.setPaymentTerms(accesApi.getPaymentTerms());
        partner.setShppwsNationality((String) persona.get("RCnationalityAux"));
        partner.setEEIEmail((String) persona.get("email"));

        String dateBirth = (String) persona.get("RCbirthdate");
        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate localDate = LocalDate.parse(dateBirth, formatoEntrada);
        Date newDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        partner.setSbpcDatebirth(newDate);

        OBDal.getInstance().save(partner);
        OBDal.getInstance().flush();
        persona.put("partnerID", partner.getId());
      } else {// Me actualiza el Tercero existente
        partner.setClient(accesApi.getClient());
        partner.setOrganization(accesApi.getOrganization());
        partner.setActive(accesApi.isActive());
        partner.setCreatedBy(accesApi.getCreatedBy());
        partner.setUpdatedBy(accesApi.getUpdatedBy());

        partner.setSearchKey(ID); // identificador
        partner.setName((String) persona.get("RCname1")); // FiscalName
        partner.setName2((String) persona.get("RCname2"));
        partner.setTaxID(ID);
        BigDecimal bd = new BigDecimal("0");
        partner.setCreditLimit(bd);
        partner.setSsscrbpName((String) persona.get("RCname1"));
        partner.setSsscrbpName2((String) persona.get("RCname2"));
        partner.setSSSCRBPLastname((String) persona.get("RClastName1"));
        partner.setSsscrbpLastname2((String) persona.get("RClastName2"));
        partner.setSsscrbpTypeOfTaxpayer(accesApi.getTaxpayer());
        partner.setBusinessPartnerCategory(accesApi.getBusinessPartnerCategory());
        partner.setSswhTaxidtype(accesApi.getTypeID());
        partner.setSSWHTaxpayer(accesApi.getTaxpayerType());
        partner.setCustomer(accesApi.isCustomer());
        partner.setPriceList(accesApi.getPriceList());

        // cliente
        partner.setCustomer(accesApi.isCustomer());
        partner.setPriceList(accesApi.getPriceList());
        partner.setPaymentMethod(accesApi.getPaymentMethod());
        partner.setPaymentTerms(accesApi.getPaymentTerms());
        partner.setShppwsNationality((String) persona.get("RCnationalityAux"));
        // partner.setEEIEmail((String)persona.get("email"));

        String dateBirth = (String) persona.get("RCbirthdate");
        if (StringUtils.isNotBlank(dateBirth)) {
          DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("d/M/yyyy");
          LocalDate localDate = LocalDate.parse(dateBirth, formatoEntrada);
          Date newDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
          partner.setSbpcDatebirth(newDate);
        }

        OBDal.getInstance().save(partner);
        OBDal.getInstance().flush();
        persona.put("partnerID", partner.getId());

      }

      OBCriteria<Location> queryLocation = OBDal.getInstance().createCriteria(Location.class);
      queryLocation.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, partner));
      queryLocation.setMaxResults(1);
      Location address = OBProvider.getInstance().get(Location.class);
      if (queryLocation.list().isEmpty()) {
        // Dirección
        address.setClient(accesApi.getClient());
        address.setOrganization(accesApi.getOrganization());
        address.setActive(accesApi.isActive());
        address.setCreatedBy(accesApi.getCreatedBy());
        address.setUpdatedBy(accesApi.getUpdatedBy());
        address.setBusinessPartner(partner);
        address.setName("Default Location");
        address.setScactuCellphoneNumber((String) persona.get("CellPhone"));
        address.setPhone((String) persona.get("CellPhone"));
        OBDal.getInstance().save(address);
        OBDal.getInstance().flush();
      }
    } catch (Exception e) {
      throw new Exception("No se ha podido procesar la información del tercero");
    }
    return partner;
  }

  //
  // Filter blacklist
  //
  public boolean validateLN_CI(String ID, String apiResponse, Map<String, Object> persona)
      throws JSONException {
    JSONArray arrayBlacklist = new JSONArray(apiResponse);
    Boolean validate_CI = true;

    for (int i = 0; i < arrayBlacklist.length(); i++) {
      JSONObject objBlacklist = arrayBlacklist.getJSONObject(i);
      String auxValidate = objBlacklist.getString("motivo");
      if (auxValidate.equals("No existe motivo.") || auxValidate.equals("ERROR KEY")) {
        persona.put("LNCedulaStatus", "CedulaOK");
      } else {
        try {
          OBCriteria<shppev_rIdentification> queryReason = OBDal.getInstance()
              .createCriteria(shppev_rIdentification.class);
          queryReason.add(Restrictions.eq(shppev_rIdentification.PROPERTY_REASON, auxValidate));
          if (!queryReason.list().isEmpty()) {
            // error en cedula
            persona.put("LNCedulaStatus", "ErrorCedula");
            persona.put("LNCedulaMotivo", auxValidate);
            persona.put("LNCedulaAll", arrayBlacklist.toString());
            validate_CI = false;
          }

        } catch (Exception e) {
          SHPPWS_Helper_Model.putSectionRejected(persona, "msg erroneo en motivo cédula", ID,
              "Motivo cédula");
          persona.put("haserror", "Motivo cédula");
          break;
        }
      }
    }
    return validate_CI;
  }

  public boolean validateLN_Cellphone(String ID, String apiResponse, Map<String, Object> persona)
      throws JSONException {
    JSONArray arrayBlacklist = new JSONArray(apiResponse);
    Boolean validate_Celphone = true;

    for (int i = 0; i < arrayBlacklist.length(); i++) {
      JSONObject objBlacklist = arrayBlacklist.getJSONObject(i);
      String auxValidate = objBlacklist.getString("tipo");
      if (auxValidate.equals("No existe registro.") || auxValidate.equals("ERROR KEY")) {
        persona.put("LNPhoneStatus", "PhoneOk");
      } else {
        try {
          OBCriteria<shppev_phoneReason> queryReason = OBDal.getInstance()
              .createCriteria(shppev_phoneReason.class);
          queryReason.add(Restrictions.eq(shppev_phoneReason.PROPERTY_REASON, auxValidate));
          if (!queryReason.list().isEmpty()) {
            // error en cedula
            persona.put("LNPhoneStatus", "ErrorPhone");
            persona.put("LNPhoneMotivo", auxValidate);
            persona.put("LNPhoneAll", objBlacklist.toString());
            validate_Celphone = false;
          }
        } catch (Exception e) {
          SHPPWS_Helper_Model.putSectionRejected(persona, "msg erroneo en motivo teléfono", ID,
              "Motivo teléfono");
          persona.put("haserror", "Motivo teléfono");
          break;
        }
      }
    }
    return validate_Celphone;
  }

  public boolean validateLN_Email(String ID, String apiResponse, Map<String, Object> persona)
      throws JSONException {
    JSONArray arrayBlacklist = new JSONArray(apiResponse);
    Boolean validate_Email = true;

    for (int i = 0; i < arrayBlacklist.length(); i++) {
      JSONObject objBlacklist = arrayBlacklist.getJSONObject(i);
      String auxValidate = objBlacklist.getString("tipo");
      if (auxValidate.equals("No existe registro.") || auxValidate.equals("ERROR KEY")) {
        persona.put("LNMailStatus", "MailOk");
      } else {
        try {
          OBCriteria<shppev_emailReason> queryReason = OBDal.getInstance()
              .createCriteria(shppev_emailReason.class);
          queryReason.add(Restrictions.eq(shppev_emailReason.PROPERTY_REASON, auxValidate));
          if (!queryReason.list().isEmpty()) {
            persona.put("LNMailStatus", "ErrorMail");
            persona.put("LNMailMotivo", auxValidate);
            persona.put("LNMailAll", objBlacklist.toString());
            validate_Email = false;
          }
        } catch (Exception e) {
          SHPPWS_Helper_Model.putSectionRejected(persona, "msg erroneo en motivo correo", ID,
              "Motivo correo");
          persona.put("haserror", "Motivo correo");
          break;
        }
      }
    }
    return validate_Email;
  }

  public boolean validateLN_Profession(Map<String, Object> persona) throws JSONException {
    String RCprofession = (String) persona.get("RCprofession");
    OBCriteria<shppev_reasonProfession> queryProfession = OBDal.getInstance()
        .createCriteria(shppev_reasonProfession.class);
    queryProfession.add(Restrictions.eq(shppev_reasonProfession.PROPERTY_PROFESSION, RCprofession));
    List<shppev_reasonProfession> listProfessions = queryProfession.list();
    if (!queryProfession.list().isEmpty()) {
      shppev_reasonProfession objReason = listProfessions.get(0);
      persona.put("msgLN", objReason.getMessage());
      persona.put("idLN", RCprofession);
      persona.put("matrizReason", "Motivo Profesión");
      return false;
    } else {
      return true;
    }
  }

  public boolean validateDataDI(String apiResponse, Map<String, Object> persona)
      throws JSONException {// Deuda interna
    JSONArray arrayBlacklist = new JSONArray(apiResponse);
    Boolean validate_DI = true;
    Boolean validateDefault = true;
    for (int i = 0; i < arrayBlacklist.length(); i++) {
      JSONObject objInternalDue = arrayBlacklist.getJSONObject(i);
      String auxValidate = objInternalDue.getString("cedente");
      BigDecimal auxAmmount = new BigDecimal(objInternalDue.getString("total_Pagar"));
      Long auxDueDays = Long.getLong(objInternalDue.getString("dias_Mora"));
      if (auxAmmount == null || auxAmmount.compareTo(BigDecimal.ZERO) < 0) {
        auxAmmount = BigDecimal.ZERO;
      }
      if (auxDueDays == null || auxDueDays < 0) {
        auxDueDays = Long.valueOf(0L);
      }
      persona.put("recover_days_late", auxDueDays);
      persona.put("recover_amount_pay", auxAmmount);
      if (auxValidate.equals("No existe credito.") || auxValidate.equals("ERROR KEY")) {
        validate_DI = true;
        persona.put("LNIntDebtStatus", "DeudaInternaOk");
      } else {
        try {
          OBCriteria<shppev_internalDebt> queryReason = OBDal.getInstance()
              .createCriteria(shppev_internalDebt.class);
          queryReason.add(Restrictions.eq(shppev_internalDebt.PROPERTY_CREDITOR, auxValidate));
          List<shppev_internalDebt> listReason = queryReason.list();
          if (listReason.size() > 0) {
            for (shppev_internalDebt objReason : listReason) {
              if (auxAmmount.compareTo(objReason.getLowerrangemount()) >= 0
                  && auxAmmount.compareTo(objReason.getUpperrangemount()) <= 0) {
                if ((auxDueDays >= objReason.getLowerrangedays())
                    && (auxDueDays <= objReason.getUpperrangedays())) {
                  validateDefault = false;
                  String response = objReason.getAnswer();
                  if (response.equals("C")) {
                    validate_DI = true;
                  } else {
                    SHPPWS_Helper_Model.putSectionRejected(persona, objReason.getMessage(),
                        auxValidate + ", " + auxAmmount + ", " + auxDueDays, "Deuda Interna");
                    persona.put("haserror", "Deuda Interna");

                    persona.put("LNIntDebtStatus", "ErrorDeudaInterna");
                    persona.put("LNIntDebtMotivo", auxValidate);
                    persona.put("LNIntDebtAll", objInternalDue.toString());

                    validate_DI = false;
                    break;
                  }
                }
              }
            }
          }

          // DEFAULT
          if (validateDefault) {
            OBCriteria<shppev_internalDebt> queryDefault = OBDal.getInstance()
                .createCriteria(shppev_internalDebt.class);
            List<shppev_internalDebt> listyDefault = queryDefault.list();
            for (shppev_internalDebt deafultObj : listyDefault) {
              if (deafultObj.isShppwsDefaultField()) {
                String response = deafultObj.getAnswer();
                if (response.equals("C")) {
                  validate_DI = true;
                } else {
                  SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getMessage(),
                      auxValidate + ", " + auxAmmount + ", " + auxDueDays, "Deuda Interna");
                  persona.put("haserror", "Deuda Interna");

                  persona.put("filterRC", deafultObj.getAnswer());
                  validate_DI = false;

                  persona.put("LNIntDebtStatus", "ErrorDeudaInterna");
                  persona.put("LNIntDebtMotivo", auxValidate);
                  persona.put("LNIntDebtAll", objInternalDue.toString());
                  break;
                }
              }
            }
          }

        } catch (Exception e) {
          SHPPWS_Helper_Model.putSectionRejected(persona, "msg erroneo en servicio Deuda Interna",
              auxValidate + ", " + auxAmmount + ", " + auxDueDays, "Deuda Interna");
          persona.put("haserror", "Deuda Interna");

          persona.put("LNIntDebtStatus", "ErrorDeudaInterna");
          persona.put("LNIntDebtMotivo", auxValidate);
          persona.put("LNIntDebtAll", objInternalDue.toString());
          validate_DI = false;
          break;
        }
      }
    }
    return validate_DI;

  }

  public String validateStatusPartner(String apiResponse) throws JSONException {
    JSONObject jsonResponse = new JSONObject(apiResponse);
    String respuestaApi = jsonResponse.getString("clasificacion_cliente");
    String TipoClienteAprobado = "";
    OBCriteria<shppwsPartnertype> queryPartnerType = OBDal.getInstance()
        .createCriteria(shppwsPartnertype.class);
    queryPartnerType.add(Restrictions.eq(shppwsPartnertype.PROPERTY_SEARCHKEY, respuestaApi));
    shppwsPartnertype accesPartnerType = (shppwsPartnertype) queryPartnerType.uniqueResult();
    if (!queryPartnerType.list().isEmpty()) {
      TipoClienteAprobado = accesPartnerType.getCommercialName();
    } else {
      TipoClienteAprobado = "CLIENTE RECHAZADO";// CLIENTE RECHAZADO
    }
    return TipoClienteAprobado;
  }

  public String validateStatusPartnerDebitIn(String apiResponse) throws JSONException {

    String respuestaApi = "";

    // Detectar si es JSONArray o JSONObject
    // JSONTokener es para interpretar y analizar cadenas JSON
    Object json = new JSONTokener(apiResponse).nextValue();

    if (json instanceof JSONArray) {
      // Caso: [ { ... } ]
      JSONArray arr = (JSONArray) json;
      if (arr.length() > 0) {
        JSONObject obj = arr.getJSONObject(0);
        respuestaApi = obj.optString("clasificacion_cliente", "");
      }
    } else if (json instanceof JSONObject) {
      // Caso: { ... }
      JSONObject obj = new JSONObject(apiResponse);
      respuestaApi = obj.optString("clasificacion_cliente", "");
    } else {
      throw new JSONException("Formato JSON no reconocido");
    }

    String TipoClienteAprobado = "";

    OBCriteria<shppwsPartnertype> queryPartnerType = OBDal.getInstance()
        .createCriteria(shppwsPartnertype.class);
    queryPartnerType.add(Restrictions.eq(shppwsPartnertype.PROPERTY_SEARCHKEY, respuestaApi));

    List<shppwsPartnertype> list = queryPartnerType.list();

    if (list != null && !list.isEmpty()) {
      TipoClienteAprobado = list.get(0).getCommercialName();
    } else {
      TipoClienteAprobado = "CLIENTE RECHAZADO";
    }

    return TipoClienteAprobado;
  }

  public String classificationClient(String apiResponse) throws JSONException {
    JSONObject jsonResponse = new JSONObject(apiResponse);
    String respuestaApi = jsonResponse.getString("clasificacion_cliente");
    return respuestaApi;
  }

  public boolean customerNewOld(Map<String, Object> persona, BusinessPartner partner)
      throws JSONException {
    String identifier = (String) persona.get("Identifier");
    OBCriteria<Invoice> crtInv = OBDal.getInstance().createCriteria(Invoice.class);
    crtInv.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, partner));
    crtInv.add(Restrictions.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
    crtInv.setMaxResults(1);
    return crtInv.list().isEmpty();
  }

  public boolean customerOldCC_uncheck(BusinessPartner partner, Map<String, Object> persona)
      throws JSONException {
    Long ccPartner;
    try {
      ccPartner = partner.getShpctNoPunishedCredits();
      if (ccPartner == null) {
        ccPartner = Long.valueOf(0L);
      }
    } catch (Exception e) {
      ccPartner = Long.valueOf(0L);
    }

    OBCriteria<Shppec_CredPen> queryCredPen = OBDal.getInstance()
        .createCriteria(Shppec_CredPen.class);
    List<Shppec_CredPen> listobjCredPen = queryCredPen.list();
    String matrizStr = "Matriz Crédito Castigado";

    if (listobjCredPen.size() > 0) {
      for (Shppec_CredPen objCredPen : listobjCredPen) {
        String validatorLogic = objCredPen.getCompare();
        BigDecimal validatorValue = objCredPen.getCredPenal();
        Long newvalidatorValue = validatorValue.longValue();
        if (validatorLogic.equals("<=")) {
          if (ccPartner <= newvalidatorValue) {
            String response = objCredPen.getOutput();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                  ccPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals(">=")) {
          if (ccPartner >= newvalidatorValue) {
            String response = objCredPen.getOutput();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                  ccPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals(">")) {
          if (ccPartner > newvalidatorValue) {
            String response = objCredPen.getOutput();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                  ccPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals("<")) {
          if (ccPartner < newvalidatorValue) {
            String response = objCredPen.getOutput();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                  ccPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals("=")) {
          if (ccPartner.equals(newvalidatorValue)) {
            String response = objCredPen.getOutput();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                  ccPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        }
      }
    }

    // DEFAULT
    for (Shppec_CredPen deafultObj : listobjCredPen) {
      if (deafultObj.isShppwsDefaultField()) {
        String response = deafultObj.getOutput();
        if (response.equals("R")) {
          SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
              ccPartner.toString(), matrizStr);
          persona.put("haserror", matrizStr);
          return false;
        } else {
          return true;
        }
      }
    }

    SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado", ccPartner.toString(),
        matrizStr);
    persona.put("haserror", matrizStr);
    return false;
  }

  public boolean customerOldCV_uncheck(BusinessPartner partner, Map<String, Object> persona)
      throws JSONException {
    Long cvPartner;
    try {
      cvPartner = partner.getShpctNoCurrentCredits();
      if (cvPartner == null) {
        cvPartner = Long.valueOf(0L);
      }
    } catch (Exception e) {
      cvPartner = Long.valueOf(0L);
    }

    OBCriteria<Shppec_CredCurr> queryCredCurr = OBDal.getInstance()
        .createCriteria(Shppec_CredCurr.class);
    List<Shppec_CredCurr> listobjCredCurr = queryCredCurr.list();
    String matrizStr = "Matriz Crédito Vigente";

    if (listobjCredCurr.size() > 0) {
      for (Shppec_CredCurr objCredV : listobjCredCurr) {
        String validatorLogic = objCredV.getCompare();
        BigDecimal validatorValue = objCredV.getCredCurrent();
        Long newvalidatorValue = validatorValue.longValue();
        if (validatorLogic.equals("<=")) {
          if (cvPartner <= newvalidatorValue) {
            String response = objCredV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                  cvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals(">=")) {
          if (cvPartner >= newvalidatorValue) {
            String response = objCredV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                  cvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals(">")) {
          if (cvPartner > newvalidatorValue) {
            String response = objCredV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                  cvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals("<")) {
          if (cvPartner < newvalidatorValue) {
            String response = objCredV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                  cvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals("=")) {
          if (cvPartner.equals(newvalidatorValue)) {
            String response = objCredV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                  cvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        }
      }
    }

    // DEFAULT
    for (Shppec_CredCurr deafultObj : listobjCredCurr) {
      if (deafultObj.isShppwsDefaultField()) {
        String response = deafultObj.getResult();
        if (response.equals("R")) {
          SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
              cvPartner.toString(), matrizStr);
          persona.put("haserror", matrizStr);
          return false;
        } else {
          return true;
        }
      }
    }

    SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado", cvPartner.toString(),
        matrizStr);
    persona.put("haserror", matrizStr);
    return false;
  }

  public boolean customerOldCVV_uncheck(BusinessPartner partner, Map<String, Object> persona)
      throws JSONException {
    Long cvvPartner;
    try {
      cvvPartner = partner.getShpctNoCCreditsExpired();
      if (cvvPartner == null) {
        cvvPartner = Long.valueOf(0L);
      }
    } catch (Exception e) {
      cvvPartner = Long.valueOf(0L);
    }

    OBCriteria<Shppec_CredExp> queryCredExp = OBDal.getInstance()
        .createCriteria(Shppec_CredExp.class);
    List<Shppec_CredExp> listobjCredExp = queryCredExp.list();
    String matrizStr = "Matriz Crédito Vigente Vencido";

    if (listobjCredExp.size() > 0) {
      for (Shppec_CredExp objCredVV : listobjCredExp) {
        String validatorLogic = objCredVV.getCompare();
        BigDecimal validatorValue = objCredVV.getCredExpired();
        Long newvalidatorValue = validatorValue.longValue();
        if (validatorLogic.equals("<=")) {
          if (cvvPartner <= newvalidatorValue) {
            String response = objCredVV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                  cvvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals(">=")) {
          if (cvvPartner >= newvalidatorValue) {
            String response = objCredVV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                  cvvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals(">")) {
          if (cvvPartner > newvalidatorValue) {
            String response = objCredVV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                  cvvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals("<")) {
          if (cvvPartner < newvalidatorValue) {
            String response = objCredVV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                  cvvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        } else if (validatorLogic.equals("=")) {
          if (cvvPartner.equals(newvalidatorValue)) {
            String response = objCredVV.getResult();
            if (response.equals("R")) {
              SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                  cvvPartner.toString(), matrizStr);
              persona.put("haserror", matrizStr);
              return false;
            } else {
              return true;
            }
          }
        }
      }
    }

    // DEFAULT
    for (Shppec_CredExp deafultObj : listobjCredExp) {
      if (deafultObj.isShppwsDefaultField()) {
        String response = deafultObj.getResult();
        if (response.equals("R")) {
          SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
              cvvPartner.toString(), matrizStr);
          persona.put("haserror", matrizStr);
          return false;
        } else {
          return true;
        }
      }
    }
    SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado", cvvPartner.toString(),
        matrizStr);
    persona.put("haserror", matrizStr);
    return false;
  }

  public String SearchClieSegm(String apiResponse) throws JSONException {
    JSONObject jsonResponse = new JSONObject(apiResponse);
    JSONObject comportamientoCrediticio = jsonResponse.getJSONObject("data")
        .getJSONObject("comportamiento_crediticio");
    return comportamientoCrediticio.getString("CLIE_PERFIL_COMP_PAGO_INTERNO");
  }

  public boolean customerOldCC(BusinessPartner partner, Map<String, Object> persona,
      String typePartner) throws JSONException {
    Long ccPartner;
    try {
      ccPartner = partner.getShpctNoPunishedCredits();
      if (ccPartner == null) {
        ccPartner = Long.valueOf(0L);
      }
    } catch (Exception e) {
      ccPartner = Long.valueOf(0L);
    }
    String matrizStr = "Matriz Crédito Castigado";

    if (typePartner.equals("CLIENTE NUEVO") || typePartner.equals("CLIENTE APROBADO")) {
      OBCriteria<Shppec_CredPen> queryCredPen = OBDal.getInstance()
          .createCriteria(Shppec_CredPen.class);
      List<Shppec_CredPen> listobjCredPen = queryCredPen.list();

      if (listobjCredPen.size() > 0) {
        for (Shppec_CredPen objCredPen : listobjCredPen) {
          String validatorLogic = objCredPen.getCompare();
          BigDecimal validatorValue = objCredPen.getCredPenal();
          Long newvalidatorValue = validatorValue.longValue();
          if (validatorLogic.equals("<=")) {
            if (ccPartner <= newvalidatorValue) {
              String response = objCredPen.getOutput();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                    ccPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals(">=")) {
            if (ccPartner >= newvalidatorValue) {
              String response = objCredPen.getOutput();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                    ccPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals(">")) {
            if (ccPartner > newvalidatorValue) {
              String response = objCredPen.getOutput();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                    ccPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals("<")) {
            if (ccPartner < newvalidatorValue) {
              String response = objCredPen.getOutput();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                    ccPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals("=")) {
            if (ccPartner.equals(newvalidatorValue)) {
              String response = objCredPen.getOutput();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredPen.getMessage(),
                    ccPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          }
        }
      }

      // DEFAULT
      for (Shppec_CredPen deafultObj : listobjCredPen) {
        if (deafultObj.isShppwsDefaultField()) {
          String response = deafultObj.getOutput();
          if (response.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
                ccPartner.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            return false;
          } else {
            return true;
          }
        }
      }

      SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado",
          ccPartner.toString(), matrizStr);
      persona.put("haserror", matrizStr);
      return false;
    } else {
      OBCriteria<shppee_Quotas> qDefaultMsg = OBDal.getInstance()
          .createCriteria(shppee_Quotas.class);
      qDefaultMsg.add(Restrictions.eq(shppee_Quotas.PROPERTY_SHPPWSDEFAULTFIELD, true));
      qDefaultMsg.setMaxResults(1);
      shppee_Quotas msgDefault = (shppee_Quotas) qDefaultMsg.uniqueResult();
      SHPPWS_Helper_Model.putSectionRejected(persona, msgDefault.getShppwsDefaultMessage(),
          ccPartner.toString(), matrizStr);
      persona.put("haserror", matrizStr);
      return false;
    }
  }

  public boolean customerOldCV(BusinessPartner partner, Map<String, Object> persona,
      String typePartner) throws JSONException {
    Long cvPartner;
    try {
      cvPartner = partner.getShpctNoCurrentCredits();
      if (cvPartner == null) {
        cvPartner = Long.valueOf(0L);
      }
    } catch (Exception e) {
      cvPartner = Long.valueOf(0L);
    }
    String matrizStr = "Matriz Crédito Vigente";

    if (typePartner.equals("CLIENTE NUEVO") || typePartner.equals("CLIENTE APROBADO")) {
      OBCriteria<Shppec_CredCurr> queryCredCurr = OBDal.getInstance()
          .createCriteria(Shppec_CredCurr.class);
      List<Shppec_CredCurr> listobjCredCurr = queryCredCurr.list();
      if (listobjCredCurr.size() > 0) {
        for (Shppec_CredCurr objCredV : listobjCredCurr) {
          String validatorLogic = objCredV.getCompare();
          BigDecimal validatorValue = objCredV.getCredCurrent();
          Long newvalidatorValue = validatorValue.longValue();
          if (validatorLogic.equals("<=")) {
            if (cvPartner <= newvalidatorValue) {
              String response = objCredV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                    cvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals(">=")) {
            if (cvPartner >= newvalidatorValue) {
              String response = objCredV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                    cvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals(">")) {
            if (cvPartner > newvalidatorValue) {
              String response = objCredV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                    cvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals("<")) {
            if (cvPartner < newvalidatorValue) {
              String response = objCredV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                    cvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals("=")) {
            if (cvPartner.equals(newvalidatorValue)) {
              String response = objCredV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredV.getMessage(),
                    cvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          }
        }
      }

      // DEFAULT
      for (Shppec_CredCurr deafultObj : listobjCredCurr) {
        if (deafultObj.isShppwsDefaultField()) {
          String response = deafultObj.getResult();
          if (response.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
                cvPartner.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            return false;
          } else {
            return true;
          }
        }
      }

      SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado",
          cvPartner.toString(), matrizStr);
      persona.put("haserror", matrizStr);
      return false;
    } else {

      SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado",
          cvPartner.toString(), matrizStr);
      persona.put("haserror", matrizStr);
      return false;
    }
  }

  public boolean customerOldCVV(BusinessPartner partner, Map<String, Object> persona,
      String typePartner) throws JSONException {

    Long cvvPartner;
    try {
      cvvPartner = partner.getShpctNoCCreditsExpired();
      if (cvvPartner == null) {
        cvvPartner = Long.valueOf(0L);
      }
    } catch (Exception e) {
      cvvPartner = Long.valueOf(0L);
    }
    String matrizStr = "Matriz Crédito Vigente Vencido";

    if (typePartner.equals("CLIENTE NUEVO") || typePartner.equals("CLIENTE APROBADO")) {
      OBCriteria<Shppec_CredExp> queryCredExp = OBDal.getInstance()
          .createCriteria(Shppec_CredExp.class);
      if (!queryCredExp.list().isEmpty()) {
        for (Shppec_CredExp objCredVV : queryCredExp.list()) {
          String validatorLogic = objCredVV.getCompare();
          BigDecimal validatorValue = objCredVV.getCredExpired();
          Long newvalidatorValue = validatorValue.longValue();
          if (validatorLogic.equals("<=")) {
            if (cvvPartner <= newvalidatorValue) {
              String response = objCredVV.getResult();

              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                    cvvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals(">=")) {
            if (cvvPartner >= newvalidatorValue) {
              String response = objCredVV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                    cvvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals(">")) {
            if (cvvPartner > newvalidatorValue) {
              String response = objCredVV.getResult();

              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                    cvvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals("<")) {
            if (cvvPartner < newvalidatorValue) {
              String response = objCredVV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                    cvvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          } else if (validatorLogic.equals("=")) {
            if (cvvPartner.equals(newvalidatorValue)) {
              String response = objCredVV.getResult();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objCredVV.getMessage(),
                    cvvPartner.toString(), matrizStr);
                persona.put("haserror", matrizStr);
                return false;
              } else {
                return true;
              }
            }
          }
        }
      }

      // DEFAULT
      for (Shppec_CredExp deafultObj : queryCredExp.list()) {
        if (deafultObj.isShppwsDefaultField()) {
          String response = deafultObj.getResult();
          if (response.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
                cvvPartner.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            return false;
          } else {
            return true;
          }
        }
      }
      SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado",
          cvvPartner.toString(), matrizStr);
      persona.put("haserror", matrizStr);
      return false;
    } else {
      SHPPWS_Helper_Model.putSectionRejected(persona, "Registro no encontrado",
          cvvPartner.toString(), matrizStr);
      persona.put("haserror", matrizStr);
      return false;
    }
  }

  public String parallelCredits(BusinessPartner partner, Map<String, Object> persona)
      throws JSONException {
    Long mfpaid = partner.getShpctMaximumFeePaid(); // N0 plazo
    Long lipaid = partner.getShpctLastInstallmentpaid();// Última cuota

    if (lipaid == null) {
      lipaid = Long.valueOf(0L);
    }
    if (mfpaid == null) {
      mfpaid = Long.valueOf(0L);
    }

    OBCriteria<Shppec_ParallelC> queryParallelC = OBDal.getInstance()
        .createCriteria(Shppec_ParallelC.class);
    List<Shppec_ParallelC> listObjParallelC = queryParallelC.list();
    if (listObjParallelC.size() > 0) {
      for (Shppec_ParallelC objParallelC : listObjParallelC) {
        String validatorLogic = objParallelC.getComparison();
        Long validatorTerm = objParallelC.getTerm();
        Long validatorQuota = objParallelC.getMINQuote();
        if (validatorLogic.equals("<=")) {
          if (mfpaid.equals(validatorTerm) && lipaid <= validatorQuota) {
            String response = objParallelC.getResponse();
            return response;
          }
        } else if (validatorLogic.equals(">=")) {
          if (mfpaid.equals(validatorTerm) && lipaid >= validatorQuota) {
            String response = objParallelC.getResponse();
            return response;
          }
        } else if (validatorLogic.equals("<")) {
          if (mfpaid.equals(validatorTerm) && lipaid < validatorQuota) {
            String response = objParallelC.getResponse();
            return response;
          }
        } else if (validatorLogic.equals(">")) {
          if (mfpaid.equals(validatorTerm) && lipaid > validatorQuota) {
            String response = objParallelC.getResponse();
            return response;
          }
        } else if (validatorLogic.equals("=")) {
          if (mfpaid.equals(validatorTerm) && lipaid.equals(validatorQuota)) {
            String response = objParallelC.getResponse();
            return response;
          }
        }
      }
    }

    // DEFAULT X
    for (Shppec_ParallelC deafultObj : listObjParallelC) {
      if (deafultObj.isShppwsDefaultField()) {
        String response = deafultObj.getResponse();
        return response;
      }
    }

    return "F";
  }

  public String customerRiskIndex(BusinessPartner partner, String VoF, Map<String, Object> persona,
      shppws_config accesApi, boolean newClient) throws JSONException {

    // No. Créditos Pagados
    Long ncpaid = partner.getShpctNoCreditsPaid();
    // Índice de Riesgo
    BigDecimal irisk = partner.getShpctRiskIndex();
    String segmentationEQ = (String) persona.get("EQ_segmentacion");

    // Validacion Inclucion
    boolean isInclusion = accesApi.isEquifaxInclusion();
    String codeFlt = newClient ? accesApi.getEquifaxProfNewclient()
        : accesApi.getEquifaxProfOldclient();

    if (ncpaid == null) {
      ncpaid = Long.valueOf(0L);
    }
    if (irisk == null) {
      irisk = BigDecimal.ZERO;
    }
    Long newvalueRiskIndex = irisk.longValue();

    OBCriteria<shppee_RiskIndex> queryRiskIndex = OBDal.getInstance()
        .createCriteria(shppee_RiskIndex.class);
    queryRiskIndex.add(Restrictions.eq(shppee_RiskIndex.PROPERTY_ANSWER, VoF));
    // List<shppee_RiskIndex> listObjRiskIndex = queryRiskIndex.list();
    String matrizStr = "Matriz Índice de Riesgo";
    String idStr = "Riesgo:" + irisk + ", " + " Créditos pagados:" + ncpaid;

    if (!queryRiskIndex.list().isEmpty()) {
      for (shppee_RiskIndex objParallelC : queryRiskIndex.list()) {
        String EQsegments = objParallelC.getProfileCampPayment();
        Long credcan = Long.valueOf(objParallelC.getCredCanc());
        Long from = Long.valueOf(objParallelC.getFrom());
        Long until = Long.valueOf(objParallelC.getUntil());
        // SI SE MANTIENE CON .contains() ABARCA VACIOS
        if (segmentationEQ != null && !segmentationEQ.isEmpty()
            && EQsegments.contains(segmentationEQ)
            && (newvalueRiskIndex >= from && newvalueRiskIndex <= until)) {
          String validatorLogic = objParallelC.getCompare();

          if (validatorLogic.equals("<=")) {
            if (ncpaid <= credcan) {
              String response = objParallelC.getSegment();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objParallelC.getMessage(), idStr,
                    matrizStr);
                persona.put("haserror", matrizStr);
              }
              return response;
            }
          } else if (validatorLogic.equals(">=")) {
            if (ncpaid >= credcan) {
              String response = objParallelC.getSegment();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objParallelC.getMessage(), idStr,
                    matrizStr);
                persona.put("haserror", matrizStr);
              }
              return response;
            }
          } else if (validatorLogic.equals("<")) {
            if (ncpaid < credcan) {
              String response = objParallelC.getSegment();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objParallelC.getMessage(), idStr,
                    matrizStr);
                persona.put("haserror", matrizStr);
              }
              return response;
            }
          } else if (validatorLogic.equals(">")) {
            if (ncpaid > credcan) {
              String response = objParallelC.getSegment();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objParallelC.getMessage(), idStr,
                    matrizStr);
                persona.put("haserror", matrizStr);
              }
              return response;
            }
          } else if (validatorLogic.equals("=")) {
            if (ncpaid.equals(credcan)) {
              String response = objParallelC.getSegment();
              if (response.equals("R")) {
                SHPPWS_Helper_Model.putSectionRejected(persona, objParallelC.getMessage(), idStr,
                    matrizStr);
                persona.put("haserror", matrizStr);
              }
              return response;
            }
          }
        }
      }
    }

    // DEFAULT
    for (shppee_RiskIndex deafultObj : queryRiskIndex.list()) {
      if (deafultObj.isShppwsDefaultField()) {
        String response = deafultObj.getSegment();
        if (response.equals("R")) {
          SHPPWS_Helper_Model.putSectionRejected(persona, deafultObj.getShppwsDefaultMessage(),
              idStr, matrizStr);
          persona.put("haserror", matrizStr);
        }
        return response;
      }
    }
    // if (persona.get("Mensaje_Operacional").equals("Error equifax")) {
    // return isInclusion ? codeFlt : SHPPWS_Helper_Model.getDefaultRiskIndiexEqError();
    // } else {
    String msgStr = "NO EXISTEN DATOS EN LA MATRIZ DE INDICE DE RIESGO";
    SHPPWS_Helper_Model.putSectionRejected(persona, msgStr, idStr, matrizStr);
    persona.put("haserror", matrizStr);
    return "R";
    // }
  }

  //
  // Filtro Equifax
  //
  public String procesarApiEQ(BusinessPartner partner, shppws_config accesApi, String ID,
      Map<String, Object> persona, JSONObject jsonMonitor) throws JSONException {
    Double CV = 0.0;
    Double DJ = 0.0;
    Double CC = 0.0;
    Double inclusion = Double.valueOf(0);
    persona.put("CV", CV);
    persona.put("CC", CC);
    persona.put("DJ", DJ);
    persona.put("EQ_segmentacion", "");
    persona.put("EQ_score_inclusion", inclusion);
    String responseEquifaxMessage = "";
    persona.put("Mensaje_Operacional", "");
    // Se verifica existencia de equifax para el nuevo usuario
    persona.put("ApiResponse", "C");
    try {

      SweqxEquifax ltsEquifax = SHPPWS_Helper_Model.lastRequestEquifax(partner.getId());

      if (ltsEquifax != null) {
        String ltsDateEquifax = ltsEquifax != null
            ? SHPPWS_Helper.getDateStringFormat(ltsEquifax.getCreationDate(), "MM/YYYY")
            : "";
        String currentDate = SHPPWS_Helper.getDateStringFormat(new Date(), "MM/YYYY");
        if (ltsDateEquifax.equals(currentDate)) { // VIGENCIA DE CONSULTA
          EQoldSearch(ltsEquifax, persona); // solo obtener CC, CV, DJ
        } else { // Ya es vieja la consulta, se crea un nuevo Equifax
          String apiResponse = getApiResponse(accesApi, ID, 7, jsonMonitor,
              (String) persona.get("identifierLog"));
          if (apiResponse.equals("Error equifax")) {
            persona.put("Exception_Institucion", "False");
            persona.put("Mensaje_Operacional", apiResponse);
            responseEquifaxMessage = apiResponse;
            statusEquifax = false;
          } else {
            EQnewSearch(accesApi, apiResponse, partner, persona);
            persona.get("EQ_segmentacion");
            statusEquifax = true;
          }

        }
      } else { // se crea un nuevo Euifax directamente, al saber que la lista esta vacia
        String apiResponse = getApiResponse(accesApi, ID, 7, jsonMonitor,
            (String) persona.get("identifierLog"));
        if (apiResponse.equals("Error equifax")) {
          persona.put("Exception_Institucion", "False");
          persona.put("Mensaje_Operacional", apiResponse);
          responseEquifaxMessage = apiResponse;
        } else {
          EQnewSearch(accesApi, apiResponse, partner, persona);
        }
      }

    } catch (Exception e) {
      SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID, "Servicio Equifax");
      if (persona.containsKey("haserror") && persona.get("haserror").equals("NA")) {
        persona.put("haserror", "Servicio Equifax");
      }
      responseEquifaxMessage = e.getMessage() + "";
      responseEquifaxMessage = (responseEquifaxMessage == null || responseEquifaxMessage.equals(""))
          ? "No message"
          : responseEquifaxMessage;
    }
    return responseEquifaxMessage;
  }

  public void EQnewSearch(shppws_config accesApi, String apiResponse, BusinessPartner partner,
      Map<String, Object> persona) throws JSONException {
    // Converts the JSON string to a JSONObject
    SweqxEquifax equifax = OBProvider.getInstance().get(SweqxEquifax.class);
    String equifaxValid = "Equifax";
    Boolean pass = false;
    String Segmento_Final = "";
    equifax.setClient(accesApi.getClient());
    equifax.setOrganization(accesApi.getOrganization());
    equifax.setActive(accesApi.isActive());
    equifax.setCreatedBy(accesApi.getCreatedBy());
    equifax.setUpdatedBy(accesApi.getUpdatedBy());

    equifax.setBusinessPartner(partner);
    equifax.setProfile(equifaxValid);
    Long auxEvaluation = Long.valueOf(0L);
    equifax.setEvaluation(auxEvaluation);
    String AmounttoFinance = (String) persona.get("Amount");
    BigDecimal auxfinancedValue = new BigDecimal(AmounttoFinance);
    equifax.setFinancedValue(auxfinancedValue);
    equifax.setProductType("Crédito");
    equifax.setSegmentation("C");
    equifax.setLinkJson(accesApi.getEQNamespace() + "" + accesApi.getEQReadEndpoint());
    equifax.setLinkXml(accesApi.getEQNamespace() + "" + accesApi.getEQReadEndpoint());

    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String formattedJson = gson.toJson(gson.fromJson(apiResponse, Object.class));
      // if (formattedJson.length() > 4000) {formattedJson = formattedJson.substring(0, 4000);}
      equifax.setShppwsResultApi(formattedJson);
      JsonObject jsonResponseEquifax = gson.fromJson(apiResponse, JsonObject.class);
      JsonObject interconnect = jsonResponseEquifax.getAsJsonObject("interconnectResponse");
      BigDecimal capacidadDePago = BigDecimal.ZERO;
      BigDecimal gasto_financiero = BigDecimal.ZERO;
      BigDecimal incomepredictor = BigDecimal.ZERO;
      BigDecimal score_v4 = BigDecimal.ZERO;
      if (interconnect.has("resultado")) {
        JsonArray resultadoArray = interconnect.getAsJsonArray("resultado");

        // Iterar sobre el array "RESULTADO"
        for (JsonElement element : resultadoArray) {
          JsonObject resultado = element.getAsJsonObject();
          String variable = resultado.get("variable").getAsString().trim();
          String resultadoValor = resultado.get("resultado").getAsString();

          // Identificar las variables específicas y asignarlas
          if (variable.equals("CAPACIDAD DE PAGO :")) {
            capacidadDePago = new BigDecimal(resultadoValor);
          } else if (variable.equals("GASTO FINANCIERO :")) {
            gasto_financiero = new BigDecimal(resultadoValor);
          } else if (variable.equals("INCOME PREDICTOR :")) {
            incomepredictor = new BigDecimal(resultadoValor);
          } else if (variable.equals("SCORE V4 :")) {
            score_v4 = new BigDecimal(resultadoValor);
          }
        }
      }

      equifax.setShppetLoadcapacity(capacidadDePago);
      equifax.setShppetFinancialExpense(gasto_financiero);
      equifax.setShppetIncomePredictor(incomepredictor);
      equifax.setShppetScoreV4(score_v4);
    } catch (Exception e) {
    }

    OBDal.getInstance().save(equifax);
    OBDal.getInstance().flush();

    // MAIN Equifax
    Double CV = Double.valueOf(0); // Total Vencido
    Double DJ = Double.valueOf(0); // Dem. Jud.
    Double CC = Double.valueOf(0); // Cart. Cast.
    Double TV = Double.valueOf(0); // Total Vencer
    Double NDI = Double.valueOf(0); // NDI
    Double SD = Double.valueOf(0); // Saldo Deuda

    /// DetalleDeudaActualReportadaSICOM360///
    try {
      JSONObject jsonResponse = new JSONObject(apiResponse);
      JSONArray detalleDeudaArray = null;
      if (jsonResponse.has("reporteCrediticio")) {
        JSONObject reporteCrediticio = jsonResponse.getJSONObject("reporteCrediticio");

        if (reporteCrediticio.has("detalle_deuda_actual_sicom")) {
          detalleDeudaArray = reporteCrediticio.getJSONArray("detalle_deuda_actual_sicom");
          if (detalleDeudaArray != null && detalleDeudaArray.length() > 0) {
            if (detalleDeudaArray.getJSONObject(0).has("institucion")) {
              for (int i = 0; i < detalleDeudaArray.length(); i++) {
                JSONObject detalleDeuda = detalleDeudaArray.getJSONObject(i);
                CV += detalleDeuda.getDouble("total_vencido");
                DJ += detalleDeuda.getDouble("dem_jud");
                CC += detalleDeuda.getDouble("cart_cast");
                TV += detalleDeuda.getDouble("total_vencer");
                NDI += detalleDeuda.getDouble("ndi");
                SD += detalleDeuda.getDouble("saldo_deuda");

                try {// lines detail
                  OBCriteria<InstexceptionEqfx> queryApi_Instexception_eqfx = OBDal.getInstance()
                      .createCriteria(InstexceptionEqfx.class);
                  queryApi_Instexception_eqfx
                      .add(Restrictions.eq(InstexceptionEqfx.PROPERTY_CREDITOR,
                          (String) detalleDeuda.get("institucion")));
                  queryApi_Instexception_eqfx
                      .add(Restrictions.eq(InstexceptionEqfx.PROPERTY_ACTIVE, true));
                  queryApi_Instexception_eqfx.setMaxResults(1);
                  List<InstexceptionEqfx> listOp = queryApi_Instexception_eqfx.list();
                  if (listOp.size() > 0) {
                    // valores de ventana
                    InstexceptionEqfx Instexception_eqfx = queryApi_Instexception_eqfx.list()
                        .get(0);
                    BigDecimal Rango_Inf_Valor_Vencido = Instexception_eqfx.getLowerRankExpdvalue();
                    BigDecimal Rango_Sup_Valor_Vencido = Instexception_eqfx.getUpperRankExpdvalue();
                    BigDecimal Rango_Inf_Cartera_Castigada = Instexception_eqfx
                        .getLowerRankPenportfolio();
                    BigDecimal Rango_Sup_Cartera_Castigada = Instexception_eqfx
                        .getUpperRankPenportfolio();
                    BigDecimal Rango_Inf_Demanda_Judicial = Instexception_eqfx
                        .getLowerRankJudClaim();
                    BigDecimal Rango_Sup_Demanda_Judicial = Instexception_eqfx
                        .getUpperRankJudClaim();
                    Segmento_Final = Instexception_eqfx.getFinalSegment();
                    // valores de api
                    Double total_vencido = detalleDeuda.getDouble("total_vencido");
                    Double cartera_castigada = detalleDeuda.getDouble("cart_cast");
                    Double demanda_judicial = detalleDeuda.getDouble("dem_jud");
                    if (new BigDecimal(total_vencido).compareTo(Rango_Inf_Valor_Vencido) >= 0
                        && new BigDecimal(total_vencido).compareTo(Rango_Sup_Valor_Vencido) <= 0
                        && new BigDecimal(cartera_castigada)
                            .compareTo(Rango_Inf_Cartera_Castigada) >= 0
                        && new BigDecimal(cartera_castigada)
                            .compareTo(Rango_Sup_Cartera_Castigada) <= 0
                        && new BigDecimal(demanda_judicial)
                            .compareTo(Rango_Inf_Demanda_Judicial) >= 0
                        && new BigDecimal(demanda_judicial)
                            .compareTo(Rango_Sup_Demanda_Judicial) <= 0) {
                      persona.put("Segment", Segmento_Final);
                      persona.put("Exception_Institucion", "true");
                      pass = true;

                    } else {
                      persona.put("Exception_Institucion", "false");
                    }
                    if (pass = true) {
                      shppws_detailDue detailDue = OBProvider.getInstance()
                          .get(shppws_detailDue.class);
                      detailDue.setClient(accesApi.getClient());
                      detailDue.setOrganization(accesApi.getOrganization());
                      detailDue.setActive(accesApi.isActive());
                      detailDue.setCreatedBy(accesApi.getCreatedBy());
                      detailDue.setUpdatedBy(accesApi.getUpdatedBy());
                      detailDue.setSweqxEquifax(equifax);

                      try {
                        detailDue.setInstitution((String) detalleDeuda.get("institucion"));
                      } catch (Exception e) {
                      }
                      try {
                        detailDue.setCutoffDate((String) detalleDeuda.get("fecha_corte"));
                      } catch (Exception e) {
                      }
                      try {
                        detailDue.setRiskType((String) detalleDeuda.get("tipo_riesgo"));
                      } catch (Exception e) {
                      }
                      try {
                        detailDue.setCreditType((String) detalleDeuda.get("tipo_credito"));
                      } catch (Exception e) {
                      }
                      try {
                        Double cupoMonto = detalleDeuda.getDouble("cupo_monto_original");
                        detailDue.setQuotaAmount(String.valueOf(cupoMonto));
                      } catch (Exception e) {
                      }
                      try {
                        detailDue.setOpeningDate((String) detalleDeuda.get("fecha_apertura"));
                      } catch (Exception e) {
                      }
                      try {
                        detailDue.setDueDate((String) detalleDeuda.get("fecha_vencimiento"));
                      } catch (Exception e) {
                      }
                      try {
                        detailDue.setOwnrating((String) detalleDeuda.get("Calif. Propia"));
                      } catch (Exception e) {
                      } // No se usa en la nueva estructura de equifax, no afecta funcionalidad
                      try {
                        Double totalVencer = detalleDeuda.getDouble("total_vencer");
                        detailDue.setTotalBeat(String.valueOf(totalVencer));
                      } catch (Exception e) {
                      }
                      try {
                        Double NDI2 = detalleDeuda.getDouble("ndi");
                        detailDue.setNDI(String.valueOf(NDI2));
                      } catch (Exception e) {
                      }
                      try {
                        Double totalVencido = detalleDeuda.getDouble("total_vencido");
                        detailDue.setTotalOverdue(String.valueOf(totalVencido));
                      } catch (Exception e) {
                      }
                      try {
                        Double demJud = detalleDeuda.getDouble("dem_jud");
                        detailDue.setDemJud(String.valueOf(demJud));
                      } catch (Exception e) {
                      }
                      try {
                        Double cartCast = detalleDeuda.getDouble("cart_cast");
                        detailDue.setCartCast(String.valueOf(cartCast));
                      } catch (Exception e) {
                      }
                      try {
                        Double saldoDeuda = detalleDeuda.getDouble("saldo_deuda");
                        detailDue.setDebtBalance(String.valueOf(saldoDeuda));
                      } catch (Exception e) {
                      }
                      try {
                        Double cuotaMensual = detalleDeuda.getDouble("cuota_mensual");
                        detailDue.setMonthlyFee(String.valueOf(cuotaMensual));
                      } catch (Exception e) {
                      }
                      detailDue.setMaxDaysDue("maxDays_due");

                      OBDal.getInstance().save(detailDue);
                      OBDal.getInstance().flush();
                    } else {
                      persona.put("Exception_Institucion", "false");
                    }
                  } else {
                    shppws_detailDue detailDue = OBProvider.getInstance()
                        .get(shppws_detailDue.class);
                    detailDue.setClient(accesApi.getClient());
                    detailDue.setOrganization(accesApi.getOrganization());
                    detailDue.setActive(accesApi.isActive());
                    detailDue.setCreatedBy(accesApi.getCreatedBy());
                    detailDue.setUpdatedBy(accesApi.getUpdatedBy());
                    detailDue.setSweqxEquifax(equifax);

                    try {
                      detailDue.setInstitution((String) detalleDeuda.get("institucion"));
                    } catch (Exception e) {
                    }
                    try {
                      detailDue.setCutoffDate((String) detalleDeuda.get("fecha_corte"));
                    } catch (Exception e) {
                    }
                    try {
                      detailDue.setRiskType((String) detalleDeuda.get("tipo_riesgo"));
                    } catch (Exception e) {
                    }
                    try {
                      detailDue.setCreditType((String) detalleDeuda.get("tipo_credito"));
                    } catch (Exception e) {
                    }
                    try {
                      Double cupoMonto = detalleDeuda.getDouble("cupo_monto_original");
                      detailDue.setQuotaAmount(String.valueOf(cupoMonto));
                    } catch (Exception e) {
                    }
                    try {
                      detailDue.setOpeningDate((String) detalleDeuda.get("fecha_apertura"));
                    } catch (Exception e) {
                    }
                    try {
                      detailDue.setDueDate((String) detalleDeuda.get("fecha_vencimiento"));
                    } catch (Exception e) {
                    }
                    try {
                      detailDue.setOwnrating((String) detalleDeuda.get("Calif. Propia"));
                    } catch (Exception e) {
                    } // No se usa en la nueva estructura de equifax, no afecta funcionalidad
                    try {
                      Double totalVencer = detalleDeuda.getDouble("total_vencer");
                      detailDue.setTotalBeat(String.valueOf(totalVencer));
                    } catch (Exception e) {
                    }
                    try {
                      Double NDI2 = detalleDeuda.getDouble("ndi");
                      detailDue.setNDI(String.valueOf(NDI2));
                    } catch (Exception e) {
                    }
                    try {
                      Double totalVencido = detalleDeuda.getDouble("total_vencido");
                      detailDue.setTotalOverdue(String.valueOf(totalVencido));
                    } catch (Exception e) {
                    }
                    try {
                      Double demJud = detalleDeuda.getDouble("dem_jud");
                      detailDue.setDemJud(String.valueOf(demJud));
                    } catch (Exception e) {
                    }
                    try {
                      Double cartCast = detalleDeuda.getDouble("cart_cast");
                      detailDue.setCartCast(String.valueOf(cartCast));
                    } catch (Exception e) {
                    }
                    try {
                      Double saldoDeuda = detalleDeuda.getDouble("saldo_deuda");
                      detailDue.setDebtBalance(String.valueOf(saldoDeuda));
                    } catch (Exception e) {
                    }
                    try {
                      Double cuotaMensual = detalleDeuda.getDouble("cuota_mensual");
                      detailDue.setMonthlyFee(String.valueOf(cuotaMensual));
                    } catch (Exception e) {
                    }
                    detailDue.setMaxDaysDue("maxDays_due");

                    OBDal.getInstance().save(detailDue);
                    OBDal.getInstance().flush();
                    persona.put("Exception_Institucion", "false");
                  }

                } catch (Exception e) {
                  e.getMessage();
                }
              }
            } else {
              persona.put("Exception_Institucion", "false");
            }
          } else {
            persona.put("Exception_Institucion", "false");
          }

        } else {
          jsonResponse = new JSONObject(apiResponse);
          persona.put("Exception_Institucion", "false");
          detalleDeudaArray = reporteCrediticio.getJSONArray("detalle_deuda_actual_sicom");
          for (int i = 0; i < detalleDeudaArray.length(); i++) {
            JSONObject detalleDeuda = detalleDeudaArray.getJSONObject(i);
            CV += detalleDeuda.getDouble("total_vencido");
            DJ += detalleDeuda.getDouble("dem_jud");
            CC += detalleDeuda.getDouble("cart_cast");
            TV += detalleDeuda.getDouble("total_vencer");
            NDI += detalleDeuda.getDouble("ndi");
            SD += detalleDeuda.getDouble("saldo_deuda");

            try {// lines detail
              shppws_detailDue detailDue = OBProvider.getInstance().get(shppws_detailDue.class);
              detailDue.setClient(accesApi.getClient());
              detailDue.setOrganization(accesApi.getOrganization());
              detailDue.setActive(accesApi.isActive());
              detailDue.setCreatedBy(accesApi.getCreatedBy());
              detailDue.setUpdatedBy(accesApi.getUpdatedBy());
              detailDue.setSweqxEquifax(equifax);

              try {
                detailDue.setInstitution((String) detalleDeuda.get("institucion"));
              } catch (Exception e) {
              }
              try {
                detailDue.setCutoffDate((String) detalleDeuda.get("fecha_corte"));
              } catch (Exception e) {
              }
              try {
                detailDue.setRiskType((String) detalleDeuda.get("tipo_riesgo"));
              } catch (Exception e) {
              }
              try {
                detailDue.setCreditType((String) detalleDeuda.get("tipo_credito"));
              } catch (Exception e) {
              }
              try {
                Double cupoMonto = detalleDeuda.getDouble("cupo_monto_original");
                detailDue.setQuotaAmount(String.valueOf(cupoMonto));
              } catch (Exception e) {
              }
              try {
                detailDue.setOpeningDate((String) detalleDeuda.get("fecha_apertura"));
              } catch (Exception e) {
              }
              try {
                detailDue.setDueDate((String) detalleDeuda.get("fecha_vencimiento"));
              } catch (Exception e) {
              }
              try {
                detailDue.setOwnrating((String) detalleDeuda.get("Calif. Propia"));
              } catch (Exception e) {
              } // No se usa en la nueva estructura de equifax, no afecta funcionalidad
              try {
                Double totalVencer = detalleDeuda.getDouble("total_vencer");
                detailDue.setTotalBeat(String.valueOf(totalVencer));
              } catch (Exception e) {
              }
              try {
                Double NDI2 = detalleDeuda.getDouble("ndi");
                detailDue.setNDI(String.valueOf(NDI2));
              } catch (Exception e) {
              }
              try {
                Double totalVencido = detalleDeuda.getDouble("total_vencido");
                detailDue.setTotalOverdue(String.valueOf(totalVencido));
              } catch (Exception e) {
              }
              try {
                Double demJud = detalleDeuda.getDouble("dem_jud");
                detailDue.setDemJud(String.valueOf(demJud));
              } catch (Exception e) {
              }
              try {
                Double cartCast = detalleDeuda.getDouble("cart_cast");
                detailDue.setCartCast(String.valueOf(cartCast));
              } catch (Exception e) {
              }
              try {
                Double saldoDeuda = detalleDeuda.getDouble("saldo_deuda");
                detailDue.setDebtBalance(String.valueOf(saldoDeuda));
              } catch (Exception e) {
              }
              try {
                Double cuotaMensual = detalleDeuda.getDouble("cuota_mensual");
                detailDue.setMonthlyFee(String.valueOf(cuotaMensual));
              } catch (Exception e) {
              }
              detailDue.setMaxDaysDue("maxDays_due");

              OBDal.getInstance().save(detailDue);
              OBDal.getInstance().flush();
            } catch (Exception e) {
              e.getMessage();
            }
          }
        }
      } else {
        persona.put("Exception_Institucion", "false");
      }
    } catch (Exception e) {
    }

    /// DetalleDeudaActualReportadaSBS360///
    try {
      JSONObject jsonResponse = new JSONObject(apiResponse);
      if (jsonResponse.has("reporteCrediticio")) {
        JSONObject reporteCrediticio = jsonResponse.getJSONObject("reporteCrediticio");
        JSONArray detalleDeudaArray = reporteCrediticio.getJSONArray("detalle_deuda_actual_sb");
        for (int i = 0; i < detalleDeudaArray.length(); i++) {
          JSONObject detalleDeuda = detalleDeudaArray.getJSONObject(i);
          CV += detalleDeuda.getDouble("total_vencido");
          DJ += detalleDeuda.getDouble("dem_jud");
          CC += detalleDeuda.getDouble("cart_cast");
          TV += detalleDeuda.getDouble("total_vencer");
          NDI += detalleDeuda.getDouble("ndi");
          SD += detalleDeuda.getDouble("saldo_deuda");

          try {// lines detail
            shppws_detailDue detailDue = OBProvider.getInstance().get(shppws_detailDue.class);
            detailDue.setClient(accesApi.getClient());
            detailDue.setOrganization(accesApi.getOrganization());
            detailDue.setActive(accesApi.isActive());
            detailDue.setCreatedBy(accesApi.getCreatedBy());
            detailDue.setUpdatedBy(accesApi.getUpdatedBy());
            detailDue.setSweqxEquifax(equifax);

            try {
              detailDue.setInstitution((String) detalleDeuda.get("institucion"));
            } catch (Exception e) {
            }
            try {
              detailDue.setCutoffDate((String) detalleDeuda.get("fecha_corte"));
            } catch (Exception e) {
            }
            try {
              detailDue.setRiskType((String) detalleDeuda.get("tipo_riesgo"));
            } catch (Exception e) {
            }
            try {
              detailDue.setCreditType((String) detalleDeuda.get("tipo_credito"));
            } catch (Exception e) {
            }
            try {
              Double cupoMonto = detalleDeuda.getDouble("cupo_monto_original");
              detailDue.setQuotaAmount(String.valueOf(cupoMonto));
            } catch (Exception e) {
            }
            try {
              detailDue.setOpeningDate((String) detalleDeuda.get("fecha_apertura"));
            } catch (Exception e) {
            }
            try {
              detailDue.setDueDate((String) detalleDeuda.get("fecha_vencimiento"));
            } catch (Exception e) {
            }
            try {
              detailDue.setOwnrating((String) detalleDeuda.get("Calif. Propia"));
            } catch (Exception e) {
            }
            try {
              Double totalVencer = detalleDeuda.getDouble("total_vencer");
              detailDue.setTotalBeat(String.valueOf(totalVencer));
            } catch (Exception e) {
            }
            try {
              Double NDI2 = detalleDeuda.getDouble("ndi");
              detailDue.setNDI(String.valueOf(NDI2));
            } catch (Exception e) {
            }
            try {
              Double totalVencido = detalleDeuda.getDouble("total_vencido");
              detailDue.setTotalOverdue(String.valueOf(totalVencido));
            } catch (Exception e) {
            }
            try {
              Double demJud = detalleDeuda.getDouble("dem_jud");
              detailDue.setDemJud(String.valueOf(demJud));
            } catch (Exception e) {
            }
            try {
              Double cartCast = detalleDeuda.getDouble("cart_cast");
              detailDue.setCartCast(String.valueOf(cartCast));
            } catch (Exception e) {
            }
            try {
              Double saldoDeuda = detalleDeuda.getDouble("saldo_deuda");
              detailDue.setDebtBalance(String.valueOf(saldoDeuda));
            } catch (Exception e) {
            }
            try {
              Double cuotaMensual = detalleDeuda.getDouble("cuota_mensual");
              detailDue.setMonthlyFee(String.valueOf(cuotaMensual));
            } catch (Exception e) {
            }
            detailDue.setMaxDaysDue("maxDays_due");

            OBDal.getInstance().save(detailDue);
            OBDal.getInstance().flush();
          } catch (Exception e) {
            e.getMessage();
          }
        }
      } else {
        persona.put("Exception_Institucion", "false");
      }
    } catch (Exception e) {
    }

    /// DetalleDeudaActualReportadaRFR360///
    try {
      JSONObject jsonResponse = new JSONObject(apiResponse);

      if (jsonResponse.has("reporteCrediticio")) {
        JSONObject reporteCrediticio = jsonResponse.getJSONObject("reporteCrediticio");

        JSONArray detalleDeudaArray = reporteCrediticio.getJSONArray("detalle_deuda_actual_seps");
        for (int i = 0; i < detalleDeudaArray.length(); i++) {
          JSONObject detalleDeuda = detalleDeudaArray.getJSONObject(i);
          CV += detalleDeuda.getDouble("total_vencido");
          DJ += detalleDeuda.getDouble("dem_jud");
          CC += detalleDeuda.getDouble("cart_cast");
          TV += detalleDeuda.getDouble("total_vencer");
          NDI += detalleDeuda.getDouble("ndi");
          SD += detalleDeuda.getDouble("saldo_deuda");

          try {// lines detail
            shppws_detailDue detailDue = OBProvider.getInstance().get(shppws_detailDue.class);
            detailDue.setClient(accesApi.getClient());
            detailDue.setOrganization(accesApi.getOrganization());
            detailDue.setActive(accesApi.isActive());
            detailDue.setCreatedBy(accesApi.getCreatedBy());
            detailDue.setUpdatedBy(accesApi.getUpdatedBy());
            detailDue.setSweqxEquifax(equifax);

            try {
              detailDue.setInstitution((String) detalleDeuda.get("institucion"));
            } catch (Exception e) {
            }
            try {
              detailDue.setCutoffDate((String) detalleDeuda.get("fecha_corte"));
            } catch (Exception e) {
            }
            try {
              detailDue.setRiskType((String) detalleDeuda.get("tipo_riesgo"));
            } catch (Exception e) {
            }
            try {
              detailDue.setCreditType((String) detalleDeuda.get("tipo_credito"));
            } catch (Exception e) {
            }
            try {
              Double cupoMonto = detalleDeuda.getDouble("cupo_monto_original");
              detailDue.setQuotaAmount(String.valueOf(cupoMonto));
            } catch (Exception e) {
            }
            try {
              detailDue.setOpeningDate((String) detalleDeuda.get("fecha_apertura"));
            } catch (Exception e) {
            }
            try {
              detailDue.setDueDate((String) detalleDeuda.get("fecha_vencimiento"));
            } catch (Exception e) {
            }
            try {
              detailDue.setOwnrating((String) detalleDeuda.get("Calif. Propia"));
            } catch (Exception e) {
            }
            try {
              Double totalVencer = detalleDeuda.getDouble("total_vencer");
              detailDue.setTotalBeat(String.valueOf(totalVencer));
            } catch (Exception e) {
            }
            try {
              Double NDI2 = detalleDeuda.getDouble("ndi");
              detailDue.setNDI(String.valueOf(NDI2));
            } catch (Exception e) {
            }
            try {
              Double totalVencido = detalleDeuda.getDouble("total_vencido");
              detailDue.setTotalOverdue(String.valueOf(totalVencido));
            } catch (Exception e) {
            }
            try {
              Double demJud = detalleDeuda.getDouble("dem_jud");
              detailDue.setDemJud(String.valueOf(demJud));
            } catch (Exception e) {
            }
            try {
              Double cartCast = detalleDeuda.getDouble("cart_cast");
              detailDue.setCartCast(String.valueOf(cartCast));
            } catch (Exception e) {
            }
            try {
              Double saldoDeuda = detalleDeuda.getDouble("saldo_deuda");
              detailDue.setDebtBalance(String.valueOf(saldoDeuda));
            } catch (Exception e) {
            }
            try {
              Double cuotaMensual = detalleDeuda.getDouble("cuota_mensual");
              detailDue.setMonthlyFee(String.valueOf(cuotaMensual));
            } catch (Exception e) {
            }
            detailDue.setMaxDaysDue("maxDays_due");

            OBDal.getInstance().save(detailDue);
            OBDal.getInstance().flush();
          } catch (Exception e) {
            e.getMessage();
          }
        }
      } else {
        persona.put("Exception_Institucion", "false");
      }
    } catch (Exception e) {
    }

    // Save results
    try {
      equifax.setShppetTotalBeat(new BigDecimal(TV));// total_vencer
      equifax.setShppetNdi(new BigDecimal(NDI));// NDI
      equifax.setShppetTotalOverdue(new BigDecimal(CV));// total_vencido
      equifax.setShppetDemJud(new BigDecimal(DJ));// dem_jud
      equifax.setShppetCartCast(new BigDecimal(CC));// cart_cast
      equifax.setShppetDebtBalance(new BigDecimal(SD));// saldo_deuda
      OBDal.getInstance().save(equifax);
      OBDal.getInstance().flush();

      persona.put("CV", CV);
      persona.put("CC", CC);
      persona.put("DJ", DJ);
    } catch (Exception e) {

    }

    // GET SEGMENT and SAVE
    try {
      JSONObject jsonResponse = new JSONObject(apiResponse);
      JSONObject interconnect = jsonResponse.getJSONObject("interconnectResponse");
      JSONArray resultadoArray = interconnect.getJSONArray("resultado");
      String segmentacion = null;
      for (int i = 0; i < resultadoArray.length(); i++) {
        JSONObject resultado = resultadoArray.getJSONObject(i);
        String variable = resultado.getString("variable");
        String resultadoValor = resultado.getString("resultado");
        if (pass) {
          // resultadoValor=Segmento_Final;
          if (variable.equals("SEGMENTACION BANCARIZADO:")) {
            segmentacion = (resultadoValor != null && !(resultadoValor.equals(""))) ? resultadoValor
                : "OTRO"; // when Resultado is empty
            break;
          }
        }
        if (!pass) {
          if (variable.equals("SEGMENTACION BANCARIZADO:")) {
            segmentacion = (resultadoValor != null && !(resultadoValor.equals(""))) ? resultadoValor
                : "OTRO"; // when Resultado is empty
            break;
          }

        }
      }

      segmentacion = (segmentacion == null || segmentacion.equals("")) ? "OTRO" : segmentacion; // when
                                                                                                // not
                                                                                                // find
                                                                                                // SEGMENTACION
                                                                                                // :

      equifax.setSegmentation(segmentacion);
      OBDal.getInstance().save(equifax);
      OBDal.getInstance().flush();

      persona.put("EQ_segmentacion", segmentacion);
    } catch (Exception e) {
      equifax.setSegmentation("OTRO");
      OBDal.getInstance().save(equifax);
      OBDal.getInstance().flush();
      persona.put("EQ_segmentacion", "OTRO");
    }

    // GET SCORE_INCLUSION and SAVE
    try {
      JSONObject jsonResponse = new JSONObject(apiResponse);
      JSONObject interconnect = jsonResponse.getJSONObject("interconnectResponse");
      JSONArray resultadoArray = interconnect.getJSONArray("resultado");
      Double inclusion = Double.valueOf(-999999);
      for (int i = 0; i < resultadoArray.length(); i++) {
        JSONObject resultado = resultadoArray.getJSONObject(i);
        String variable = resultado.getString("variable");
        String resultadoValor = resultado.getString("resultado");

        if (variable.equals("SCORE INCLUSION :")) {
          if (resultadoValor.equals("SIN SCORE")) {
            inclusion = Double.valueOf(0);
            break;
          } else {
            inclusion = Double.parseDouble(resultadoValor);
            break;
          }
        }
      }

      inclusion = (inclusion >= 0) ? inclusion : Double.valueOf(999999);

      Long auxEvaluation1 = inclusion.longValue();
      equifax.setEvaluation(auxEvaluation1);
      OBDal.getInstance().save(equifax);
      OBDal.getInstance().flush();

      persona.put("EQ_score_inclusion", inclusion);
    } catch (Exception e) {
      Double inclusion = Double.valueOf(999999);
      Long auxEvaluation1 = inclusion.longValue();
      equifax.setEvaluation(auxEvaluation1);
      OBDal.getInstance().save(equifax);
      OBDal.getInstance().flush();
      persona.put("EQ_score_inclusion", inclusion);
    }

    // Guardar informacion demografica
    try {
      JSONObject rootObj = new JSONObject(apiResponse);

      if (rootObj.has("reporteCrediticio")) {
        JSONObject reporteCrediticio = rootObj.optJSONObject("reporteCrediticio");
        if (reporteCrediticio != null) {
          JSONArray infoDemo = reporteCrediticio.optJSONArray("informacion_demografica");
          if (infoDemo != null && infoDemo.length() > 0) {
            JSONObject d = infoDemo.optJSONObject(0);
            if (d != null) {
              try {
                equifax.setShppwsEducationLevel(d.getString("educacion"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsProvince(d.getString("provincia"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsCanton(d.getString("canton"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsAddresses(d.getString("direcciones"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsCoordinateX(d.getString("coordenada_x"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsCoordinateY(d.getString("coordenada_y"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsConventionalPhone(d.getString("numero_telefonico_convencional"));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsCellPhone(d.getString("numero_telefonico_celular"));
              } catch (Exception ignore) {
              }
              try {
                String fn = d.getString("fecha_nacimiento");
                if (fn != null && !fn.isEmpty()) {
                  Date parsed = null;
                  try {
                    SimpleDateFormat sdfEs = new SimpleDateFormat("d 'de' MMMM 'de' yyyy",
                        new Locale("es", "ES"));
                    parsed = sdfEs.parse(fn);
                  } catch (Exception e1) {
                    try {
                      SimpleDateFormat sdfAlt = new SimpleDateFormat("dd/MM/yyyy");
                      parsed = sdfAlt.parse(fn);
                    } catch (Exception e2) {
                    }
                  }
                  if (parsed != null) {
                    equifax.setShppwsBirthDate(parsed);
                  }
                }
              } catch (Exception ignore) {
              }
              // Persistir informacion demográfica
              try {
                OBDal.getInstance().save(equifax);
                OBDal.getInstance().flush();
              } catch (Exception ignore) {
              }
            }
          }
        }
      } else {
        persona.put("Exception_Institucion", "false");
      }
    } catch (Exception ignore) {
    }

    // Guardar resultado inclusion
    try {
      JSONObject rootObj = new JSONObject(apiResponse);
      JSONObject interconnect = rootObj.getJSONObject("interconnectResponse");
      if (interconnect != null) {
        JSONArray resultadoInclusion = interconnect.optJSONArray("resultado_inclusion");
        if (resultadoInclusion != null && resultadoInclusion.length() > 0) {
          for (int i = 0; i < resultadoInclusion.length(); i++) {
            JSONObject rsi = resultadoInclusion.optJSONObject(i);
            if (rsi != null && "SCORE INCLUSION".equalsIgnoreCase(rsi.optString("politica"))) {
              // Solo si la política es SCORE INCLUSION
              try {
                equifax.setShppwsInclusionValue(new BigDecimal(rsi.optString("valor", "0")));
              } catch (Exception ignore) {
              }
              try {
                equifax.setShppwsInclusionDecision(rsi.optString("decision", null));
              } catch (Exception ignore) {
              }
              // Seccion de datos de inclusion
              JSONObject jsonInclusion = new JSONObject();
              jsonInclusion.put("decision", equifax.getShppwsInclusionDecision());
              jsonInclusion.put("value", equifax.getShppwsInclusionValue());
              persona.put("inclusionData", jsonInclusion);

              // Persistir información score
              try {
                OBDal.getInstance().save(equifax);
                OBDal.getInstance().flush();
              } catch (Exception ignore) {
              }

              break;
            }
          }
        }
      }
    } catch (Exception ignore) {
    }

  }

  public void EQoldSearch(SweqxEquifax registroMasActual, Map<String, Object> persona)
      throws JSONException {
    try {
      String segmentation = registroMasActual.getSegmentation();
      Long inclusion = registroMasActual.getEvaluation();
      Double inclusionDouble = inclusion.doubleValue();
      persona.put("EQ_segmentacion", segmentation);
      persona.put("EQ_score_inclusion", inclusionDouble);

      OBCriteria<shppws_detailDue> querydetail = OBDal.getInstance()
          .createCriteria(shppws_detailDue.class);
      querydetail.add(Restrictions.eq(shppws_detailDue.PROPERTY_SWEQXEQUIFAX, registroMasActual));
      List<shppws_detailDue> duedetails = querydetail.list();
      Double CC = Double.valueOf(0);
      Double CV = Double.valueOf(0);
      Double DJ = Double.valueOf(0);

      for (shppws_detailDue duedetail : duedetails) {
        String StringCC = duedetail.getCartCast();
        String StringCV = duedetail.getTotalOverdue();
        String StringDJ = duedetail.getDemJud();

        CC += Double.parseDouble(StringCC);
        CV += Double.parseDouble(StringCV);
        DJ += Double.parseDouble(StringDJ);
      }
      persona.put("CV", CV);
      persona.put("CC", CC);
      persona.put("DJ", DJ);

      // Seccion de datos de inclusion
      JSONObject jsonInclusion = new JSONObject();
      jsonInclusion.put("decision", registroMasActual.getShppwsInclusionDecision());
      jsonInclusion.put("value", registroMasActual.getShppwsInclusionValue());
      persona.put("inclusionData", jsonInclusion);

      try {
        JSONObject jsonResponse = new JSONObject(registroMasActual.getShppwsResultApi());
        JSONArray detalleDeudaArray = null;
        String Segmento_Final = "";
        Boolean pass = false;
        if (jsonResponse.has("reporteCrediticio")) {
          JSONObject reporteCrediticio = jsonResponse.getJSONObject("reporteCrediticio");
          if (reporteCrediticio.has("detalle_deuda_actual_sicom")) {
            detalleDeudaArray = reporteCrediticio.getJSONArray("detalle_deuda_actual_sicom");
            if (detalleDeudaArray != null && detalleDeudaArray.length() > 0) {
              if (detalleDeudaArray.getJSONObject(0).has("institucion")) {
                for (int i = 0; i < detalleDeudaArray.length(); i++) {
                  JSONObject detalleDeuda = detalleDeudaArray.getJSONObject(i);
                  try {// lines detail
                    OBCriteria<InstexceptionEqfx> queryApi_Instexception_eqfx = OBDal.getInstance()
                        .createCriteria(InstexceptionEqfx.class);
                    queryApi_Instexception_eqfx
                        .add(Restrictions.eq(InstexceptionEqfx.PROPERTY_CREDITOR,
                            (String) detalleDeuda.get("institucion")));
                    queryApi_Instexception_eqfx
                        .add(Restrictions.eq(InstexceptionEqfx.PROPERTY_ACTIVE, true));
                    queryApi_Instexception_eqfx.setMaxResults(1);
                    List<InstexceptionEqfx> listOp = queryApi_Instexception_eqfx.list();
                    if (listOp.size() > 0) {
                      // valores de ventana
                      InstexceptionEqfx Instexception_eqfx = queryApi_Instexception_eqfx.list()
                          .get(0);
                      BigDecimal Rango_Inf_Valor_Vencido = Instexception_eqfx
                          .getLowerRankExpdvalue();
                      BigDecimal Rango_Sup_Valor_Vencido = Instexception_eqfx
                          .getUpperRankExpdvalue();
                      BigDecimal Rango_Inf_Cartera_Castigada = Instexception_eqfx
                          .getLowerRankPenportfolio();
                      BigDecimal Rango_Sup_Cartera_Castigada = Instexception_eqfx
                          .getUpperRankPenportfolio();
                      BigDecimal Rango_Inf_Demanda_Judicial = Instexception_eqfx
                          .getLowerRankJudClaim();
                      BigDecimal Rango_Sup_Demanda_Judicial = Instexception_eqfx
                          .getUpperRankJudClaim();
                      Segmento_Final = Instexception_eqfx.getFinalSegment();
                      // valores de api
                      Double total_vencido = detalleDeuda.getDouble("total_vencido");
                      Double cartera_castigada = detalleDeuda.getDouble("cart_cast");
                      Double demanda_judicial = detalleDeuda.getDouble("dem_jud");
                      if (new BigDecimal(total_vencido).compareTo(Rango_Inf_Valor_Vencido) >= 0
                          && new BigDecimal(total_vencido).compareTo(Rango_Sup_Valor_Vencido) <= 0
                          && new BigDecimal(cartera_castigada)
                              .compareTo(Rango_Inf_Cartera_Castigada) >= 0
                          && new BigDecimal(cartera_castigada)
                              .compareTo(Rango_Sup_Cartera_Castigada) <= 0
                          && new BigDecimal(demanda_judicial)
                              .compareTo(Rango_Inf_Demanda_Judicial) >= 0
                          && new BigDecimal(demanda_judicial)
                              .compareTo(Rango_Sup_Demanda_Judicial) <= 0) {
                        persona.put("Segment", Segmento_Final);
                        persona.put("Exception_Institucion", "true");
                        pass = true;

                      } else {
                        persona.put("Exception_Institucion", "false");
                      }
                    } else {
                      persona.put("Exception_Institucion", "false");
                    }
                  } catch (Exception e) {
                    e.getMessage();
                  }
                }
              }
            } else {
              persona.put("Exception_Institucion", "false");
            }
          } else {
            persona.put("Exception_Institucion", "false");
          }
        } else {
          persona.put("Exception_Institucion", "false");
        }
      } catch (Exception e) {
      }

    } catch (Exception e) {
    }
  }

  public void validateEquifaxArrays(String ID, String Store_group, Map<String, Object> persona)
      throws JSONException {
    Double CV = (Double) persona.get("CV");
    Double CC = (Double) persona.get("CC");
    Double DJ = (Double) persona.get("DJ");
    // Double auxValuesSummary= CV+CC+DJ;

    ////// VALORES VENCIDOS
    Boolean validateDefaultCV = true;
    OBCriteria<Shppec_ExpActual> matriz1 = OBDal.getInstance()
        .createCriteria(Shppec_ExpActual.class);
    List<Shppec_ExpActual> expiredValues = matriz1.list();
    String segment1ExpiredValues = "-";
    String matrizStr = "Valor Vencido Actual EQFX";

    for (Shppec_ExpActual expiredValue : expiredValues) {
      if (Store_group.equals(expiredValue.getAgenCode())) {

        BigDecimal startValue = expiredValue.getValueFrom();
        BigDecimal endValue = expiredValue.getValueUntil();
        Double valueInitial = startValue.doubleValue();
        Double valueFinal = endValue.doubleValue();

        String answer = expiredValue.getSegment();
        if (CV >= valueInitial && CV <= valueFinal) {
          validateDefaultCV = false;
          segment1ExpiredValues = answer;
          if (answer.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, expiredValue.getMessage(),
                CV.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            break;
          }

        }
        // break;
      }
    }

    // DEFAULT
    if (validateDefaultCV) {
      for (Shppec_ExpActual defaultObj : expiredValues) {
        if (defaultObj.isShppwsDefaultField()) {
          String answer = defaultObj.getSegment();
          segment1ExpiredValues = answer;
          if (answer.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, defaultObj.getShppwsDefaultMessage(),
                CV.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            break;
          }
        }
      }
    }

    ////// VALOR Cart. CASTIGADO
    Boolean validateDefaultCC = true;
    OBCriteria<shppec_portpen> matriz2 = OBDal.getInstance().createCriteria(shppec_portpen.class);
    List<shppec_portpen> cartValues = matriz2.list();
    String segment2cartValues = "-";
    matrizStr = "Valor Cartera Castigada EQFX";

    for (shppec_portpen cartValue : cartValues) {
      if (Store_group.equals(cartValue.getAgenCode())) {

        BigDecimal startValue = cartValue.getValueFrom();
        BigDecimal endValue = cartValue.getValueUntil();
        Double valueInitial = startValue.doubleValue();
        Double valueFinal = endValue.doubleValue();

        String answer = cartValue.getSegment();
        if (CC >= valueInitial && CC <= valueFinal) {
          validateDefaultCC = false;
          segment2cartValues = answer;
          if (answer.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, cartValue.getMessage(), CC.toString(),
                matrizStr);
            persona.put("haserror", matrizStr);
            break;
          }
        }
        // break;
      }
    }

    // DEFAULT
    if (validateDefaultCC) {
      for (shppec_portpen defaultObj : cartValues) {
        if (defaultObj.isShppwsDefaultField()) {
          String answer = defaultObj.getSegment();
          segment2cartValues = answer;
          if (answer.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, defaultObj.getShppwsDefaultMessage(),
                CC.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            break;
          }
        }
      }
    }

    ////// Valor Demanda Judicial
    Boolean validateDefaultDJ = true;
    matrizStr = "Valor Demanda Judicial EQFX";

    OBCriteria<Shppec_Lawsuit> matriz3 = OBDal.getInstance().createCriteria(Shppec_Lawsuit.class);
    List<Shppec_Lawsuit> lawsuitValues = matriz3.list();
    String segment3lawsuitValues = "-";
    for (Shppec_Lawsuit lawsuitValue : lawsuitValues) {
      if (Store_group.equals(lawsuitValue.getAgenCode())) {

        BigDecimal startValue = lawsuitValue.getValueFrom();
        BigDecimal endValue = lawsuitValue.getValueUntil();
        Double valueInitial = startValue.doubleValue();
        Double valueFinal = endValue.doubleValue();

        String answer = lawsuitValue.getSegment();
        if (DJ >= valueInitial && DJ <= valueFinal) {
          validateDefaultDJ = false;
          segment3lawsuitValues = answer;
          if (answer.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, lawsuitValue.getMessage(),
                DJ.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            break;
          }
        }
        // break;
      }
    }

    // DEFAULT
    if (validateDefaultDJ) {
      for (Shppec_Lawsuit defaultObj : lawsuitValues) {
        if (defaultObj.isShppwsDefaultField()) {
          String answer = defaultObj.getSegment();
          segment3lawsuitValues = answer;
          if (answer.equals("R")) {
            SHPPWS_Helper_Model.putSectionRejected(persona, defaultObj.getShppwsDefaultMessage(),
                DJ.toString(), matrizStr);
            persona.put("haserror", matrizStr);
            break;
          }
        }
      }
    }
    persona.put("segment1ExpiredValues", segment1ExpiredValues);
    persona.put("segment2cartValues", segment2cartValues);
    persona.put("segment3lawsuitValues", segment3lawsuitValues);
  }

  public void validateEquifaxScoreNewClientSP(Map<String, Object> persona, String filterEQ,
      shppws_config accesApi, boolean newClient) throws JSONException {

    String newClient_segment = filterEQ;
    Double newClient_scoreInclusion = (Double) persona.get("EQ_score_inclusion");
    JSONObject jsonInclusion = persona.containsKey("inclusionData")
        ? (JSONObject) persona.get("inclusionData")
        : new JSONObject();

    // Validacion Inclusion
    boolean isInclusion = accesApi.isEquifaxInclusion() && newClient;
    String codeFlt = filterEQ;

    BigDecimal AgeBigDecimal = (BigDecimal) persona.get("auxAgeBigDecimal");
    AgeBigDecimal = AgeBigDecimal.setScale(2, RoundingMode.DOWN);
    Double Age = AgeBigDecimal.doubleValue();

    String answer = "";

    String hqlStr = "";
    String caseStr = "";
    String matrizStr = "Score Cliente nuevo";

    // Flujo INclusion
    if (isInclusion && jsonInclusion != null && StringUtils.isNotBlank(codeFlt)) {
      newClient_segment = codeFlt;
      caseStr = "2";
      hqlStr = jsonInclusion.has("decision") ? jsonInclusion.getString("decision") : "NA-";
    }

    // Flujo Edad
    if (!isInclusion) {
      caseStr = "1";
      hqlStr = Age.toString();
    }

    shppee_NewCustomerScore obc = SHPPWS_Helper_Model.getCustomerScore(false, newClient_segment,
        caseStr, hqlStr);

    answer = obc.getENDSegment();
    persona.put("EQ_scoreClient", answer);

    if (answer.equals("R")) {
      SHPPWS_Helper_Model.putSectionRejected(persona, obc.getShppwsDefaultMessage(),
          "scoreInclusion " + newClient_scoreInclusion, matrizStr);
      persona.put("haserror", matrizStr);
    }

  }

  public void validateEquifaxScoreNewClient(Map<String, Object> persona, shppws_config accesApi,
      boolean newClient) throws JSONException {

    String newClient_segment = (String) persona.get("EQ_segmentacion");
    String errorEq = persona.containsKey("msgLN") ? (String) persona.get("msgLN") : "";

    Double newClient_scoreInclusion = (Double) persona.get("EQ_score_inclusion");
    JSONObject jsonInclusion = persona.containsKey("inclusionData")
        ? (JSONObject) persona.get("inclusionData")
        : new JSONObject();

    // Validacion Inclusion
    boolean isInclusion = accesApi.isEquifaxInclusion() && newClient;
    String codeFlt = newClient_segment;
    if (errorEq.equals("Error equifax")) {
      codeFlt = newClient_segment;
    }

    BigDecimal AgeBigDecimal = (BigDecimal) persona.get("auxAgeBigDecimal");
    AgeBigDecimal = AgeBigDecimal == null ? BigDecimal.ZERO : AgeBigDecimal;
    AgeBigDecimal = AgeBigDecimal.setScale(2, RoundingMode.DOWN);
    Double Age = AgeBigDecimal.doubleValue();

    String hqlStr = "";
    String caseStr = "";
    String matrizStr = "Score Cliente nuevo";

    // Flujo INclusion
    if (isInclusion && jsonInclusion != null && StringUtils.isNotBlank(codeFlt)) {
      newClient_segment = codeFlt;
      caseStr = "2";
      hqlStr = jsonInclusion.has("decision") ? jsonInclusion.getString("decision") : "NA-";
    }

    // Flujo Edad
    if (!isInclusion) {
      caseStr = "1";
      hqlStr = Age.toString();
    }

    shppee_NewCustomerScore obc = SHPPWS_Helper_Model.getCustomerScore(false, newClient_segment,
        caseStr, hqlStr);

    String answer = obc.getENDSegment();
    persona.put("EQ_scoreClient", answer);

    if (answer.equals("R")) {
      SHPPWS_Helper_Model.putSectionRejected(persona, obc.getShppwsDefaultMessage(),
          "scoreInclusion " + newClient_scoreInclusion, matrizStr);
      persona.put("haserror", matrizStr);
    }

  }

  public void validateEquifaxQuotas(String filterEQ, Map<String, Object> persona,
      boolean completeFlow, String flt) throws JSONException {

    ////// CUPOS
    OBCriteria<shppee_Quotas> matriz1 = OBDal.getInstance().createCriteria(shppee_Quotas.class);
    List<shppee_Quotas> quotasScores = matriz1.list();
    String codeProduct = (String) persona.get("productcode");
    String aux = "";
    Boolean validateDefault = true;
    String finalfilter = completeFlow ? filterEQ : flt;

    log4j.info("validateEquifaxQuotas: " + finalfilter);
    for (shppee_Quotas quotasScore : quotasScores) {
      scsl_Product objProduct = quotasScore.getScslProduct();
      String product = objProduct.getValidationCode();
      String storeGroup = (String) persona.get("shopgroup");

      if (codeProduct.equals(product) && storeGroup.equals(quotasScore.getAgencyCode())
          && finalfilter.equals(quotasScore.getENDSegment())) {
        validateDefault = false;
        Long finalValue = quotasScore.getQuota();
        Double valueFinal = finalValue.doubleValue();
        persona.put("EQ_quota", valueFinal);
        BigDecimal entrance = quotasScore.getInput();
        Double entranceFinal = entrance.doubleValue();
        persona.put("EQ_entrance", entranceFinal);
        String Type_Entrance = quotasScore.getTypeInput();
        persona.put("Type_Entrance", Type_Entrance);
        Long deadLine = quotasScore.getMaximumTerm();
        Double deadLineFinal = deadLine.doubleValue();
        persona.put("EQ_deadline", deadLineFinal);
        String filter = quotasScore.getENDSegment();
        persona.put("EQ_scoreClient", filter);
        String checkDelivery = String.valueOf(quotasScore.isSalesdelivery());
        persona.put("CheckDelivery", checkDelivery);
      }
    }

    // DEFAULT X
    if (validateDefault) {
      for (shppee_Quotas defaultObj : quotasScores) {
        if (defaultObj.isShppwsDefaultField()) {
          Long finalValue = defaultObj.getQuota();
          Double valueFinal = finalValue.doubleValue();
          persona.put("EQ_quota", valueFinal);
          BigDecimal entrance = defaultObj.getInput();
          Double entranceFinal = entrance.doubleValue();
          persona.put("EQ_entrance", entranceFinal);
          String Type_Entrance = defaultObj.getTypeInput();
          persona.put("Type_Entrance", Type_Entrance);
          Long deadLine = defaultObj.getMaximumTerm();
          Double deadLineFinal = deadLine.doubleValue();
          persona.put("EQ_deadline", deadLineFinal);
          String filter = defaultObj.getENDSegment();
          persona.put("EQ_scoreClient", filter);
          String msg = defaultObj.getShppwsDefaultMessage();
          if (persona.containsKey("haserror") && (persona.get("haserror").equals("NA")
              || persona.get("haserror").equals("Servicio Equifax"))) {
            persona.put("msgLN", msg);
            persona.put("matrizReason", "Matriz de Cupos");
          }

          String checkDelivery = String.valueOf(defaultObj.isSalesdelivery());
          persona.put("CheckDelivery", checkDelivery);

        }
      }
    }

    persona.put("message", "Filtro No 4, Se asigna un cupo al cliente.");
    persona.put("matriz", "Matriz Cupo" + aux);
  }

  public String newOpportunity(String filter, Map<String, Object> persona, shppws_config accesApi,
      boolean statusEquifax, boolean CheckSinergia, boolean statusSinergia) throws JSONException {
    ////// Se crea una nueva instancia de Oportunidad
    Opcrmopportunities objOpportunity = OBProvider.getInstance().get(Opcrmopportunities.class);
    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);

    String newNumber = "";
    String message = ".";
    String Error = "";
    try {
      String partnerID = (String) persona.get("partnerID") != null
          ? (String) persona.get("partnerID")
          : "";
      BusinessPartner objPartner = OBDal.getInstance().get(BusinessPartner.class, partnerID);
      if (objPartner != null) {
        if (persona.get("CheckDelivery").equals("true")) {
          objOpportunity.setShppwsIssalesdelivery(true);
        } else {
          objOpportunity.setShppwsIssalesdelivery(false);
        }
        objOpportunity.setClient(accesApi.getClient());
        objOpportunity.setOrganization(accesApi.getOrganization());
        objOpportunity.setActive(accesApi.isActive());
        objOpportunity.setCreatedBy(accesApi.getCreatedBy());
        objOpportunity.setUpdatedBy(accesApi.getUpdatedBy());
        objOpportunity.setBusinessPartner(objPartner);
        Double amountQuota = (Double) persona.get("EQ_quota");
        BigDecimal quotaFinal = BigDecimal.valueOf(amountQuota).setScale(2, RoundingMode.HALF_UP);// cupo
        objOpportunity.setOpportunityAmount(quotaFinal);
        objOpportunity.setTAXBpartner((String) persona.get("RCcedula"));
        objOpportunity.setEcsfqEmail((String) persona.get("email"));
        objOpportunity.setCellphoneBpartner((String) persona.get("CellPhone"));
        objOpportunity.setEcsfqPhone((String) persona.get("CellPhone"));
        objOpportunity.setShppwsOpInterface((String) persona.get("interface"));
        objOpportunity.setShppwsOpChannel((String) persona.get("channel"));
        OBCriteria<BusinessPartner> queryBusinessPartner = OBDal.getInstance()
            .createCriteria(BusinessPartner.class);
        queryBusinessPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY,
            (String) persona.get("commercialcode")));
        List<BusinessPartner> listObjBusinessPartner = queryBusinessPartner.list();
        BusinessPartner objCommercialcode = queryBusinessPartner.list().isEmpty() ? null
            : listObjBusinessPartner.get(0);
        if (objCommercialcode != null) {
          objOpportunity.setShppwsOpCodecommercial(objCommercialcode);
        }
        objOpportunity.setShppwsOpAgencycode((String) persona.get("agencycode"));
        objOpportunity.setShppwsOpShopgroup((String) persona.get("shopgroup"));
        OBCriteria<scsl_Product> queryprod = OBDal.getInstance().createCriteria(scsl_Product.class);
        queryprod.add(Restrictions.eq(scsl_Product.PROPERTY_VALIDATIONCODE,
            (String) persona.get("productcode")));
        scsl_Product product = (scsl_Product) queryprod.uniqueResult();

        objOpportunity.setShppwsOpProductcode(product);
        objOpportunity.setShppwsOpEndsegment(filter);
        objOpportunity.setEcsfqPaymenttype(accesApi.getOPPaymentType());
        objOpportunity.setOpportunityName(accesApi.getOPName());
        Date currentDate = new Date();
        objOpportunity.setExpectedCloseDate(currentDate);
        if (filter.equals("R")) {
          objOpportunity.setOpportstatus("LOST");//
        } else {
          objOpportunity.setOpportstatus(accesApi.getOPOpportunityStatus());//
        }
        if (!statusEquifax) {
          objOpportunity.setShppwsDefaultOppSeg(true);
        }
        if (CheckSinergia) {
          if (typeClientSynergy != null && !typeClientSynergy.equals("Error en sinergia")) {
            objOpportunity.setShppwsClienttype(typeClientSynergy);
          }
          if (!statusSinergia) {
            objOpportunity.setShpctUnresponseSynergy(true);
          }
        }
        objOpportunity.setOpportunityType(accesApi.getOPOpportunityType());
        objOpportunity.setLeadSource(accesApi.getOPLeadSource());
        objOpportunity.setShppwsOpDocumentType(accesApi.getOPDocumentType());
        String OpportunityNo = (String) persona.get("docnoTransaction");
        objOpportunity.setShppwsOpDocumentno(OpportunityNo);
        String ValCedula = "NOT";
        String ValTel = "NOT";
        String ValEmail = "NOT";
        if (SHPPWS_Helper_Model.validateBlackListcheckCedula()) {
          ValCedula = "YES";
        } else {
          ValCedula = "NOT";
        }
        if (SHPPWS_Helper_Model.validateBlackListchecktelefono()) {
          ValTel = "YES";
        } else {
          ValTel = "NOT";
        }
        if (SHPPWS_Helper_Model.validateBlackListcheckemail()) {
          ValEmail = "YES";
        } else {
          ValEmail = "NOT";
        }
        objOpportunity.setDescription(
            "Cédulas: " + ValCedula + "\nTeléfonos: " + ValTel + "\nCorreos: " + ValEmail);

        String City_store_group = (String) persona.get("City_store_group");
        if (City_store_group != null) {
          objOpportunity.setShppwsCityStoreGroup(City_store_group);
        }
        String Province_store_group = (String) persona.get("Province_store_group");
        if (Province_store_group != null) {
          objOpportunity.setShppwsProvinceStoreGroup(Province_store_group);
        }

        // New fields
        Long npcredits = objPartner.getShpctNoPunishedCredits();
        if (npcredits == null) {
          npcredits = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCrdsWritten(npcredits); // N0 creditos castigados
        Long nccredits = objPartner.getShpctNoCurrentCredits();
        if (nccredits == null) {
          nccredits = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCurCrds(nccredits);// No. Créditos Vigentes
        Long ncpaid = objPartner.getShpctNoCreditsPaid();
        if (ncpaid == null) {
          ncpaid = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCrdsPaid(ncpaid);// No. Créditos Pagados
        Long ncexpired = objPartner.getShpctNoCCreditsExpired();
        if (ncexpired == null) {
          ncexpired = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCurOverCrds(ncexpired);// No. Créditos Vigentes Vencidos
        Long lipaid = objPartner.getShpctLastInstallmentpaid();
        if (lipaid == null) {
          lipaid = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpLastPaidLastcrd(lipaid);// Última cuota pagada del último crédito
        Long mfpaid = objPartner.getShpctMaximumFeePaid();
        if (mfpaid == null) {
          mfpaid = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoMaxFeePaid(mfpaid);// No. Cuota Máxima Pagada
        Long dlinstallments = objPartner.getShpctDayslateNstallments();
        if (dlinstallments == null) {
          dlinstallments = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpDaysArInstall(dlinstallments);// Días de atraso en cuotas
        Long ddsdelay = objPartner.getShpctDaysdueSecondDelay();
        if (ddsdelay == null) {
          ddsdelay = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpDays2ndMaxAr(ddsdelay);// Días mora del 2do atraso máximo
        Long coperations = objPartner.getShpctCurrentOperations();
        if (coperations == null) {
          coperations = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCurOperations(coperations);// No. Operaciones Vigentes
        BigDecimal irisk = objPartner.getShpctRiskIndex();
        if (irisk == null) {
          irisk = BigDecimal.ZERO;
        }
        objOpportunity.setShppwsOpRiskIndex(irisk);// Índice de Riesgo
        BigDecimal qused = objPartner.getShpctQuotaUsed();
        if (qused == null) {
          qused = BigDecimal.ZERO;
        }
        objOpportunity.setShppwsOpQuotaUsed(qused);// Cupo Utilizado

        BigDecimal EQ_entrance = BigDecimal.ZERO;
        try {
          EQ_entrance = BigDecimal.valueOf((Double) (persona.get("EQ_entrance")));
        } catch (Exception e) {
        }
        String Type_Entrance = "";
        try {
          Type_Entrance = (String) persona.get("Type_Entrance");
        } catch (Exception e) {
        }
        objOpportunity.setShppwsEntryAmount(EQ_entrance);
        objOpportunity.setShppwsEntryType(Type_Entrance);

        Long recover_days_late = (Long) persona.get("recover_days_late");
        recover_days_late = (recover_days_late == null) ? recover_days_late = Long.valueOf(0L)
            : recover_days_late;
        BigDecimal recover_amount_pay = (BigDecimal) persona.get("recover_amount_pay");
        recover_amount_pay = (recover_amount_pay == null) ? recover_amount_pay = BigDecimal.ZERO
            : recover_amount_pay;
        objOpportunity.setShppwsRecoverDaysLate(recover_days_late);
        objOpportunity.setShppwsRecoverAmountPay(recover_amount_pay);
        OBDal.getInstance().save(objOpportunity);
        OBDal.getInstance().flush();
        persona.put("OP_documentno", objOpportunity.getShppwsOpDocumentno());
        persona.put("OP_record_id", objOpportunity.getId());
        if (objCommercialcode != null) {
          Error = "Equifax, Se completó la operación y se crea la oportunidad.";
        } else {
          Error = "No ha sido satisfactoria la carga del comercio.";
        }
      } else {
        Error = "No ha sido satisfactoria la carga del tercero.";
      }

    } catch (Exception e) {
      Error = "Error en la Oportunidad " + e.getMessage();
      OBDal.getInstance().rollbackAndClose();
    }

    try {

      if (filter.equals("R") && objOpportunity.getBusinessPartner() != null) {

        // Se crea una nueva instancia de Bitácora
        shpctBinnacle objBinnacle = OBProvider.getInstance().get(shpctBinnacle.class);
        if (!persona.get("msgLN").equals("CONTINUA")) {
          objBinnacle.setClient(accesApi.getClient());
          objBinnacle.setOrganization(accesApi.getOrganization());
          objBinnacle.setActive(accesApi.isActive());
          objBinnacle.setCreatedBy(accesApi.getCreatedBy());
          objBinnacle.setUpdatedBy(accesApi.getUpdatedBy());
          objBinnacle.setOpcrmOpportunities(objOpportunity);

          message += " op " + objOpportunity.getShppwsOpDocumentno();
          objBinnacle.setNameMatrix((String) persona.get("matrizReason"));
          if (persona.containsKey("Identifier") && persona.get("Identifier") != null
              && !((String) persona.get("Identifier")).isEmpty()) {
            objBinnacle
                .setMessages((String) persona.get("msgLN") + " : " + persona.get("Identifier"));
          } else {
            objBinnacle.setMessages((String) persona.get("msgLN") + "");
          }
          objBinnacle.setResults("R");
          objBinnacle.setComments(persona.get("idLN") + "");
          OBDal.getInstance().save(objBinnacle);
          OBDal.getInstance().flush();
          OBDal.getInstance().getConnection().commit();
          OBDal.getInstance().refresh(objBinnacle);
        }
        Error = objBinnacle.getMessages();
        SHPPWS_Helper_Model.CreateRowLNIdError(persona, objBinnacleError, accesApi, objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNPhoneError(persona, objBinnacleError, accesApi,
            objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNIntDebtError(persona, objBinnacleError, accesApi,
            objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNMailError(persona, objBinnacleError, accesApi,
            objOpportunity);
        if (typeClientSynergy.equals("3")) {
          SHPPWS_Helper_Model.CreateRowSynergyError(persona, objBinnacleError, accesApi,
              objOpportunity);
        }

      } else {
        // Se crea una nueva instancia de Bitácora
        shpctBinnacle objBinnacle = OBProvider.getInstance().get(shpctBinnacle.class);
        objBinnacle.setClient(accesApi.getClient());
        objBinnacle.setOrganization(accesApi.getOrganization());
        objBinnacle.setActive(accesApi.isActive());
        objBinnacle.setCreatedBy(accesApi.getCreatedBy());
        objBinnacle.setUpdatedBy(accesApi.getUpdatedBy());
        objBinnacle.setOpcrmOpportunities(objOpportunity);
        String msgSuccesOpp = "";
        OBCriteria<shppws_config> queryApi = OBDal.getInstance()
            .createCriteria(shppws_config.class);
        shppws_config qmsgSuccesOpp = (shppws_config) queryApi.uniqueResult();
        msgSuccesOpp = qmsgSuccesOpp.getOpportunitySucces();
        objBinnacle.setMessages(msgSuccesOpp);
        objBinnacle.setResults("C");
        objBinnacle.setComments(persona.get("idLN") + "");
        objBinnacle.setNameMatrix((String) persona.get("matriz"));
        objBinnacle.setComments((String) persona.get("RCcedula"));

        SHPPWS_Helper_Model.CreateRowLNIdError(persona, objBinnacleError, accesApi, objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNPhoneError(persona, objBinnacleError, accesApi,
            objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNIntDebtError(persona, objBinnacleError, accesApi,
            objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNMailError(persona, objBinnacleError, accesApi,
            objOpportunity);
        if (typeClientSynergy.equals("3")) {
          SHPPWS_Helper_Model.CreateRowSynergyError(persona, objBinnacleError, accesApi,
              objOpportunity);
        }

        OBDal.getInstance().save(objBinnacle);
        OBDal.getInstance().flush();
        OBDal.getInstance().getConnection().commit();
        OBDal.getInstance().refresh(objBinnacle);
        Error = objBinnacle.getMessages();

      }
    } catch (Exception e) {
      Error = "Hubo un error al generar la Bitácora de la oportunidad "
          + objOpportunity.getShppwsOpDocumentno() + message + e.getMessage() + e.getMessage();
      OBDal.getInstance().rollbackAndClose();
    }
    return Error;
  }

  public String newOpportunity_Exception(String filter, Map<String, Object> persona,
      Ecsce_CustomerException accesApi_exception, shppws_config accesApi, boolean statusEquifax,
      boolean CheckSinergia, boolean statusSinergia) throws JSONException {
    ///// se crea una nueva oportunidad para el proceso de excepcion de clientes
    Opcrmopportunities objOpportunity = OBProvider.getInstance().get(Opcrmopportunities.class);
    shpctBinnacle objBinnacleError = OBProvider.getInstance().get(shpctBinnacle.class);

    String newNumber = "";
    String message = ".";
    String Error = "";
    try {
      String partnerID = (String) persona.get("partnerID") != null
          ? (String) persona.get("partnerID")
          : "";
      BusinessPartner objPartner = OBDal.getInstance().get(BusinessPartner.class, partnerID);
      if (objPartner != null) {
        if (persona.get("CheckDelivery").equals("true")) {
          objOpportunity.setShppwsIssalesdelivery(true);
        } else {
          objOpportunity.setShppwsIssalesdelivery(false);
        }
        objOpportunity.setClient(accesApi.getClient());
        objOpportunity.setOrganization(accesApi.getOrganization());
        objOpportunity.setActive(accesApi.isActive());
        objOpportunity.setCreatedBy(accesApi.getCreatedBy());
        objOpportunity.setUpdatedBy(accesApi.getUpdatedBy());
        objOpportunity.setBusinessPartner(objPartner);
        BigDecimal quotaException = new BigDecimal(accesApi_exception.getQuota());
        objOpportunity.setOpportunityAmount(quotaException);
        objOpportunity.setTAXBpartner(accesApi_exception.getTaxID());
        objOpportunity.setEcsfqEmail((String) persona.get("email"));
        objOpportunity.setCellphoneBpartner((String) persona.get("CellPhone"));
        objOpportunity.setEcsfqPhone((String) persona.get("CellPhone"));
        objOpportunity.setShppwsOpInterface((String) persona.get("interface"));
        objOpportunity.setShppwsOpChannel((String) persona.get("channel"));
        OBCriteria<BusinessPartner> queryBusinessPartner = OBDal.getInstance()
            .createCriteria(BusinessPartner.class);
        queryBusinessPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY,
            (String) persona.get("commercialcode")));
        List<BusinessPartner> listObjBusinessPartner = queryBusinessPartner.list();
        BusinessPartner objCommercialcode = listObjBusinessPartner.get(0);
        if (objCommercialcode != null) {
          objOpportunity.setShppwsOpCodecommercial(objCommercialcode);
        }
        objOpportunity.setShppwsOpAgencycode((String) persona.get("agencycode"));
        objOpportunity.setShppwsOpShopgroup((accesApi_exception.getStoregroup()));
        OBCriteria<scsl_Product> queryprod = OBDal.getInstance().createCriteria(scsl_Product.class);
        queryprod.add(Restrictions.eq(scsl_Product.PROPERTY_VALIDATIONCODE,
            (String) persona.get("productcode")));
        scsl_Product product = (scsl_Product) queryprod.uniqueResult();

        objOpportunity.setShppwsOpProductcode(product);
        objOpportunity.setShppwsOpEndsegment(accesApi_exception.getFinalsegment());
        objOpportunity.setEcsfqPaymenttype(accesApi.getOPPaymentType());// 1
        objOpportunity.setOpportunityName(accesApi.getOPName());// 2
        Date currentDate = new Date();
        objOpportunity.setExpectedCloseDate(currentDate);
        if (filter.equals("R")) {
          objOpportunity.setOpportstatus("LOST");//
        } else {
          objOpportunity.setOpportstatus(accesApi.getOPOpportunityStatus());// 3
        }
        if (!statusEquifax) {
          objOpportunity.setShppwsDefaultOppSeg(true);
        }
        if (CheckSinergia) {
          if (!statusSinergia) {
            objOpportunity.setShpctUnresponseSynergy(true);
          }
        }
        objOpportunity.setOpportunityType(accesApi.getOPOpportunityType());// 4
        objOpportunity.setLeadSource(accesApi.getOPLeadSource());// 5
        objOpportunity.setShppwsOpDocumentType(accesApi.getOPDocumentType());// 6
        // extra del cupo en seccion datos credito
        objOpportunity.setShpcfOpCoupon(accesApi_exception.getQuota());
        boolean CustomerexcepY = true;
        objOpportunity.setEcsceCustomerexcep(CustomerexcepY);
        objOpportunity.setShpctException(accesApi_exception);
        // Nuevo
        String OpportunityNo = (String) persona.get("docnoTransaction");

        objOpportunity.setShppwsOpDocumentno(OpportunityNo);
        objOpportunity.setDescription("Cédulas: NOT" + "\nTeléfonos: NOT" + "\nCorreos: NOT");

        String City_store_group = (String) persona.get("City_store_group");
        if (City_store_group != null) {
          objOpportunity.setShppwsCityStoreGroup(City_store_group);
        }
        String Province_store_group = (String) persona.get("Province_store_group");
        if (Province_store_group != null) {
          objOpportunity.setShppwsProvinceStoreGroup(Province_store_group);
        }

        // New fields
        Long npcredits = objPartner.getShpctNoPunishedCredits();
        if (npcredits == null) {
          npcredits = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCrdsWritten(npcredits); // N0 creditos castigados
        Long nccredits = objPartner.getShpctNoCurrentCredits();
        if (nccredits == null) {
          nccredits = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCurCrds(nccredits);// No. Créditos Vigentes
        Long ncpaid = objPartner.getShpctNoCreditsPaid();
        if (ncpaid == null) {
          ncpaid = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCrdsPaid(ncpaid);// No. Créditos Pagados
        Long ncexpired = objPartner.getShpctNoCCreditsExpired();
        if (ncexpired == null) {
          ncexpired = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCurOverCrds(ncexpired);// No. Créditos Vigentes Vencidos
        Long lipaid = objPartner.getShpctLastInstallmentpaid();
        if (lipaid == null) {
          lipaid = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpLastPaidLastcrd(lipaid);// Última cuota pagada del último crédito
        Long mfpaid = objPartner.getShpctMaximumFeePaid();
        if (mfpaid == null) {
          mfpaid = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoMaxFeePaid(mfpaid);// No. Cuota Máxima Pagada
        Long dlinstallments = objPartner.getShpctDayslateNstallments();
        if (dlinstallments == null) {
          dlinstallments = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpDaysArInstall(dlinstallments);// Días de atraso en cuotas
        Long ddsdelay = objPartner.getShpctDaysdueSecondDelay();
        if (ddsdelay == null) {
          ddsdelay = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpDays2ndMaxAr(ddsdelay);// Días mora del 2do atraso máximo
        Long coperations = objPartner.getShpctCurrentOperations();
        if (coperations == null) {
          coperations = Long.valueOf(0L);
        }
        objOpportunity.setShppwsOpNoCurOperations(coperations);// No. Operaciones Vigentes
        BigDecimal irisk = objPartner.getShpctRiskIndex();
        if (irisk == null) {
          irisk = BigDecimal.ZERO;
        }
        objOpportunity.setShppwsOpRiskIndex(irisk);// Índice de Riesgo
        BigDecimal qused = objPartner.getShpctQuotaUsed();
        if (qused == null) {
          qused = BigDecimal.ZERO;
        }
        objOpportunity.setShppwsOpQuotaUsed(qused);// Cupo Utilizado

        BigDecimal EQ_entrance = BigDecimal.ZERO;
        try {
          EQ_entrance = accesApi_exception.getEntry();
        } catch (Exception e) {
        }
        String Type_Entrance = "";
        try {
          Type_Entrance = accesApi_exception.getTypeInput();
        } catch (Exception e) {
        }
        objOpportunity.setShppwsEntryAmount(EQ_entrance);
        objOpportunity.setShppwsEntryType(Type_Entrance);

        Long recover_days_late = (Long) persona.get("recover_days_late");
        recover_days_late = (recover_days_late == null) ? recover_days_late = Long.valueOf(0L)
            : recover_days_late;
        BigDecimal recover_amount_pay = (BigDecimal) persona.get("recover_amount_pay");
        recover_amount_pay = (recover_amount_pay == null) ? recover_amount_pay = BigDecimal.ZERO
            : recover_amount_pay;
        objOpportunity.setShppwsRecoverDaysLate(recover_days_late);
        objOpportunity.setShppwsRecoverAmountPay(recover_amount_pay);

        if (objCommercialcode != null) {
          OBDal.getInstance().save(objOpportunity);
          OBDal.getInstance().flush();
          persona.put("OP_documentno", objOpportunity.getShppwsOpDocumentno());
          persona.put("OP_record_id", objOpportunity.getId());
          Error = "Equifax, Se completó la operación y se crea la oportunidad.";
        } else {
          Error = "No ha sido satisfactoria la carga del comercio.";
        }
      } else {
        Error = "No ha sido satisfactoria la carga del tercero.";
      }

    } catch (Exception e) {
      Error = "Error en la Oportunidad " + e.getMessage();
      OBDal.getInstance().rollbackAndClose();
    }

    try {
      if (filter.equals("R") && objOpportunity.getBusinessPartner() != null) {

        // Se crea una nueva instancia de Bitácora
        shpctBinnacle objBinnacle = OBProvider.getInstance().get(shpctBinnacle.class);
        objBinnacle.setClient(accesApi.getClient());
        objBinnacle.setOrganization(accesApi.getOrganization());
        objBinnacle.setActive(accesApi.isActive());
        objBinnacle.setCreatedBy(accesApi.getCreatedBy());
        objBinnacle.setUpdatedBy(accesApi.getUpdatedBy());
        objBinnacle.setOpcrmOpportunities(objOpportunity);

        message += " op " + objOpportunity.getShppwsOpDocumentno();
        objBinnacle.setNameMatrix((String) persona.get("matrizReason"));
        if (persona.containsKey("Identifier") && persona.get("Identifier") != null
            && !((String) persona.get("Identifier")).isEmpty()) {
          objBinnacle
              .setMessages((String) persona.get("msgLN") + " : " + persona.get("Identifier"));
        } else {
          objBinnacle.setMessages((String) persona.get("msgLN") + "");
        }
        objBinnacle.setResults("R");
        objBinnacle.setComments(persona.get("idLN") + "");
        OBDal.getInstance().save(objBinnacle);
        OBDal.getInstance().flush();
        OBDal.getInstance().getConnection().commit();
        OBDal.getInstance().refresh(objBinnacle);
        Error = objBinnacle.getMessages();
        SHPPWS_Helper_Model.CreateRowLNIdError(persona, objBinnacleError, accesApi, objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNPhoneError(persona, objBinnacleError, accesApi,
            objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNIntDebtError(persona, objBinnacleError, accesApi,
            objOpportunity);
        SHPPWS_Helper_Model.CreateRowLNMailError(persona, objBinnacleError, accesApi,
            objOpportunity);
        if (typeClientSynergy.equals("3")) {
          SHPPWS_Helper_Model.CreateRowSynergyError(persona, objBinnacleError, accesApi,
              objOpportunity);
        }
      }
    } catch (Exception e) {
      Error = "Hubo un error al generar la Bitácora de la oportunidad "
          + objOpportunity.getShppwsOpDocumentno() + message + e.getMessage() + e.getMessage();
      OBDal.getInstance().rollbackAndClose();
    }
    return Error;
  }

  public String validateNewClient(Map<String, Object> persona, Boolean clientnew,
      boolean statusEquifax, String ID, String Store_group, BusinessPartner partner,
      shppws_config accesApi) throws JSONException {

    String filterEQ = "";
    String errorEq = persona.containsKey("msgLN") ? (String) persona.get("msgLN") : "";

    // Validacion Inclucion
    boolean isInclusion = accesApi.isEquifaxInclusion();
    String codeFlt = clientnew ? accesApi.getEquifaxProfNewclient()
        : accesApi.getEquifaxProfOldclient();
    String codeInclusion = getCodeINclusion(persona);
    try {
      if (Boolean.TRUE.equals(clientnew)) { // proceso para Clientes nuevos

        if ("true".equals(persona.get("Exception_Institucion"))) {
          filterEQ = (String) persona.get("Segment");

        } else {
          Double CV = persona.get("CV") != null ? (Double) persona.get("CV") : 0D;
          Double CC = persona.get("CC") != null ? (Double) persona.get("CC") : 0D;
          Double DJ = persona.get("DJ") != null ? (Double) persona.get("DJ") : 0D;

          Double auxValuesSummary = CV + CC + DJ;

          if (auxValuesSummary > 0) {
            // Si -> Compara en las tres matrices y devuelve un único segmento
            validateEquifaxArrays(ID, Store_group, persona);

            String segment1 = (String) persona.get("segment1ExpiredValues");
            String segment2 = (String) persona.get("segment2cartValues");
            String segment3 = (String) persona.get("segment3lawsuitValues");

            if ("R".equals(segment1) || "R".equals(segment2) || "R".equals(segment3)) {
              filterEQ = "R";
              persona.put("matriz", "Recover");
              // persona.put("matrizReason", "Matriz Crédito Vigente Vencido");
            } else {
              ///////////////////////////////////////////////////////
              if (!statusEquifax) {//////////////////////////////////// AQUI EQUIFAX ESTADO
                filterEQ = SHPPWS_Helper_Model.getScoreNewClientDefault(accesApi, clientnew,
                    codeInclusion, false);

              } else {
                filterEQ = "SP";
                validateEquifaxScoreNewClientSP(persona, filterEQ, accesApi, clientnew);
                filterEQ = (String) persona.get("EQ_scoreClient");

              }
            }

          } else {
            // Sin deuda interna -> Score cliente nuevo
            if (persona.get("Mensaje_Operacional").equals("Error equifax")) {
              filterEQ = SHPPWS_Helper_Model.getScoreNewClientDefault(accesApi, clientnew,
                  codeInclusion, false);

            } else {
              validateEquifaxScoreNewClient(persona, accesApi, clientnew);
              filterEQ = (String) persona.get("EQ_scoreClient");

            }
          }
        }
      } else {
        // // No es cliente nuevo (por compatibilidad, mantenemos comportamiento anterior)
        // if (persona.get("Mensaje_Operacional").equals("Error equifax")) {
        // filterEQ = isInclusion ? codeFlt : SHPPWS_Helper_Model.getDefaultRiskIndiexEqError();
        //
        // } else {
        // Créditos paralelos
        String parallelCreditMatrix = parallelCredits(partner, persona);
        // Indice de Riesgo
        filterEQ = customerRiskIndex(partner, parallelCreditMatrix, persona, accesApi, clientnew);

      }

    } catch (JSONException e) {
      // Ante error interno, rechazamos y dejamos trazabilidad mínima
      filterEQ = SHPPWS_Helper_Model.getScoreNewClientDefault(accesApi, clientnew, codeInclusion,
          false);
      persona.put("message", "Error interno validando Equifax");
      log4j.error("Error en validateNewClient para ID: " + ID, e);
    }

    return filterEQ;
  }

  public String EQnewSearch_filter(Map<String, Object> persona, Boolean clientnew) {
    String filter = "";
    Boolean statusSinergia = true;
    if (Boolean.TRUE.equals(clientnew)) {
      if ((persona.containsKey("Exception_Institucion")
          && persona.get("Exception_Institucion").equals("true"))
          && persona.get("EQ_scoreClient").equals("R")) {
        filter = "R";
        if (persona.containsKey("msgLN") && persona.get("msgLN").equals("Error equifax")) {
          filter = persona.containsKey("EQ_segmentacion") ? (String) persona.get("EQ_segmentacion")
              : filter;
        }
        statusSinergia = false;
      } else if (persona.containsKey("EQ_scoreClient")) {
        filter = (String) persona.get("EQ_scoreClient");
      }
    } else if (persona.containsKey("EQ_scoreClient")) {
      filter = (String) persona.get("EQ_scoreClient");
    }
    return filter;
  }

  public void validateReferences(String filter, Map<String, Object> persona) throws JSONException {
    String Nacionality = (String) persona.get("RCnationality");
    if (Nacionality == null) {
      Nacionality = "";
    }
    if (!Nacionality.equals("ECUATORIANA")) {
      Nacionality = "EXTRANJERA";
    }
    OBCriteria<ShpperReferenceMatrix> queryRefrence = OBDal.getInstance()
        .createCriteria(ShpperReferenceMatrix.class);
    queryRefrence
        .add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_SHOPGROUP, persona.get("shopgroup")));
    queryRefrence.add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_ENDSEGMENT, filter));
    queryRefrence.add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_NATIONALITY, Nacionality));
    if (!queryRefrence.list().isEmpty()) {
      try {
        ShpperReferenceMatrix objReference = (ShpperReferenceMatrix) queryRefrence.uniqueResult();
        persona.put("Ref1", objReference.getReferenceType1());
        persona.put("Ref2", objReference.getReferenceType2());

      } catch (Exception e) {
        persona.put("Ref1", ".");
        persona.put("Ref2", ".");
      }
    } else {
      OBCriteria<ShpperReferenceMatrix> queryRefrenceD = OBDal.getInstance()
          .createCriteria(ShpperReferenceMatrix.class);
      queryRefrenceD.add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_SHPPWSDEFAULTFIELD, true));
      List<ShpperReferenceMatrix> listRefrenceDef = queryRefrenceD.list();
      try {
        ShpperReferenceMatrix objReference = listRefrenceDef.get(0);
        persona.put("Ref1", objReference.getReferenceType1());
        persona.put("Ref2", objReference.getReferenceType2());
      } catch (Exception e) {
        persona.put("Ref1", ".");
        persona.put("Ref2", ".");
      }
    }
  }

  public void validateReferencesException(String filter, Map<String, Object> persona, Date now)
      throws JSONException {
    String Nacionality = (String) persona.get("RCnationality");
    if (Nacionality == null) {
      Nacionality = "";
    }
    if (!Nacionality.equals("ECUATORIANA")) {
      Nacionality = "EXTRANJERA";
    }
    OBCriteria<Ecsce_CustomerException> customerexception = OBDal.getInstance()
        .createCriteria(Ecsce_CustomerException.class);
    customerexception
        .add(Restrictions.and(
            Restrictions.and(
                Restrictions.eq(Ecsce_CustomerException.PROPERTY_TAXID, persona.get("Identifier")),
                Restrictions.eq(Ecsce_CustomerException.PROPERTY_STOREGROUP,
                    persona.get("shopgroup"))),
            Restrictions.and(Restrictions.le(Ecsce_CustomerException.PROPERTY_STARTINGDATE, now),
                Restrictions.ge(Ecsce_CustomerException.PROPERTY_DATEUNTIL, now)) // dateUntil >=
                                                                                  // hoy
        ));
    customerexception.addOrder(Order.desc(Ecsce_CustomerException.PROPERTY_CREATIONDATE)); // más
                                                                                           // reciente
    customerexception.setMaxResults(1);
    Ecsce_CustomerException cf = (Ecsce_CustomerException) customerexception.uniqueResult();

    OBCriteria<ShpperReferenceMatrix> queryRefrence = OBDal.getInstance()
        .createCriteria(ShpperReferenceMatrix.class);
    queryRefrence
        .add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_SHOPGROUP, cf.getStoregroup()));
    queryRefrence
        .add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_ENDSEGMENT, cf.getFinalsegment()));
    queryRefrence.add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_NATIONALITY, Nacionality));
    persona.put("SegmentException", cf.getFinalsegment());

    if (!queryRefrence.list().isEmpty()) {
      try {
        ShpperReferenceMatrix objReference = (ShpperReferenceMatrix) queryRefrence.uniqueResult();
        persona.put("Ref1", objReference.getReferenceType1());
        persona.put("Ref2", objReference.getReferenceType2());

      } catch (Exception e) {
        persona.put("Ref1", ".");
        persona.put("Ref2", ".");
      }
    } else {
      OBCriteria<ShpperReferenceMatrix> queryRefrenceD = OBDal.getInstance()
          .createCriteria(ShpperReferenceMatrix.class);
      queryRefrenceD.add(Restrictions.eq(ShpperReferenceMatrix.PROPERTY_SHPPWSDEFAULTFIELD, true));
      List<ShpperReferenceMatrix> listRefrenceDef = queryRefrenceD.list();
      try {
        ShpperReferenceMatrix objReference = listRefrenceDef.get(0);
        persona.put("Ref1", objReference.getReferenceType1());
        persona.put("Ref2", objReference.getReferenceType2());
      } catch (Exception e) {
        persona.put("Ref1", ".");
        persona.put("Ref2", ".");
      }
    }

  }

  private String applyBlacklists(shppws_config accesApi, String ID, String CellPhone, String email,
      Map<String, Object> persona, JSONObject jsonMonitor, String flt,
      boolean CustomerExeption_result, Holder<String> errorHolder) {
    String filter = flt;
    if (!filter.equals("C") || !CustomerExeption_result) {
      filter = "R";
    }

    String Error = errorHolder.value;
    try {
      Boolean validateServices = true;
      String responseLN_CI = "";
      Boolean validate_CI = false;
      String responseLN_Celphone = "";
      Boolean validate_Celphone = false;
      String responseLN_Email = "";
      Boolean validate_Email = false;
      Boolean validate_Profession = false;
      String refNo = (String) persona.get("identifierLog");
      try {
        if (!SHPPWS_Helper_Model.validateBlackListcheckCedula()) {
          responseLN_CI = getApiResponse(accesApi, ID, 2, jsonMonitor, refNo);
          validate_CI = validateLN_CI(ID, responseLN_CI, persona);
        } else {
          validate_CI = true;
        }
      } catch (Exception e) {
        filter = accesApi.getLN1Response();
        SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID, "Motivo Cédulas");
        persona.put("haserror", "Motivo Cédulas");
        validate_CI = (filter != null && filter.equals("C")) ? true : false;
        Error = e.getMessage();
      }

      try {
        if (!SHPPWS_Helper_Model.validateBlackListchecktelefono()) {
          responseLN_Celphone = getApiResponse(accesApi, CellPhone, 3, jsonMonitor,
              refNo + " - " + ID);
          validate_Celphone = validateLN_Cellphone(CellPhone, responseLN_Celphone, persona);
        } else {
          validate_Celphone = true;
        }

      } catch (Exception e) {
        filter = accesApi.getLN2Response();

        SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID, "Motivo Teléfonos");
        persona.put("haserror", "Motivo Teléfonos");
        validate_Celphone = (filter != null && filter.equals("C")) ? true : false;
        Error = e.getMessage();
      }

      try {
        if (!SHPPWS_Helper_Model.validateBlackListcheckemail()) {
          responseLN_Email = getApiResponse(accesApi, email, 4, jsonMonitor, refNo + " - " + ID);
          validate_Email = validateLN_Email(email, responseLN_Email, persona);
        } else {
          validate_Email = true;
        }

      } catch (Exception e) {
        filter = accesApi.getLN3Response();

        SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID, "Motivo Correos");
        persona.put("haserror", "Motivo Correos");
        validate_Email = (filter != null && filter.equals("C")) ? true : false;
        Error = e.getMessage();
      }

      try {
        validate_Profession = validateLN_Profession(persona);
        SHPPWS_Helper_Model.validateBlackListcheckemail();
        if (validate_Profession) {
          filter = "C";
        } else {
          filter = "R";
        }

      } catch (Exception e) {
        filter = "R";
        SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID, "Motivo Profesión");
        persona.put("haserror", "Motivo Profesión");
        Error = e.getMessage();
      }

      if (!validate_CI || !validate_Celphone || !validate_Email || !validate_Profession) {
        filter = "R";
      }

    } catch (Exception e) {
      filter = "R";

      SHPPWS_Helper_Model.putSectionRejected(persona, "Servicio de Listas Negras fuera de línea",
          ID, "Servicio de Listas Negras");
      persona.put("haserror", "Servicio de Listas Negras");
      Error = e.getMessage();
    }

    errorHolder.value = Error;
    return filter;
  }

  private static class Holder<T> {
    T value;

    Holder(T value) {
      this.value = value;
    }
  }

  public void ValidateIntDebtStatus(String responseLN_CI, Map<String, Object> persona)
      throws JSONException {
    // Metodo para futuras validaciones de estados de cedula
    // apiResponse contiene el JSON como String
    JSONArray jsonArray = new JSONArray(responseLN_CI);

    String motivoBuscado = null;

    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject item = jsonArray.getJSONObject(i);

      if ("HAPPYCEL".equals(item.optString("empresa"))) {
        motivoBuscado = item.optString("motivo");
        break;
      }
    }

    // Validación
    if ("CLIENTES REPORTADOS POR LA UDF".equals(motivoBuscado)) {

      persona.put("LNIntDebtStatus", "ErrorDeudaInterna");
      persona.put("LNIntDebtMotivo", motivoBuscado);
      persona.put("LNIntDebtAll", jsonArray.toString());

    } else {
      persona.put("LNIntDebtStatus", "OkDeudaInterna");
    }
  }

  public String codigoListasNegras(String validateCustomer_old_new_Synergy, String Error,
      shppws_config accesApi, String ID, String CellPhone, String email,
      Map<String, Object> persona, JSONObject jsonMonitor, boolean CustomerExeption_result,
      Boolean CheckSinergia, Boolean statusSinergia, String flt) {
    //////////////////////////////////
    /////// FILTRO Listas Negras /////
    //////////////////////////////////
    String filter = flt;

    if (validateCustomer_old_new_Synergy.equals("CLIENTE RECHAZADO")) {
      filter = "R";
    } else {
      Holder<String> errorHolder = new Holder<>(Error);
      filter = applyBlacklists(accesApi, ID, CellPhone, email, persona, jsonMonitor, filter,
          CustomerExeption_result, errorHolder);
      Error = errorHolder.value;
    }

    //////////////////////////////////
    //// FILTRO Deudas Internas //////
    //////////////////////////////////
    if (filter.equals("C") && CustomerExeption_result) {
      try {
        String response_DI_DebitIn = getApiResponse(accesApi, ID, 5, jsonMonitor,
            (String) persona.get("identifierLog"));
        Boolean validate_DI = validateDataDI(response_DI_DebitIn, persona);
        if (validate_DI) {
          filter = "C";
        } else {
          filter = "R";
        }
      } catch (Exception e) {

        SHPPWS_Helper_Model.putSectionRejected(persona, e.getMessage(), ID,
            "Servicio de Deudas Internas");
        persona.put("haserror", "Servicio de Deudas Internas");
        filter = accesApi.getLndiResponse();
        Error = e.getMessage();
      }
    }

    if (CheckSinergia && statusSinergia
        && "CLIENTE APROBADO".equals(validateCustomer_old_new_Synergy)
        && "2".equals(typeClientSynergy)) {

      if (!accesApi.isSynergyValidateReiterative()) {
        filter = "C";
      } else {
        if (filter.equals("R")) {
          persona.put("matriz", "Control de Seguridad");
          persona.put("matrizReason", "Validación Reiterativos");

          String motivoOriginal = (String) persona.get("msgLN");
          String mensajeFinal = "Bloqueado por Lista Negra: "
              + (motivoOriginal != null ? motivoOriginal : "Reporte Externo");
          persona.put("msgLN", mensajeFinal);
        }
      }
    }
    return filter;
  }

  public Map<String, Object> codigoClienteNuevoAntiguo(Boolean CheckSinergia, Boolean stsSinergia,
      String response_DI, String valOldNewSny, shppws_config accesApi, String ID, int filternumber,
      JSONObject jsonMonitor, String flt, Map<String, Object> persona, BusinessPartner partner,
      Boolean CustomerExeption_result, String Error) throws Exception {
    //////////////////////////////////
    // FILTRO Cliente nuevo antiguo //
    //////////////////////////////////
    Boolean clientnew = false;
    Boolean validateCustomer_old_cc = null;
    Boolean validateCustomer_old_cv = null;
    Boolean validateCustomer_old_cvv = null;
    Boolean validateCustomer_old_new_Standar = customerNewOld(persona, partner);
    Boolean statusSinergia = stsSinergia;

    if (CheckSinergia) {
      if (statusSinergia) {
        response_DI = getApiResponse(accesApi, ID, 8, jsonMonitor,
            (String) persona.get("identifierLog"));
        if (!response_DI.equals("Error sinergia")) {
          valOldNewSny = validateStatusPartner(response_DI);
        } else {
          statusSinergia = false;
        }
      }
    }
    String filter = flt;

    // verifica que el check de sinergia este activo y el estado de sinergia sea diferente de R, si
    // da R se pasara a false
    if (CheckSinergia) {
      if (statusSinergia) {
        if (response_DI.equals("Error sinergia")) {
          filter = "R";
          Error = "Servicio de Deudas Internas fuera de línea";
          SHPPWS_Helper_Model.putSectionRejected(persona,
              "Servicio de Deudas Internas fuera de línea", ID, "Servicio de Deudas Internas");
          persona.put("haserror", "Servicio de Deudas Internas");
          statusSinergia = false;
        }
        if (valOldNewSny.equals("CLIENTE RECHAZADO")) {
          filter = "R";
        }
        if (filter.equals("C") && CustomerExeption_result) {
          typeClientSynergy = classificationClient(response_DI);
          if (valOldNewSny.equals("CLIENTE NUEVO")) {// customer new
            filter = "C";
            clientnew = true;
          } else {// customer old
            clientnew = false;
            if (valOldNewSny.equals("CLIENTE APROBADO") || StringUtils.isBlank(valOldNewSny)) {
              // if (valOldNewSny.equals("CLIENTE APROBADO") || StringUtils.isBlank(valOldNewSny)) {
              filter = "C";
            } else {
              filter = "R";
              statusSinergia = false;
            }

            validateCustomer_old_cc = customerOldCC(partner, persona, valOldNewSny);// Matriz
                                                                                    // credito
            // castigado
            if (validateCustomer_old_cc) {
              validateCustomer_old_cv = customerOldCV(partner, persona, valOldNewSny);// Matriz
                                                                                      // credito
              // vigente
              if (validateCustomer_old_cv) {
                validateCustomer_old_cvv = customerOldCVV(partner, persona, valOldNewSny);// Matriz
                                                                                          // credito
                                                                                          // vigente
                // vencido
                if (validateCustomer_old_cvv) {
                  filter = "C";
                } else {
                  filter = "R";
                  statusSinergia = false;
                }
              } else {
                filter = "R";
                statusSinergia = false;
              }
            } else {
              filter = "R";
              statusSinergia = false;
            }

          }
        }
      } else {
        filter = "C";
        if (filter.equals("C") && CustomerExeption_result) {
          typeClientSynergy = "Error en sinergia";
          if (validateCustomer_old_new_Standar) {// customer new
            filter = "C";
            clientnew = true;
          } else {// customer old
            clientnew = false;
            validateCustomer_old_cc = customerOldCC_uncheck(partner, persona);// Matriz credito
                                                                              // castigado
            if (validateCustomer_old_cc) {
              validateCustomer_old_cv = customerOldCV_uncheck(partner, persona);// Matriz credito
                                                                                // vigente
              if (validateCustomer_old_cv) {
                validateCustomer_old_cvv = customerOldCVV_uncheck(partner, persona);// Matriz
                                                                                    // credito
                                                                                    // vigente
                                                                                    // vencido
                if (validateCustomer_old_cvv) {
                  filter = "C";
                } else {
                  filter = "R";
                }
              } else {
                filter = "R";
              }
            } else {
              filter = "R";
            }
          }
        }
      }
    } else {
      if (filter.equals("C") && CustomerExeption_result) {
        if (validateCustomer_old_new_Standar) {// customer new
          filter = "C";
          clientnew = true;
        } else {// customer old
          clientnew = false;
          validateCustomer_old_cc = customerOldCC_uncheck(partner, persona);// Matriz credito
                                                                            // castigado
          if (validateCustomer_old_cc) {
            validateCustomer_old_cv = customerOldCV_uncheck(partner, persona);// Matriz credito
                                                                              // vigente
            if (validateCustomer_old_cv) {
              validateCustomer_old_cvv = customerOldCVV_uncheck(partner, persona);// Matriz credito
                                                                                  // vigente vencido
              if (validateCustomer_old_cvv) {
                filter = "C";
              } else {
                filter = "R";
              }
            } else {
              filter = "R";
            }
          } else {
            filter = "R";
          }
        }
      }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("clientnew", clientnew);
    result.put("filter", filter);
    result.put("response_DI", response_DI);
    result.put("validateCustomer_old_new_Synergy", valOldNewSny);
    result.put("validateCustomer_old_new_Standar", validateCustomer_old_new_Standar);
    result.put("Error", Error);
    result.put("statusSinergia", statusSinergia);
    return result;
  }

  public String getCodeINclusion(Map<String, Object> persona) throws JSONException {

    JSONObject jsonInclusion = persona.containsKey("inclusionData")
        ? (JSONObject) persona.get("inclusionData")
        : new JSONObject();
    return jsonInclusion.has("decision") ? jsonInclusion.getString("decision") : "NA-";

  }

  public void inclusionbyDefault(Map<String, Object> persona, String decision, String value)
      throws JSONException {

    JSONObject jsonInclusion = new JSONObject();
    jsonInclusion.put("decision", decision);
    jsonInclusion.put("value", value);
    persona.put("inclusionData", jsonInclusion);

  }

}
