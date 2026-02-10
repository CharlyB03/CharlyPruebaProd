package ec.com.sidesoft.happypay.web.services.service;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.web.WebService;

import com.sidesoft.localization.ecuador.withholdings.Taxpayer;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.web.services.service.utils.SHPPWS_Helper;
import ec.com.sidesoft.happypay.web.services.shppws_config;

import ec.com.sidesoft.localization.geography.secpm_canton;

public class SHPPWS_BusinessPartner implements WebService {
	  private final Logger logger = Logger.getLogger(SHPPWS_BusinessPartner.class);

	  @Override
	  public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
	      throws Exception {
	    JSONObject json = new JSONObject();
	    String logId = null;
	    ConnectionProvider conn = new DalConnectionProvider(false);

	    try {
	      OBContext.setAdminMode(true);
	      logger.info("Begin BusinessPartner doPost");

	      JSONObject body = SHPPWS_Helper.readAllIntoJSONObject(request.getInputStream());

	      Scactu_Log log = SHPPWS_Helper.createLog(body, "createBusinessPartner", "IN");
	      logId = log.getId();

	      createBusinessPartner(conn, body);
	      json = SHPPWS_Helper.getResponse(body);

	      log.setJsonResponse(json.toString());
	      log.setRecordID(body.getString("id"));
	      log.setResult("OK");
	      OBDal.getInstance().save(log);
	      OBDal.getInstance().flush();
	      OBDal.getInstance().commitAndClose();
	    } catch (Exception e) {
	      OBDal.getInstance().rollbackAndClose();
	      String message = SHPPWS_Helper.getErrorMessage(logger, e);
	      logger.error(message);
	      Scactu_Log log = OBDal.getInstance().get(Scactu_Log.class, logId);
	      if (log != null) {
	        log.setResult("ERROR");
	        log.setError(message);
	        OBDal.getInstance().save(log);
	        OBDal.getInstance().flush();
	        OBDal.getInstance().getConnection().commit();
	      }
	      json = SHPPWS_Helper.getErrorResponse(message);
	    } finally {
	      try {
	        conn.getConnection().close();
	        conn.destroy();
	      } catch (Exception e) {
	    	  SHPPWS_Helper.getErrorMessage(logger, e);
	      }
	      OBContext.restorePreviousMode();
	      logger.info("Finish BusinessPartner doPost");
	    }

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    Writer w = response.getWriter();
	    w.write(json.toString());
	    w.close();
	  }

	  @Override
	  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
	      throws Exception {
	    JSONObject json = new JSONObject();
	    String logId = null;

	    try {
	      OBContext.setAdminMode(true);
	      logger.info("Begin BusinessPartner doGet");

	      JSONObject body = new JSONObject();
	      Scactu_Log log = SHPPWS_Helper.createLog(body, "getBusinessPartner", "OUT");
	      logId = log.getId();

	      String taxId = request.getParameter("taxId");
	      if (StringUtils.isEmpty(taxId)) {
	        throw new OBException("taxId is required");
	      }

	      getBusinessPartner(taxId.trim(), body);
	      json = SHPPWS_Helper.getResponse(body);
	      log.setJsonResponse(json.toString());
	      log.setRecordID(body.getString("id"));
	      log.setResult("OK");
	      OBDal.getInstance().save(log);
	      OBDal.getInstance().flush();
	      OBDal.getInstance().commitAndClose();
	    } catch (Exception e) {
	      String message = SHPPWS_Helper.getErrorMessage(logger, e);
	      logger.error(message);
	      json = SHPPWS_Helper.getErrorResponse(message);

	      Scactu_Log log = StringUtils.isNotBlank(logId)
	          ? OBDal.getInstance().get(Scactu_Log.class, logId)
	          : null;
	      if (log != null) {
	        log.setResult("ERROR");
	        log.setError(message);
	        log.setJsonResponse(json.toString());
	        OBDal.getInstance().save(log);
	        OBDal.getInstance().flush();
	        OBDal.getInstance().getConnection().commit();
	      }
	    } finally {
	      OBContext.restorePreviousMode();
	      logger.info("Finish BusinessPartner doGet");
	    }

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    Writer w = response.getWriter();
	    w.write(json.toString());
	    w.close();
	  }

