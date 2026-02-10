package ec.com.sidesoft.happypay.web.services.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.shppws_monitor;
import ec.com.sidesoft.happypay.web.services.service.log_records;

public class MonitorManager {
	
	public void sendMonitorData(JSONObject jsonMonitor, shppws_config accesApi, Boolean apiService, JSONArray data) throws Exception {
		if(accesApi == null) {
			OBCriteria<shppws_config> queryApi= OBDal.getInstance().createCriteria(shppws_config.class);
			List<shppws_config> listAccesApi = queryApi.list();
			if(listAccesApi.size() > 0) {
				accesApi = listAccesApi.get(0);
			}
		}
		
		ResultJSON dataCustom = null;
		ProccesDataMonitor dataClass = new ProccesDataMonitor();
		if(accesApi != null && apiService) {
			dataCustom= dataClass.processDataMonitor(jsonMonitor, accesApi);
			data = dataCustom.getDataJSONArray();
		}
		
		if(accesApi != null && data.length() > 0) {
			log_records logger = new log_records();
			String	apiUrl = "";
			String	apiEndPoint = "";
			String	apiTypeAuth = "";
			String	apiUser = "";
			String	apiPass = "";
			String	apiToken = "";
			
			String Interface = "";
			String Process = "";
			if(dataCustom.getDataTypeOfMonitor().equals("Pagos") || dataCustom.getDataTypeOfMonitor().equals("Reverso")) {
				apiUrl = accesApi.getMonitorpayNamespace();
				apiEndPoint = accesApi.getMonitorpayReadEndpoint();
				apiTypeAuth = accesApi.getMonitorpayTypeAuth();
				apiUser = accesApi.getMonitorpayUser();
				apiPass = accesApi.getMonitorpayPass();
				apiToken = accesApi.getMonitorpayToken();
				Interface = "SHPPWS_MonitorP";
				Process = "Monitor de Pagos";
			}else {
				apiUrl = accesApi.getMonitorsrvNamespace();
				apiEndPoint = accesApi.getMonitorsrvReadEndpoint();
				apiTypeAuth = accesApi.getMonitorsrvTypeAuth();
				apiUser = accesApi.getMonitorsrvUser();
				apiPass = accesApi.getMonitorsrvPass();
				apiToken = accesApi.getMonitorsrvToken();
				Interface = "SHPPWS_MonitorC";
				Process = "Monitor de Comunicación";
			}
			Scactu_Log log = logger.log_start_register(accesApi, apiEndPoint, data.toString());
				
			int responseCode = 500;
			HttpURLConnection connectionhttp = null;
			HttpsURLConnection connectionhttps = null;
			HttpURLConnection connection = null;
			JSONObject requestBody = new JSONObject();
			String apiResponse = "";

			if (apiTypeAuth.equals("BA")) {
				URL url = new URL(apiUrl + apiEndPoint);
				connectionhttp = (HttpURLConnection) url.openConnection();
				connectionhttp.setRequestMethod("GET");
				String username = apiUser;
				String password = apiPass;
				String authString = username + ":" + password;
				String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
				connectionhttp.setRequestProperty("Authorization", authHeaderValue);

				responseCode = connectionhttp.getResponseCode();
				connection = connectionhttp;
			} else if (apiTypeAuth.equals("AT")) {
				// Deshabilitar la validación de certificados SSL --> Temporal
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
				} };

				// Configurar SSLContext con la configuración personalizada
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
				// Obtener la conexión HTTPS y aplicar la configuración personalizada
				URL url = new URL(apiUrl + apiEndPoint);
				connectionhttps = (HttpsURLConnection) url.openConnection();
				// Desactivar la verificación estricta del nombre del host
				connectionhttps.setHostnameVerifier((hostname, session) -> true);
				connectionhttps.setSSLSocketFactory(sslContext.getSocketFactory());
				connectionhttps.setRequestMethod("POST");

				String authHeaderValue = "Bearer " + apiToken;
				connectionhttps.setRequestProperty("Authorization", authHeaderValue);

				connectionhttps.setRequestProperty("Content-Type", "application/json");

				connectionhttps.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(connectionhttps.getOutputStream());
				writer.write(requestBody.toString());
				writer.flush();
				writer.close();
				
				responseCode = connectionhttps.getResponseCode();
				connection = connectionhttps;
			} else if (apiTypeAuth.equals("OA")) { //OAuth 2.0
				String tokenUrl = accesApi.getMonitorsrvParams();
				String clientId = accesApi.getMonitorsrvUser();
				String clientSecret = accesApi.getMonitorsrvPass();
				String scope = "api1";

				try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
					String oauthToken = getOAuth2Token(tokenUrl, clientId, clientSecret, scope);

					if (oauthToken != null) {
						HttpPost httpPost = new HttpPost(apiUrl + apiEndPoint);
						//fillJSON
						//requestBody.put("field1", "hi");
						//Body
						StringEntity entity = null;
						if(dataCustom.getDataTypeOfMonitor().equals("Pagos") || dataCustom.getDataTypeOfMonitor().equals("Reverso")) {
							entity = new StringEntity(data.get(0).toString(), "UTF-8");
						}else {
							entity = new StringEntity(data.toString(), "UTF-8");
						}
						entity.setContentType("application/json; charset=UTF-8");
						httpPost.setEntity(entity);
						//Authorization
						httpPost.setHeader("Authorization", "Bearer " + oauthToken);

						try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
							responseCode = response.getStatusLine().getStatusCode();
							HttpEntity responseEntity = response.getEntity();
							apiResponse = EntityUtils.toString(responseEntity);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			JSONObject json = data.getJSONObject(0);
			String noReference = "";			
			if(json.has("opportunityNumber") && json.getString("opportunityNumber") != null &&!json.getString("opportunityNumber").equals("")) {
				noReference = json.getString("opportunityNumber");
			}else if(json.has("customerIdentificacion") && json.getString("customerIdentificacion") != null && !json.getString("customerIdentificacion").equals("")) {
				noReference = json.getString("customerIdentificacion");
			}
			
			if(dataCustom != null && dataCustom.getDataTypeOfMonitor() != null) {
				
			}
			String Error = "";

			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 202) {
				logger.log_end_register(log, apiUrl, noReference,apiResponse, "OK", "OUT", Interface, Process, noReference, Error);
			} else {
				logger.log_end_register(log, apiUrl, noReference,"Response Code:"+responseCode+" "+apiResponse, "ERROR", "OUT", Interface, Process, noReference, Error);
			}
		}
	}
	
	
	
	public String getOAuth2Token( String baseUrl, String clientId, String clientSecret, String scope) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
	        // New Post
	        HttpPost httpPost = new HttpPost(baseUrl);

	        // Request Body
	        List<NameValuePair> params = new ArrayList<>();
	        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
	        params.add(new BasicNameValuePair("client_id", clientId));
	        params.add(new BasicNameValuePair("client_secret", clientSecret));
	        params.add(new BasicNameValuePair("scope", scope));
	        httpPost.setEntity(new UrlEncodedFormEntity(params));

	        HttpResponse response = httpClient.execute(httpPost);
	        HttpEntity entity = response.getEntity();

	        //  Get token if code = 200
	        if (response.getStatusLine().getStatusCode() == 200) {
	        	String responseBody = EntityUtils.toString(entity);
	            Gson gson = new Gson();
	            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
	            String accessToken = jsonResponse.get("access_token").getAsString();
	            return accessToken;
	        } else {
	            return "";
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
    }

}
