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
	 * 入口
	 */
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		
		// 请求消息
		if (messageIsRequest) {
			byte[] request = messageInfo.getRequest();
			IRequestInfo requestInfo = helpers.analyzeRequest( messageInfo.getHttpService(), request);
			List<String> headers = requestInfo.getHeaders();
			List<IParameter> params = requestInfo.getParameters();
			DataBean data = new DataBean();
			StringBuffer raw = new StringBuffer();
			//获取请求头
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
			//获取参数 JsonObject
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
		} else {// 响应消息
//			byte[] response = messageInfo.getResponse(); // 取得响应消息
//			IResponseInfo analyzedResponse = helpers.analyzeResponse(response); // 解析响应消息
//			List<String> headers = analyzedResponse.getHeaders(); // 取得响应头
//			for (String header : headers) {
//				//System.out.println(header);
//			}
//			String resp = new String(messageInfo.getResponse()); // 把响应包转换成字符串
//			// 截取出body字符串
//			int bodyOffset = analyzedResponse.getBodyOffset();
//			String body = resp.substring(bodyOffset);
//			// body = unicodeToString(body);
//			// byte[] bodybyte = body.getBytes(); ////body还原成字节码
//			// messageInfo.setResponse(helpers.buildHttpMessage(headers, bodybyte));
//			// //构建响应消息并更新
		}
	}

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stdout.println(Msg);
		helpers = callbacks.getHelpers();
		// 设置拓展名
		callbacks.setExtensionName(ExtenderName);
		// 注册HttpListener,处理请求和响应
		callbacks.registerHttpListener(this);
	}
}