	  @Override
	  public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
	      throws Exception {
	    JSONObject json = new JSONObject();
	    String logId = null;
	    ConnectionProvider conn = new DalConnectionProvider(false);

	    try {
	      OBContext.setAdminMode(true);
	      logger.info("Begin BusinessPartner doPut");

	      JSONObject body = SHPPWS_Helper.readAllIntoJSONObject(request.getInputStream());

	      Scactu_Log log = SHPPWS_Helper.createLog(body, "updateBusinessPartner", "IN");
	      logId = log.getId();

	      updateBusinessPartner(conn, body);
	      json = SHPPWS_Helper.getResponse(body);

	      log.setJsonResponse(json.toString());
	      log.setRecordID(body.getString("id"));
	      log.setResult("OK");
	      OBDal.getInstance().save(log);
	      OBDal.getInstance().flush();
	      OBDal.getInstance().commitAndClose();
	    } catch (Exception e) {
	      OBDal.getInstance().rollbackAndClose();
	      String message = SHPPWS_Helper.getErrorMessage(logger, e);
	      logger.error(message);
	      Scactu_Log log = OBDal.getInstance().get(Scactu_Log.class, logId);
	      if (log != null) {
	        log.setResult("ERROR");
	        log.setError(message);
	        OBDal.getInstance().save(log);
	        OBDal.getInstance().flush();
	        OBDal.getInstance().getConnection().commit();
	      }
	      json = SHPPWS_Helper.getErrorResponse(message);
	    } finally {
	      try {
	        conn.getConnection().close();
	        conn.destroy();
	      } catch (Exception e) {
	        SHPPWS_Helper.getErrorMessage(logger, e);
	      }
	      OBContext.restorePreviousMode();
	      logger.info("Finish BusinessPartner doPut");
	    }

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    Writer w = response.getWriter();
	    w.write(json.toString());
	    w.close();
	  }

	  @Override
	  public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
	      throws Exception {
	  }

