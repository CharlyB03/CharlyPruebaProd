package ec.com.sidesoft.happypay.web.services.monitor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.service.log_records;

public class BlacklistsEntry {

  public void BlacklistEntryApi(shppws_config accesApi, String CI, String Tlf, String docNo)
      throws Exception {
    log_records logger = new log_records();

    String Interface = "SHPPWS_BlackList_Entry";
    String Process = "Externo";
    String idRegister = "";
    String Error = "";

    String apiUrl = accesApi.getEntrylnNamespace();
    String apiEndPoint = accesApi.getEntrylnReadEndpoint();
    String apiTypeAuth = accesApi.getEntrylnTypeAuth();
    String apiUser = accesApi.getEntrylnUser();
    String apiPass = accesApi.getEntrylnPass();
    String apiToken = accesApi.getEntrylnToken();
    String nameService = "Recover";

    Scactu_Log log = logger.log_start_register(accesApi, apiEndPoint, null);
    int responseCode = 500;
    HttpURLConnection connectionhttp = null;
    HttpsURLConnection connectionhttps = null;
    HttpURLConnection connection = null;
    JSONObject requestBody = new JSONObject();

    String Reference = !CI.equals("") && !Tlf.equals("") ? CI + " - " + Tlf
        : !CI.equals("") ? CI : Tlf;

    // BA -> Basic auth
    // TA -> Token auth
    // OA -> Oauth2.0
    String apiResponse = "[{}]";
    Reference = docNo + "-" + Reference;
    try {
      // Config SSL
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

      if (apiTypeAuth.equals("BA")) {
        URL url = new URL(apiUrl + apiEndPoint);
        connectionhttp = (HttpURLConnection) url.openConnection();
        connectionhttp.setRequestMethod("GET");
        String username = apiUser;
        String password = apiPass;
        String authString = username + ":" + password;
        String authHeaderValue = "Basic "
            + Base64.getEncoder().encodeToString(authString.getBytes());
        connectionhttp.setRequestProperty("Authorization", authHeaderValue);

        responseCode = connectionhttp.getResponseCode();
        connection = connectionhttp;

      } else if (apiTypeAuth.equals("AT")) {

        // Configurar el cliente para ignorar validación SSL
        TrustManager[] trustAllCerts1 = new TrustManager[] { new X509TrustManager() {
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

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts1, new java.security.SecureRandom());

        // Crear una fábrica que ignore la verificación del nombre del host
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
            new javax.net.ssl.HostnameVerifier() {
              public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
                return true; // Acepta cualquier nombre de host
              }
            });

        // Crear el cliente HTTP ignorando validación SSL
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
            .build();

        HttpPost httpPost = new HttpPost(apiUrl + apiEndPoint);

        requestBody.put("Cedula_Cliente", CI);
        requestBody.put("Telefono_Cliente", Tlf);
        requestBody.put("Key", apiToken);
        log = logger.log_setValues(log, requestBody.toString());

        // Body
        StringEntity entity = new StringEntity(requestBody.toString());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        // Header Token (si se desea incluir)
        // httpPost.setHeader("Authorization", "Bearer " + apiToken);

        // Execute POST
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
          responseCode = response.getStatusLine().getStatusCode();
          HttpEntity responseEntity = response.getEntity();
          apiResponse = EntityUtils.toString(responseEntity);
        }

      } else if (apiTypeAuth.equals("OA")) {
        String baseUrl = accesApi.getEntrylnParams();
        String clientId = accesApi.getEntrylnUser();
        String clientSecret = accesApi.getEntrylnPass();
        String scope = "api1";
        MonitorManager mmanager = new MonitorManager();
        String oauthToken = mmanager.getOAuth2Token(baseUrl, clientId, clientSecret, scope);

        HttpPost httpPost = new HttpPost(apiUrl + apiEndPoint);

        requestBody.put("Cedula_Cliente", CI);
        requestBody.put("Telefono_Cliente", Tlf);
        requestBody.put("Key", apiToken);
        log = logger.log_setValues(log, requestBody.toString());

        // Body
        StringEntity entity = new StringEntity(requestBody.toString());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        // OAuth 2.0
        httpPost.setHeader("Authorization", "Bearer " + oauthToken);

        // Execute POST
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
          responseCode = response.getStatusLine().getStatusCode();
          HttpEntity responseEntity = response.getEntity();
          apiResponse = EntityUtils.toString(responseEntity);
        }

      }

      if (responseCode == HttpURLConnection.HTTP_OK) {
        String Status = "ERROR";
        if (apiResponse != null) {
          try {
            JSONArray responseArray = new JSONArray(apiResponse);
            JSONObject objResponse = responseArray.getJSONObject(0);
            String msgResponse = objResponse.getString("Estado");
            Status = msgResponse != null && !msgResponse.equals("") && msgResponse.equals("1")
                ? "OK"
                : Status;
          } catch (Exception e) {
          }
        }

        logger.log_end_register(log, apiUrl, Reference, apiResponse, Status, "OUT", Interface,
            Process, idRegister, Error);
      } else {
        Error = "Error en la consulta a la API " + nameService + " Código de respuesta: "
            + responseCode;
        logger.log_end_register(log, apiUrl, Reference, "Response Code " + responseCode, "ERROR",
            "OUT", Interface, Process, idRegister, Error);

      }
    } catch (Exception e) {
      Error = "Error en la consulta a la API " + nameService + " Código de respuesta: "
          + responseCode + " msg: " + e.getMessage() + " cause: " + e.getCause();
      if (logger != null) {
        logger.log_end_register(log, apiUrl, Reference, "Response Code " + responseCode, "ERROR",
            "OUT", Interface, Process, idRegister, Error);
      }
    }

  }

}
