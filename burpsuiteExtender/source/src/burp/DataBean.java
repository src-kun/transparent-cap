package burp;

import javax.json.Json;
import javax.json.JsonObject;


public class DataBean {

	private String method = null;
	private String url = null;
	private JsonObject headers = null;
	private String raw = null;
	private JsonObject params = null;

	public DataBean() {
		
	}
	
	public DataBean(String method, String url, JsonObject headers, String raw, JsonObject params) {
		super();
		this.method = method;
		this.url = url;
		this.headers = headers;
		this.raw = raw;
		this.params = params;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public JsonObject getParams() {
		return params;
	}

	public void setParams(JsonObject params) {
		this.params = params;
	}

	public JsonObject getHeaders() {
		return headers;
	}

	public void setHeaders(JsonObject headers) {
		this.headers = headers;
	}

	//method + setParams.toString() + url
	public String getKey() {
		StringBuffer s = new StringBuffer();
		s.append(this.method);
		s.append(this.params.toString());
		s.append(this.url);
		return Util.getMD5(s.toString());
	}
	

	/**
	 * {'request':{'method':'', 'url':'', 'headers':{}, 'params':{}, 'raw':''}
	 * API DOC: https://docs.oracle.com/javaee/7/api/javax/json/JsonObject.html
	 * 
	 */
	public String toString() {
		JsonObject requestJson = Json.createObjectBuilder()
				.add("request", Json.createObjectBuilder()
				        .add("headers", this.headers)
						.add("params", this.params)
						.add("url",  this.url)
						.add("method", this.method)
						.add("raw", this.raw).build()).build();
		return requestJson.toString();
	}
	
	/**
	 * base64 格式化后的字符串
	 * @return
	 */
	public String encodeBase64() {
		return Util.encodeBase64(this.toString());
	}


}