	  public BusinessPartner createBusinessPartner(ConnectionProvider conn, JSONObject body)
	      throws Exception {
	    String taxId = SHPPWS_Helper.getString(body, "taxId", true);
	    String taxIdType = SHPPWS_Helper.getString(body, "taxIdType", true);
	    String taxPayer = SHPPWS_Helper.getString(body, "taxPayer", true);
	    String name1 = SHPPWS_Helper.getString(body, "name1", true);
	    String name2 = SHPPWS_Helper.getString(body, "name2", false);
	    String lastName1 = SHPPWS_Helper.getString(body, "lastName1", false);
	    String lastName2 = SHPPWS_Helper.getString(body, "lastName2", false);
	    String fiscalName = SHPPWS_Helper.getString(body, "socialName", false);
	    String commercialName = SHPPWS_Helper.getString(body, "comercialName", false);
	    String email = SHPPWS_Helper.getString(body, "email", true);
	    String typeTaxPayerId = SHPPWS_Helper.getString(body, "typeTaxPayerId", false);
	    String url = SHPPWS_Helper.getString(body, "url", false);
	    Boolean isCustomer = SHPPWS_Helper.getBoolean(body, "isCustomer", false);
	    Boolean isVendor = SHPPWS_Helper.getBoolean(body, "isVendor", false);
	    Boolean isEmployee = SHPPWS_Helper.getBoolean(body, "isEmployee", false);
	    Boolean isSalesRepresentative = SHPPWS_Helper.getBoolean(body, "isSalesRepresentative", false);

	    OBCriteria<shppws_config> qConfig = OBDal.getInstance().createCriteria(shppws_config.class);
	    qConfig.add(Restrictions.eq(shppws_config.PROPERTY_ACTIVE, true));
	    qConfig.addOrder(org.hibernate.criterion.Order.desc(shppws_config.PROPERTY_ORGANIZATION));
	    qConfig.setMaxResults(1);
	    if (qConfig.list().size() == 0) {
	      throw new OBException("Configuration not found");
	    }
	    shppws_config config = (shppws_config) qConfig.uniqueResult();

	    OBCriteria<BusinessPartner> qBusinessPartner = OBDal.getInstance()
	        .createCriteria(BusinessPartner.class);
	    qBusinessPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, taxId));
	    qBusinessPartner.setMaxResults(1);
	    if (qBusinessPartner.list().size() > 0) {
	      throw new OBException("Business partner with taxId [" + taxId + "] is already registered");
	    }

	    BusinessPartner businessPartner = OBProvider.getInstance().get(BusinessPartner.class);
	    businessPartner.setCustomer(isCustomer);
	    businessPartner.setVendor(isVendor);
	    businessPartner.setEmployee(isEmployee);
	    businessPartner.setSalesRepresentative(isSalesRepresentative);

	    businessPartner.setSearchKey(taxId);
	    businessPartner.setTaxID(taxId);
	    businessPartner.setBusinessPartnerCategory(config.getBusinessPartnerCategory());
	    businessPartner.setPriceList(config.getPriceList());
	    businessPartner.setPaymentMethod(config.getPaymentMethod());
	    businessPartner.setPaymentTerms(config.getPaymentTerms());

	    boolean isTaxIdValid = false;
	    switch (taxIdType) {
	    case "D":
	      // isTaxIdValid = Scactu_ValidateTaxId.validarCedula(taxId);
	      isTaxIdValid = taxId.length() == 10;
	      break;
	    case "R":
	      // isTaxIdValid = Scactu_ValidateTaxId.validarRucPersonaNatural(taxId) ||
	      // Scactu_ValidateTaxId.validarRucSociedadPrivada(taxId);
	      isTaxIdValid = taxId.length() == 13;
	      String suffix = taxId.substring(10, 13);
	      if (!suffix.equals("001")) {
	        String prefix = taxId.toString().substring(0, 10);
	        businessPartner.setTaxID(prefix + "001");
	      }
	      break;
	    case "P":
	      isTaxIdValid = true;
	      break;
	    }
	    if (!isTaxIdValid) {
	      throw new OBException("taxId [" + taxId + "] is not valid");
	    }
	    businessPartner.setSswhTaxidtype(taxIdType);

	    businessPartner.setName(fiscalName);
	    businessPartner.setName2(" "); // espacion en blanco por que ya existe un trigger de concatenacion
	    businessPartner.setEEIEeioice(true);
	    businessPartner.setEEIEmail(email);
	    businessPartner.setCreditLimit(new BigDecimal(0));
	    businessPartner.setURL(url);
	    
	    if (taxPayer.trim().length() == 0) {
	    	throw new OBException("TaxPayer [" + taxPayer + "] not found");
	    }
	    
	    businessPartner.setSsscrbpTypeOfTaxpayer(taxPayer);
	    
	    businessPartner.setSsscrbpName(name1);
	    businessPartner.setSsscrbpName2(name2);
	    
	    String lname1 = null, lname2 = null;
	    if (taxPayer.trim().equals("NP")) {
	    	if (SHPPWS_Helper.hasParam(body, "lastName1") && SHPPWS_Helper.hasParam(body, "lastName2")) {
	    		if (lastName1.trim().length() == 0 || lastName2.trim().length() == 0) {
	    			throw new OBException("lastName1, lastName2 not found");
	    		}
	    	} else {
	    		throw new OBException("lastName1, lastName2 not found");
	    	}
	    	lname1 = lastName1;
	    	lname2 = lastName2;
	    }
	    
	    businessPartner.setSSSCRBPLastname(lname1);
	    businessPartner.setSsscrbpLastname2(lname2);
	    
	    Taxpayer qTaxPayer = null;
	    if (SHPPWS_Helper.hasParam(body, "typeTaxPayerId")) {
	      qTaxPayer = OBDal.getInstance().get(Taxpayer.class, typeTaxPayerId);

	      if (qTaxPayer == null) {
	        throw new OBException("typeTaxPayerId [" + typeTaxPayerId + "] not found");
	      }
	    }

	    businessPartner.setSSWHTaxpayer(qTaxPayer);

	    OBDal.getInstance().save(businessPartner);
	    OBDal.getInstance().flush();

	    body.put("id", businessPartner.getId());

	    JSONArray addresses;
	    try {
	      addresses = body.has("addresses") ? body.getJSONArray("addresses") : new JSONArray();
	    } catch (Exception e) {
	      throw new OBException("addresses should be an array");
	    }
	    // if (addresses.length() == 0) {
	    // throw new OBException("addresses is required");
	    // }

	    for (int i = 0; i < addresses.length(); i++) {
	      JSONObject address = addresses.getJSONObject(i);
	      Location businessPartnerLocation = createBusinessPartnerLocation(address, businessPartner);
	      address.put("id", businessPartnerLocation.getId());
	      addresses.put(i, address);
	    }
	    body.put("addresses", addresses);

	    JSONArray contactPersons;
	    try {
	      contactPersons = body.has("contactPersons") ? body.getJSONArray("contactPersons")
	          : new JSONArray();
	    } catch (Exception e) {
	      throw new OBException("contactPersons should be an array");
	    }

	    for (int i = 0; i < contactPersons.length(); i++) {
	      JSONObject contactPerson = contactPersons.getJSONObject(i);
	      User user = createContactPerson(contactPerson, businessPartner);
	      contactPerson.put("id", user.getId());
	      contactPersons.put(i, contactPerson);
	    }
	    body.put("contactPersons", contactPersons);
	    logger.info(
	        "BusinessPartner created: " + businessPartner.getTaxID() + "-" + businessPartner.getName());
	    return businessPartner;
	  }

	  public BusinessPartner updateBusinessPartner(ConnectionProvider conn, JSONObject body)
	      throws Exception {
	    String taxId = SHPPWS_Helper.getString(body, "taxId", true);
	    String fiscalName = SHPPWS_Helper.getString(body, "socialName", true);
	    String commercialName = SHPPWS_Helper.getString(body, "comercialName", true);
	    String email = SHPPWS_Helper.getString(body, "email", true);
	    String typeTaxPayerId = SHPPWS_Helper.getString(body, "typeTaxPayerId", false);
	    String url = SHPPWS_Helper.getString(body, "url", false);

	    OBCriteria<BusinessPartner> qBusinessPartner = OBDal.getInstance()
	        .createCriteria(BusinessPartner.class);
	    qBusinessPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, taxId));
	    qBusinessPartner.setMaxResults(1);
	    if (qBusinessPartner.list().size() == 0) {
	      throw new OBException("Business partner taxId [" + taxId + "] not found");
	    }
	    BusinessPartner businessPartner = (BusinessPartner) qBusinessPartner.uniqueResult();
	    body.put("id", businessPartner.getId());

	    businessPartner.setCustomer(true);
	    businessPartner.setName(fiscalName);
	    businessPartner.setName2(commercialName);
	    businessPartner.setEEIEeioice(true);
	    businessPartner.setEEIEmail(email);
	    businessPartner.setURL(url);

	    if (SHPPWS_Helper.hasParam(body, "typeTaxPayerId")) {
	      Taxpayer qTaxPayer = OBDal.getInstance().get(Taxpayer.class, typeTaxPayerId);
	      if (qTaxPayer == null) {
	        throw new OBException("typeTaxPayerId [" + typeTaxPayerId + "] not found");
	      }
	      businessPartner.setSSWHTaxpayer(qTaxPayer);
	    }

	    OBDal.getInstance().save(businessPartner);
	    OBDal.getInstance().flush();

	    JSONArray addresses;
	    try {
	      addresses = body.has("addresses") ? body.getJSONArray("addresses") : new JSONArray();
	    } catch (Exception e) {
	      throw new OBException("addresses should be an array");
	    }

	    for (int i = 0; i < addresses.length(); i++) {
	      JSONObject address = addresses.getJSONObject(i);

	      OBCriteria<Location> qBusinessPartnerLocation = OBDal.getInstance()
	          .createCriteria(Location.class);
	      qBusinessPartnerLocation
	          .add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, businessPartner));
	      qBusinessPartnerLocation.setMaxResults(1);

	      Location businessPartnerLocation = null;
	      if (qBusinessPartnerLocation.list().size() == 0) {
	        businessPartnerLocation = createBusinessPartnerLocation(address, businessPartner);
	        address.put("id", businessPartnerLocation.getId());
	        addresses.put(i, address);
	      } else {
	        businessPartnerLocation = updateBusinessPartnerLocation(address, businessPartner);
	      }
	    }
	    body.put("addresses", addresses);

	    JSONArray contactPersons;
	    try {
	      contactPersons = body.has("contactPersons") ? body.getJSONArray("contactPersons")
	          : new JSONArray();
	    } catch (Exception e) {
	      throw new OBException("contactPersons should be an array");
	    }

	    // Eliminar o desactivar contactos
	    OBCriteria<User> qUserList = OBDal.getInstance().createCriteria(User.class);
	    qUserList.add(Restrictions.eq(User.PROPERTY_BUSINESSPARTNER, businessPartner));
	    List<User> userList = qUserList.list();
	    for (User user : userList) {
	      JSONObject userFound = null;
	      for (int i = 0; i < contactPersons.length(); i++) {
	        JSONObject userSearch = contactPersons.getJSONObject(i);
	        String userEmail = SHPPWS_Helper.getString(userSearch, "email", true);
	        if (user.getEmail() != null
	            && user.getEmail().toLowerCase().trim().equals(userEmail.toLowerCase())) {
	          userFound = userSearch;
	          i = contactPersons.length();
	        }
	      }

	      if (userFound == null) {
	        user.setActive(false);
	        OBDal.getInstance().save(user);
	        OBDal.getInstance().flush();
	      }
	    }

	    for (int i = 0; i < contactPersons.length(); i++) {
	      JSONObject contactPerson = contactPersons.getJSONObject(i);
	      String contactPersonEmail = SHPPWS_Helper.getString(contactPerson, "email", false);

	      OBCriteria<User> qUser = OBDal.getInstance().createCriteria(User.class);
	      qUser.add(Restrictions.eq(User.PROPERTY_BUSINESSPARTNER, businessPartner));
	      qUser.add(Restrictions.sqlRestriction("UPPER(TRIM(email)) = ?",
	          contactPersonEmail.toUpperCase(), StringType.INSTANCE));
	      qUser.setMaxResults(1);
	      qUser.setFilterOnActive(false);

	      User user = null;
	      if (qUser.list().size() == 0) {
	        user = createContactPerson(contactPerson, businessPartner);
	        contactPerson.put("id", user.getId());
	        contactPersons.put(i, contactPerson);
	      } else {
	        updateContactPerson(contactPerson, businessPartner);
	      }
	    }
	    body.put("contactPersons", contactPersons);
	    logger.info(
	        "BusinessPartner updated: " + businessPartner.getTaxID() + "-" + businessPartner.getName());
	    return businessPartner;
	  }

	  private Location createBusinessPartnerLocation(JSONObject address,
	      BusinessPartner businessPartner) throws Exception {
	    String mainAddress = SHPPWS_Helper.getString(address, "address", true);
	    String reference = SHPPWS_Helper.getString(address, "reference", false);
	    String countryCode = SHPPWS_Helper.getString(address, "countryCode", true).toUpperCase();
	    String province = SHPPWS_Helper.getString(address, "province", true);
	    String canton = SHPPWS_Helper.getString(address, "canton", true);
	    boolean isShipping = SHPPWS_Helper.getBoolean(address, "isShipping", true);
	    boolean isBilling = SHPPWS_Helper.getBoolean(address, "isBilling", true);
	    String phoneNumber = SHPPWS_Helper.getString(address, "phoneNumber", false);
	    String alternativePhoneNumber = SHPPWS_Helper.getString(address, "alternativePhoneNumber",
	        false);

	    String address1 = null;
	    String address2 = null;
	    if (mainAddress.length() > 60) {
	      address1 = mainAddress.substring(0, 60);
	      address2 = mainAddress.substring(60, mainAddress.length() > 120 ? 120 : mainAddress.length());
	    } else {
	      address1 = mainAddress;
	      address2 = reference;
	    }

	    org.openbravo.model.common.geography.Location location = OBProvider.getInstance()
	        .get(org.openbravo.model.common.geography.Location.class);
	    location.setAddressLine1(address1);
	    location.setAddressLine2(address2);

	    OBCriteria<Country> qCountry = OBDal.getInstance().createCriteria(Country.class);
	    qCountry.add(Restrictions.eq(Country.PROPERTY_ACTIVE, true));
	    qCountry.add(Restrictions.eq(Country.PROPERTY_ISOCOUNTRYCODE, countryCode));
	    qCountry.setMaxResults(1);
	    if (qCountry.list().size() == 0) {
	      throw new OBException("countryCode [" + countryCode + "] not found");
	    }
	    location.setCountry(qCountry.list().get(0));

	    if (province != null) {
	      OBCriteria<Region> qRegion = OBDal.getInstance().createCriteria(Region.class);
	      qRegion.add(Restrictions.eq(Region.PROPERTY_ACTIVE, true));
	      qRegion.add(Restrictions.eq(Region.PROPERTY_SEARCHKEY, province));
	      qRegion.setMaxResults(1);
	      if (qRegion.list().size() == 0) {
	        throw new OBException("province [" + province + "] not found");
	      }
	      location.setRegion(qRegion.list().get(0));
	    }

	    if (canton != null) {
	      OBCriteria<secpm_canton> qCanton = OBDal.getInstance().createCriteria(secpm_canton.class);
	      qCanton.add(Restrictions.eq(secpm_canton.PROPERTY_ACTIVE, true));
	      qCanton.add(Restrictions.eq(secpm_canton.PROPERTY_IDENTIFICADOR, canton));
	      qCanton.setMaxResults(1);
	      if (qCanton.list().size() == 0) {
	        throw new OBException("canton [" + canton + "] not found");
	      }
	      secpm_canton _canton = qCanton.list().get(0);
	      // location.setSdinccCanton(_canton);
	      location.setCityName(_canton.getName());
	    }

	    OBDal.getInstance().save(location);
	    OBDal.getInstance().flush();

	    Location businessPartnerLocation = OBProvider.getInstance().get(Location.class);
	    businessPartnerLocation.setBusinessPartner(businessPartner);
	    businessPartnerLocation.setLocationAddress(location);
	    businessPartnerLocation.setName(address1);
	    businessPartnerLocation.setShipToAddress(isShipping);
	    businessPartnerLocation.setInvoiceToAddress(isBilling);
	    businessPartnerLocation.setPhone(phoneNumber);
	    businessPartnerLocation.setAlternativePhone(alternativePhoneNumber);
	    OBDal.getInstance().save(businessPartnerLocation);
	    OBDal.getInstance().flush();

	    return businessPartnerLocation;
	  }

	  private Location updateBusinessPartnerLocation(JSONObject address,
	      BusinessPartner businessPartner) throws Exception {
	    String mainAddress = SHPPWS_Helper.getString(address, "address", true);
	    String reference = SHPPWS_Helper.getString(address, "reference", false);
	    String countryCode = SHPPWS_Helper.getString(address, "countryCode", true).toUpperCase();
	    String province = SHPPWS_Helper.getString(address, "province", true);
	    String canton = SHPPWS_Helper.getString(address, "canton", true);
	    boolean isShipping = SHPPWS_Helper.getBoolean(address, "isShipping", true);
	    boolean isBilling = SHPPWS_Helper.getBoolean(address, "isBilling", true);
	    String phoneNumber = SHPPWS_Helper.getString(address, "phoneNumber", false);
	    String alternativePhoneNumber = SHPPWS_Helper.getString(address, "alternativePhoneNumber",
	        false);

	    String address1 = null;
	    String address2 = null;
	    if (mainAddress.length() > 60) {
	      address1 = mainAddress.substring(0, 60);
	      address2 = mainAddress.substring(60, mainAddress.length() > 120 ? 120 : mainAddress.length());
	    } else {
	      address1 = mainAddress;
	      address2 = reference;
	    }

	    OBCriteria<Location> qBusinessPartnerLocation = OBDal.getInstance()
	        .createCriteria(Location.class);
	    qBusinessPartnerLocation
	        .add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, businessPartner));
	    qBusinessPartnerLocation.setMaxResults(1);
	    if (qBusinessPartnerLocation.list().size() == 0) {
	      return createBusinessPartnerLocation(address, businessPartner);
	    }
	    Location businessPartnerLocation = (Location) qBusinessPartnerLocation.uniqueResult();

	    org.openbravo.model.common.geography.Location location = businessPartnerLocation
	        .getLocationAddress();
	    location.setAddressLine1(address1);
	    location.setAddressLine2(address2);

	    OBCriteria<Country> qCountry = OBDal.getInstance().createCriteria(Country.class);
	    qCountry.add(Restrictions.eq(Country.PROPERTY_ACTIVE, true));
	    qCountry.add(Restrictions.eq(Country.PROPERTY_ISOCOUNTRYCODE, countryCode));
	    qCountry.setMaxResults(1);
	    if (qCountry.list().size() == 0) {
	      throw new OBException("countryCode [" + countryCode + "] not found");
	    }
	    location.setCountry(qCountry.list().get(0));

	    if (province != null) {
	      OBCriteria<Region> qRegion = OBDal.getInstance().createCriteria(Region.class);
	      qRegion.add(Restrictions.eq(Region.PROPERTY_ACTIVE, true));
	      qRegion.add(Restrictions.eq(Region.PROPERTY_SEARCHKEY, province));
	      qRegion.setMaxResults(1);
	      if (qRegion.list().size() == 0) {
	        throw new OBException("province [" + province + "] not found");
	      }
	      location.setRegion(qRegion.list().get(0));
	    }

	    if (canton != null) {
	      OBCriteria<secpm_canton> qCanton = OBDal.getInstance().createCriteria(secpm_canton.class);
	      qCanton.add(Restrictions.eq(secpm_canton.PROPERTY_ACTIVE, true));
	      qCanton.add(Restrictions.eq(secpm_canton.PROPERTY_IDENTIFICADOR, canton));
	      qCanton.setMaxResults(1);
	      if (qCanton.list().size() == 0) {
	        throw new OBException("canton [" + canton + "] not found");
	      }
	      secpm_canton _canton = qCanton.list().get(0);
	      // location.setSdinccCanton(_canton);
	      location.setCityName(_canton.getName());
	    }

	    OBDal.getInstance().save(location);
	    OBDal.getInstance().flush();

	    businessPartnerLocation.setLocationAddress(location);
	    businessPartnerLocation.setName(address1);
	    businessPartnerLocation.setShipToAddress(isShipping);
	    businessPartnerLocation.setInvoiceToAddress(isBilling);
	    businessPartnerLocation.setPhone(phoneNumber);
	    businessPartnerLocation.setAlternativePhone(alternativePhoneNumber);

	    OBDal.getInstance().save(businessPartnerLocation);
	    OBDal.getInstance().flush();

	    return businessPartnerLocation;
	  }

	  private User createContactPerson(JSONObject contactPerson, BusinessPartner businessPartner)
	      throws Exception {
	    String firstname = SHPPWS_Helper.getString(contactPerson, "firstname", true);
	    String lastname = SHPPWS_Helper.getString(contactPerson, "lastname", false);
	    lastname = lastname != null ? lastname : "";
	    String email = SHPPWS_Helper.getString(contactPerson, "email", true);
	    String phoneNumber = SHPPWS_Helper.getString(contactPerson, "phoneNumber", false);
	    String alternativePhoneNumber = SHPPWS_Helper.getString(contactPerson, "alternativePhoneNumber",
	        false);
	    String position = SHPPWS_Helper.getString(contactPerson, "position", false);
	    String comments = SHPPWS_Helper.getString(contactPerson, "comments", false);

	    User user = OBProvider.getInstance().get(User.class);
	    user.setBusinessPartner(businessPartner);
	    user.setName(firstname + " " + lastname);
	    user.setFirstName(firstname);
	    user.setLastName(lastname);
	    user.setEmail(email);
	    user.setPhone(phoneNumber);
	    user.setAlternativePhone(alternativePhoneNumber);
	    user.setPosition(position);
	    user.setComments(comments);

	    OBDal.getInstance().save(user);
	    OBDal.getInstance().flush();

	    return user;
	  }

	  private User updateContactPerson(JSONObject contactPerson, BusinessPartner businessPartner)
	      throws Exception {
	    String firstname = SHPPWS_Helper.getString(contactPerson, "firstname", true);
	    String lastname = SHPPWS_Helper.getString(contactPerson, "lastname", false);
	    lastname = lastname != null ? lastname : "";
	    String email = SHPPWS_Helper.getString(contactPerson, "email", true);
	    String phoneNumber = SHPPWS_Helper.getString(contactPerson, "phoneNumber", false);
	    String alternativePhoneNumber = SHPPWS_Helper.getString(contactPerson, "alternativePhoneNumber",
	        false);
	    String position = SHPPWS_Helper.getString(contactPerson, "position", false);
	    String comments = SHPPWS_Helper.getString(contactPerson, "comments", false);

	    OBCriteria<User> qUser = OBDal.getInstance().createCriteria(User.class);
	    qUser.add(Restrictions.eq(User.PROPERTY_BUSINESSPARTNER, businessPartner));
	    qUser.add(Restrictions.sqlRestriction("UPPER(TRIM(email)) = ?", email.toUpperCase(),
	        StringType.INSTANCE));
	    qUser.setMaxResults(1);
	    qUser.setFilterOnActive(false);
	    if (qUser.list().size() == 0) {
	      return createContactPerson(contactPerson, businessPartner);
	    }
	    User user = qUser.list().get(0);

	    user.setActive(true);
	    user.setName(firstname + " " + lastname);
	    user.setFirstName(firstname);
	    user.setLastName(lastname);
	    user.setEmail(email);
	    user.setPhone(phoneNumber);
	    user.setAlternativePhone(alternativePhoneNumber);
	    user.setPosition(position);
	    user.setComments(comments);

	    OBDal.getInstance().save(user);
	    OBDal.getInstance().flush();

	    return user;
	  }

	  public void getBusinessPartner(String taxId, JSONObject body) throws Exception {
	    OBCriteria<BusinessPartner> qBPartner = OBDal.getInstance()
	        .createCriteria(BusinessPartner.class);
	    qBPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_ACTIVE, true));
	    qBPartner.add(Restrictions.eq(BusinessPartner.PROPERTY_SEARCHKEY, taxId.trim()));
	    qBPartner.setMaxResults(1);
	    if (qBPartner.list().size() == 0) {
	      throw new OBException("taxId [" + taxId + "] not found");
	    }
	    BusinessPartner bPartner = qBPartner.list().get(0);
	    logger.info("BusinessPartner found with taxId: " + taxId + "-" + bPartner.getName());

	    body.put("id", bPartner.getId());
	    body.put("taxId", bPartner.getSearchKey());
	    body.put("taxIdType", bPartner.getSswhTaxidtype());
	    body.put("socialName", bPartner.getName());
	    body.put("comercialName", bPartner.getName2());
	    body.put("email", bPartner.getEEIEmail() != null ? bPartner.getEEIEmail() : "");
	    body.put("typeTaxPayerId",
	        bPartner.getSSWHTaxpayer() != null ? bPartner.getSSWHTaxpayer().getId() : "");

	    JSONArray addresses = new JSONArray();
	    for (Location bPartnerLocation : bPartner.getBusinessPartnerLocationList()) {
	      org.openbravo.model.common.geography.Location location = bPartnerLocation
	          .getLocationAddress();

	      JSONObject address = new JSONObject();
	      address.put("id", bPartnerLocation.getId());
	      address.put("address", location.getAddressLine1() != null ? location.getAddressLine1() : "");
	      address.put("reference",
	          location.getAddressLine2() != null ? location.getAddressLine2() : "");
	      address.put("countryCode",
	          location.getCountry() != null ? location.getCountry().getISOCountryCode() : "");
	      address.put("province",
	          location.getRegion() != null ? location.getRegion().getSearchKey() : "");
	      address.put("isShipping", bPartnerLocation.isShipToAddress());
	      address.put("isBilling", bPartnerLocation.isInvoiceToAddress());
	      address.put("phoneNumber",
	          bPartnerLocation.getPhone() != null ? bPartnerLocation.getPhone() : "");
	      address.put("alternativePhoneNumber",
	          bPartnerLocation.getAlternativePhone() != null ? bPartnerLocation.getAlternativePhone()
	              : "");
	      addresses.put(address);
	    }
	    body.put("addresses", addresses);

	    JSONArray contactPersons = new JSONArray();
	    for (User user : bPartner.getADUserList()) {
	      if (user.isActive()) {
	        JSONObject contact = new JSONObject();
	        contact.put("id", user.getId());
	        contact.put("firstname", user.getFirstName() != null ? user.getFirstName() : "");
	        contact.put("lastname", user.getLastName() != null ? user.getLastName() : "");
	        contact.put("email", user.getEmail() != null ? user.getEmail() : "");
	        contact.put("phoneNumber", user.getPhone() != null ? user.getPhone() : "");
	        contact.put("alternativePhoneNumber",
	            user.getAlternativePhone() != null ? user.getAlternativePhone() : "");
	        contact.put("position", user.getPosition() != null ? user.getPosition() : "");
	        contact.put("comments", user.getComments() != null ? user.getComments() : "");
	        contactPersons.put(contact);
	      }
	    }
	    body.put("contactPersons", contactPersons);
	  }
	}