package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import db.Redis;

/**
 * API DOC: https://portswigger.net/burp/extender/api/
 * @author max
 *
 */
public class BurpExtender implements IBurpExtender, IHttpListener {
	private IExtensionHelpers helpers;
	private PrintWriter stdout;
	private Redis redis = new Redis();
	private String ExtenderName = "TransparentCap\n";
	private String Msg = "Register TransparentCap Success\n" + redis.ping();
	/**
	 * ���
	 */
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		
		// ������Ϣ
		if (messageIsRequest) {
			byte[] request = messageInfo.getRequest();
			IRequestInfo requestInfo = helpers.analyzeRequest( messageInfo.getHttpService(), request);
			List<String> headers = requestInfo.getHeaders();
			List<IParameter> params = requestInfo.getParameters();
			DataBean data = new DataBean();
			StringBuffer raw = new StringBuffer();
			//��ȡ����ͷ
			JsonObjectBuilder headerJson = Json.createObjectBuilder();
			for (int i = 1; i < headers.size(); i++) {
				raw.append(headers.get(i));
				raw.append("\n");
				try {
					String[] s = headers.get(i).split(": ");
					headerJson.add(s[0], s[1]);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			//��ȡ���� JsonObject
			JsonObjectBuilder paramsJson = Json.createObjectBuilder();
			for (IParameter param : params) {
				paramsJson.add(param.getName(), param.getName());
			}
			
			data.setMethod(requestInfo.getMethod());
			data.setHeaders(headerJson.build());
			data.setParams(paramsJson.build());
			data.setRaw(raw.toString());
			URL url = requestInfo.getUrl();
			data.setUrl(url.toString());
			redis.set(data.getKey(), data.encodeBase64());
		} else {// ��Ӧ��Ϣ
//			byte[] response = messageInfo.getResponse(); // ȡ����Ӧ��Ϣ
//			IResponseInfo analyzedResponse = helpers.analyzeResponse(response); // ������Ӧ��Ϣ
//			List<String> headers = analyzedResponse.getHeaders(); // ȡ����Ӧͷ
//			for (String header : headers) {
//				//System.out.println(header);
//			}
//			String resp = new String(messageInfo.getResponse()); // ����Ӧ��ת�����ַ���
//			// ��ȡ��body�ַ���
//			int bodyOffset = analyzedResponse.getBodyOffset();
//			String body = resp.substring(bodyOffset);
//			// body = unicodeToString(body);
//			// byte[] bodybyte = body.getBytes(); ////body��ԭ���ֽ���
//			// messageInfo.setResponse(helpers.buildHttpMessage(headers, bodybyte));
//			// //������Ӧ��Ϣ������
		}
	}

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stdout.println(Msg);
		helpers = callbacks.getHelpers();
		// ������չ��
		callbacks.setExtensionName(ExtenderName);
		// ע��HttpListener,�����������Ӧ
		callbacks.registerHttpListener(this);
	}
}