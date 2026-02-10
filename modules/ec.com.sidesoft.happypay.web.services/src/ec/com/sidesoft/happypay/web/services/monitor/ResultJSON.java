package ec.com.sidesoft.happypay.web.services.monitor;

import org.codehaus.jettison.json.JSONArray;

public class ResultJSON {
	
	private JSONArray dataJSONArray;
	private String dataTypeOfService;
	
	public ResultJSON(JSONArray dataJSONArray, String dataTypeOfMonitor) {
		this.setDataTypeOfMonitor(dataTypeOfMonitor);
		this.setDataJSONArray(dataJSONArray);
	}

	/**
	 * @return the dataTypeOfMonitor
	 */
	public String getDataTypeOfMonitor() {
		return dataTypeOfService;
	}

	/**
	 * @param dataTypeOfMonitor the dataTypeOfMonitor to set
	 */
	public void setDataTypeOfMonitor(String dataTypeOfService) {
		this.dataTypeOfService = dataTypeOfService;
	}

	/**
	 * @return the dataJSONArray
	 */
	public JSONArray getDataJSONArray() {
		return dataJSONArray;
	}

	/**
	 * @param dataJSONArray the dataJSONArray to set
	 */
	public void setDataJSONArray(JSONArray dataJSONArray) {
		this.dataJSONArray = dataJSONArray;
	}

}
