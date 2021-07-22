import com.akulaku.orange.json.JsonUtil;
import com.akulaku.orange.security.HashUtil;
import com.akulaku.orange.web.ApiResult;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.silvrr.installment.core.config.AppConfig;
import com.silvrr.installment.core.config.EurekaRibbonConfig;
import com.silvrr.installment.core.config.SystemConfig;
import com.silvrr.installment.core.entity.Company;
import com.silvrr.installment.core.entity.SocialSecurityRequest;
import com.silvrr.installment.core.entity.TargetServiceAddress;
import com.silvrr.installment.core.utils.DbDataTypeUtil;
import com.silvrr.installment.core.utils.RibbonServerListSelector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xiaoweilin on 18-4-16.
 */
public class SnowflakeService {
	private static final Logger log = LoggerFactory.getLogger(SnowflakeService.class);
	private static final int CONN_TIMEOUT = 5 * 1000;
	private static final int CONN_REQ_TIMEOUT = 10 * 1000;
	private static final int SOCK_TIMEOUT = 10 * 1000;

	private static CloseableHttpClient client;

	static {
		initHttpClient();
	}

	private static void initHttpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(20);
		cm.setDefaultMaxPerRoute(20);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONN_TIMEOUT).setConnectionRequestTimeout(CONN_REQ_TIMEOUT).setSocketTimeout(SOCK_TIMEOUT).build();
		client = HttpClientBuilder.create().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
	}

	private static String genSign(Long t) {
		if (null == t) return "";
		SystemConfig config = SystemConfig.instance();
		String raw = t + config.getSnowflakeApp() + config.getSnowflakeSecKey();
		return HashUtil.getInstance("SHA-512").digestToBase64Url(raw);
	}

	public static class OpenPayTokenRsp {
		public Long appId;
		public String account;
	}

	public static class CreateOpenPayTokenRsp {
		public String token;
	}

	public static class SearchCompanyRsp {
		public Long id;
		public String name;
	}

	public static class IndexCompanyRsp {
	}

	public static class UnbindSpiderEcommRsp {
	}

	public static class SocialSecurityRsp {
		public String uid;
		public String multiple_form;
		public String session_id;
		public String status_code;
		public String msg;
		public String native_msg;
		public String path;

		@Override
		public String toString() {
			return "SocialSecurityRsp{" +
				"uid='" + uid + '\'' +
				", multiple_form='" + multiple_form + '\'' +
				", session_id='" + session_id + '\'' +
				", status_code='" + status_code + '\'' +
				", msg='" + msg + '\'' +
				", native_msg='" + native_msg + '\'' +
				", path='" + path + '\'' +
				'}';
		}
	}

	public static CreateOpenPayTokenRsp createOpenPayToken(Long appId, String account, String phone) {
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("appId", DbDataTypeUtil.toString(appId)));
		p.add(Pair.of("account", account));
		if (null != phone) p.add(Pair.of("phone", phone));
		Pair<StatusLine, ApiResult<CreateOpenPayTokenRsp>> r = post("/openpay/token/create", null, p, new TypeToken<ApiResult<CreateOpenPayTokenRsp>>() {
		}.getType());
		if (null == r.getLeft()) {
			log.error("no response. appId={}, acct={}, phone={}", appId, account, phone);
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("http status error. status-line={}, appId={}, acct={}, phone={}", r.getLeft(), appId, account, phone);
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("ec={}, msg={}", r.getRight().getErrCode(), r.getRight().getErrMsg());
			return null;
		}
		return r.getRight().getData();
	}

	public static OpenPayTokenRsp checkOpenPayToken(String token) {
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("token", token));
		Pair<StatusLine, ApiResult<OpenPayTokenRsp>> r = get("/openpay/token/check", null, p, new TypeToken<ApiResult<OpenPayTokenRsp>>() {
		}.getType());
		if (null == r.getLeft()) {
			log.error("no response");
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("http status error. status-line={}", r.getLeft().toString());
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("ec={}, msg={}", r.getRight().getErrCode(), r.getRight().getErrMsg());
			return null;
		}
		return r.getRight().getData();
	}

	public static SearchCompanyRsp searchCompany(Long countryId, String name) {
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("countryId", countryId.toString()));
		p.add(Pair.of("name", name));
		Pair<StatusLine, ApiResult<SearchCompanyRsp>> r = get("/auth/company/search", null, p, new TypeToken<ApiResult<SearchCompanyRsp>>() {
		}.getType());
		if (null == r.getLeft()) {
			log.error("no response");
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("http status error. status-line={}", r.getLeft().toString());
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("ec={}, msg={}", r.getRight().getErrCode(), r.getRight().getErrMsg());
			return null;
		}
		return r.getRight().getData();
	}

	public static IndexCompanyRsp indexCompany(Company company) {
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("id", DbDataTypeUtil.toString(company.getId())));
		p.add(Pair.of("countryId", DbDataTypeUtil.toString(company.getCountryId())));
		p.add(Pair.of("name", company.getName()));
		Pair<StatusLine, ApiResult<IndexCompanyRsp>> r = post("/auth/company/index", null, p, new TypeToken<ApiResult<IndexCompanyRsp>>() {
		}.getType());
		if (null == r.getLeft()) {
			log.error("no response");
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("http status error. status-line={}", r.getLeft().toString());
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("ec={}, msg={}", r.getRight().getErrCode(), r.getRight().getErrMsg());
			return null;
		}
		return r.getRight().getData();
	}

	public static UnbindSpiderEcommRsp unbindSpiderEcomm(Long uid) {
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("uid", DbDataTypeUtil.toString(uid)));
		Pair<StatusLine, ApiResult<UnbindSpiderEcommRsp>> r = post("/ecomm/info/unbind", null, p, new TypeToken<ApiResult<UnbindSpiderEcommRsp>>() {
		}.getType());
		if (null == r.getLeft()) {
			log.error("no response");
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("http status error. status-line={}", r.getLeft().toString());
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("ec={}, msg={}", r.getRight().getErrCode(), r.getRight().getErrMsg());
			return null;
		}
		return r.getRight().getData();
	}

	public static <T> Pair<StatusLine, T> post(String api, Map<String, String> headers, List<Pair<String, String>> params, Type rspType) {
		//失败重试2次,并非3次
		int retryTimes = 3;
		try {
			URIBuilder b = null;
			b = createUriBuilder(api);
			URI uri = b.build();
			HttpPost request = new HttpPost(uri);
			if (null != params) {
				request.setEntity(new UrlEncodedFormEntity(params.stream().map(p -> new BasicNameValuePair(p.getKey(), p.getValue())).collect(Collectors.toList()), Consts.UTF_8));
			}
			if (null != headers) {
				for (Map.Entry<String, String> h : headers.entrySet()) {
					request.addHeader(h.getKey(), h.getValue());
				}
			}
			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					EntityUtils.consume(response.getEntity());
					if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500)
						log.warn("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					else
						log.error("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					return Pair.of(response.getStatusLine(), null);
				}
				String rspStr = EntityUtils.toString(response.getEntity());
				T result = JsonUtil.fromJson(rspStr, rspType);
				return Pair.of(response.getStatusLine(), result);
			} catch (Exception e) {
				//发生连接超时或者未知主机超时进行重试
				if (e instanceof ConnectTimeoutException || e.getCause() instanceof ConnectTimeoutException ||
					e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException ||
					e instanceof HttpHostConnectException || e.getCause() instanceof HttpHostConnectException) {
					log.debug("retry call api! api={}", api, e.getMessage());
					//记录调用失败的host
					Set<String> failHosts = new HashSet<>();
					failHosts.add(uri.getHost());
					return retryPost(--retryTimes, failHosts, api, headers, params, rspType);
				}
				log.error("call api error! api={}", api, e);
			}
		} catch (Exception e) {
			log.error("api={}", api, e);
		}
		return Pair.of(null, null);
	}

	private static URIBuilder createUriBuilder(String api) throws URISyntaxException {
		SystemConfig config = SystemConfig.instance();

		URIBuilder b = new URIBuilder(getSnowflakeApiPrefix() + api);;
		b.setCharset(Charset.forName("UTF-8"));
		Long t = System.currentTimeMillis();
		b.addParameter("_t", t.toString());
		b.addParameter("_app", config.getSnowflakeApp());
		b.addParameter("_sign", genSign(t));

		return b;
	}

	public static String getSnowflakeApiPrefix(){
		TargetServiceAddress snowflakeAddress = RibbonServerListSelector.roundRobinSelectOne(AppConfig.getInstance().getEurekaConfig().get("snowflakeTargetServiceName"));
		if (!Objects.isNull(snowflakeAddress)) {
			return "http://" + snowflakeAddress.getHost() + ":" + snowflakeAddress.getPort() + "/api/json/inner";
		} else {
			log.error("snowflakeService is not available");
			throw  new RuntimeException("snowflakeService is not available");
		}
	}

	public static <T> Pair<StatusLine, T> get(String api, Map<String, String> headers, List<Pair<String, String>> params, Type rspType) {
		//失败了尝试2次,并非3次
		int retryTimes = 3;
		try {
			URIBuilder b = createUriBuilder(api);
			if (null != params) {
				for (Pair<String, String> p : params) {
					b.addParameter(p.getKey(), p.getValue());
				}
			}
			URI uri = b.build();
			HttpGet request = new HttpGet(uri);
			if (null != headers) {
				for (Map.Entry<String, String> h : headers.entrySet()) {
					request.addHeader(h.getKey(), h.getValue());
				}
			}
			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					EntityUtils.consume(response.getEntity());
					if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500)
						log.warn("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					else
						log.error("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					return Pair.of(response.getStatusLine(), null);
				}
				String rspStr = EntityUtils.toString(response.getEntity());
				try {
					T result = JsonUtil.fromJson(rspStr, rspType);
					return Pair.of(response.getStatusLine(), result);
				} catch (Exception e) {
					log.error("api={}, rspBody={}", api, rspStr, e);
				}
			} catch (Exception e) {
				//发生连接超时或者未知主机超时进行重试
				if (e instanceof ConnectTimeoutException || e.getCause() instanceof ConnectTimeoutException ||
					e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException ||
					e instanceof HttpHostConnectException || e.getCause() instanceof HttpHostConnectException) {
					log.debug("retry call api! api={}", api, e.getMessage());
					//记录调用失败的host
					Set<String> failHosts = new HashSet<>();
					failHosts.add(uri.getHost());
					return retryGet(--retryTimes, failHosts, api, headers, params, rspType);
				}
				log.error("call api error!  api={}", api, e);
			}
		} catch (Exception e) {
			log.error("api={}", api, e);
		}
		return Pair.of(null, null);
	}

	/**
	 * 可以进行重试Get方法
	 *
	 * @param retryTimes
	 * @param failHosts
	 * @param api
	 * @param headers
	 * @param params
	 * @param rspType
	 * @param <T>
	 * @return
	 */
	private static <T> Pair<StatusLine, T> retryGet(int retryTimes, Set<String> failHosts, String api, Map<String, String> headers, List<Pair<String, String>> params, Type rspType) {
		if (retryTimes == 0) {
			return Pair.of(null, null);
		}
		try {
			URIBuilder b = createUriBuilder(api, failHosts);

			//如果URIBuilder是空，代表所有的host都无法可用
			if (null == b) {
				return Pair.of(null, null);
			}

			if (null != params) {
				for (Pair<String, String> p : params) {
					b.addParameter(p.getKey(), p.getValue());
				}
			}
			URI uri = b.build();
			HttpGet request = new HttpGet(uri);
			if (null != headers) {
				for (Map.Entry<String, String> h : headers.entrySet()) {
					request.addHeader(h.getKey(), h.getValue());
				}
			}

			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					EntityUtils.consume(response.getEntity());
					if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500)
						log.warn("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					else
						log.error("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					return Pair.of(response.getStatusLine(), null);
				}
				String rspStr = EntityUtils.toString(response.getEntity());
				try {
					T result = JsonUtil.fromJson(rspStr, rspType);
					return Pair.of(response.getStatusLine(), result);
				} catch (Exception e) {
					log.error("api={}, rspBody={}", api, rspStr, e);
				}
			} catch (Exception e) {
				//发生连接超时或者未知主机超时进行重试
				if (e instanceof ConnectTimeoutException || e.getCause() instanceof ConnectTimeoutException ||
					e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException ||
					e instanceof HttpHostConnectException || e.getCause() instanceof HttpHostConnectException) {
					log.debug("retry call api! api={}", api, e.getMessage());
					//记录调用失败的host
					failHosts.add(uri.getHost());
					return retryGet(--retryTimes, failHosts, api, headers, params, rspType);
				}
				log.error("call api error!  api={}", api, e);
			}

		} catch (Exception e) {
			log.error("retryGet error! api={}", api, e);
		}
		return Pair.of(null, null);
	}

	/**
	 * 可以进行重试的Post
	 *
	 * @param retryTimes
	 * @param failHosts
	 * @param api
	 * @param headers
	 * @param params
	 * @param rspType
	 * @param <T>
	 * @return
	 */
	private static <T> Pair<StatusLine, T> retryPost(int retryTimes, Set<String> failHosts, String api, Map<String, String> headers, List<Pair<String, String>> params, Type rspType) {
		if (retryTimes == 0) {
			return Pair.of(null, null);
		}
		try {
			URIBuilder b = createUriBuilder(api, failHosts);

			//如果URIBuilder是空，代表所有的host都无法可用
			if (null == b) {
				return Pair.of(null, null);
			}

			URI uri = b.build();
			HttpPost request = new HttpPost(uri);
			if (null != params) {
				request.setEntity(new UrlEncodedFormEntity(params.stream().map(p -> new BasicNameValuePair(p.getKey(), p.getValue())).collect(Collectors.toList()), Consts.UTF_8));
			}
			if (null != headers) {
				for (Map.Entry<String, String> h : headers.entrySet()) {
					request.addHeader(h.getKey(), h.getValue());
				}
			}
			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					EntityUtils.consume(response.getEntity());
					if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500)
						log.warn("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					else
						log.error("api={}, status={}, reason={}", api, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					return Pair.of(response.getStatusLine(), null);
				}
				String rspStr = EntityUtils.toString(response.getEntity());
				T result = JsonUtil.fromJson(rspStr, rspType);
				return Pair.of(response.getStatusLine(), result);
			} catch (Exception e) {
				//发生连接超时或者未知主机超时进行重试
				if (e instanceof ConnectTimeoutException || e.getCause() instanceof ConnectTimeoutException ||
					e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException ||
					e instanceof HttpHostConnectException || e.getCause() instanceof HttpHostConnectException) {
					log.debug("retry call api! api={}", api, e.getMessage());
					//记录调用失败的host
					failHosts.add(uri.getHost());
					return retryPost(--retryTimes, failHosts, api, headers, params, rspType);
				}
				log.error("call api error! api={}", api, e);
			}

		} catch (Exception e) {
			log.error("retryPost error! api={}", api, e);
		}
		return Pair.of(null, null);
	}

	/**
	 * 创建一个可以过滤失败Host的URIBuilder
	 *
	 * @param api
	 * @param failHosts
	 * @return 此方法可能返回null
	 */
	@Nullable
	private static URIBuilder createUriBuilder(String api, Set<String> failHosts) throws URISyntaxException {
		SystemConfig config = SystemConfig.instance();
		URIBuilder b = null;
		if (Boolean.valueOf(AppConfig.getInstance().getEurekaConfig().get("isEnableEureka")) && !EurekaRibbonConfig.isShutDown()) {
			TargetServiceAddress snowflakeAddress = RibbonServerListSelector.roundRobinSelectOne(AppConfig.getInstance().getEurekaConfig().get("snowflakeTargetServiceName"), failHosts);
			if (!Objects.isNull(snowflakeAddress)) {
				b = new URIBuilder("http://" + snowflakeAddress.getHost() + ":" + snowflakeAddress.getPort() + "/api/json/inner" + api);
			}
		}

		if (Objects.isNull(b)) {
			log.warn("can't get available host! api={},failHosts={}", api, failHosts);
			return b;
		}

		b.setCharset(Charset.forName("UTF-8"));
		Long t = System.currentTimeMillis();
		b.addParameter("_t", t.toString());
		b.addParameter("_app", config.getSnowflakeApp());
		b.addParameter("_sign", genSign(t));

		return b;
	}

	/**
	 * 调用snowflake解除用户绑定的银行账户，并且清除缓存
	 *
	 * @param uid
	 */
	public static void unBindSpiderBankInfo(Long uid) {
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("uid", uid.toString()));
		Pair<StatusLine, ApiResult<IndexCompanyRsp>> r = post("/bank/info/unbind", null, p, new TypeToken<ApiResult<String>>() {
		}.getType());
		if (null == r.getLeft()) {
			log.error("no response");
			return;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("http status error. status-line={}", r.getLeft().toString());
			return;
		}
		if (!r.getRight().isSuccess()) {
			log.error("ec={}, msg={}", r.getRight().getErrCode(), r.getRight().getErrMsg());
		}
	}

	public static SocialSecurityRsp getCrawler(SocialSecurityRequest social) {
		log.debug("SocialSecurityRsp begin:");
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("uid", social.getUid().toString()));
		p.add(Pair.of("sessionId", social.getSession_id()));
		p.add(Pair.of("path", social.getPath()));
		p.add(Pair.of("formId", social.getForm_id().toString()));
		p.add(Pair.of("loginProvince", social.getLogin_province()));
		p.add(Pair.of("loginDistrict", social.getLogin_district()));
		p.add(Pair.of("loginIdNo", social.getLogin_id_no()));
		p.add(Pair.of("loginSocialSecurityId", social.getLogin_social_security_id()));
		p.add(Pair.of("loginName", social.getLogin_name()));
		p.add(Pair.of("loginValidCode", social.getLogin_valid_code()));

		log.debug("SocialSecurityRsp request begin:");
		Pair<StatusLine, ApiResult<SocialSecurityRsp>> r = post("/social/security/check", null, p, new TypeToken<ApiResult<SocialSecurityRsp>>() {
		}.getType());
		log.debug("SocialSecurityRsp response end:");
		if (null == r.getLeft()) {
			log.error("no response. uid={}, sessionId={}, loginProvince={}, loginDistrict={}, loginIdNo={}, loginSocialSecurityId={}",
				social.getUid(), social.getSession_id(), social.getLogin_province(), social.getLogin_district(),
				social.getLogin_id_no(), social.getLogin_social_security_id());
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("r.getLeft().getStatusCode():" + r.getLeft().getStatusCode() + "no response. uid={}, sessionId={}, loginProvince={}, loginDistrict={}, loginIdNo={}, loginSocialSecurityId={}",
				social.getUid().toString(), social.getSession_id(), social.getLogin_province(), social.getLogin_district(),
				social.getLogin_id_no(), social.getLogin_social_security_id());
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("no response. uid={}, sessionId={}, loginProvince={}, loginDistrict={}, loginIdNo={}, loginSocialSecurityId={}",
				social.getUid().toString(), social.getSession_id(), social.getLogin_province(), social.getLogin_district(),
				social.getLogin_id_no(), social.getLogin_social_security_id());
			return null;
		}
		log.debug("response data:" + r.getRight().getData());

		return r.getRight().getData();
	}

	public static SocialSecurityRsp getPHUserLoginStatus(SocialSecurityRequest social) {
		log.debug("SocialSecurityRsp begin:");
		List<Pair<String, String>> p = new ArrayList<>();
		p.add(Pair.of("uid", social.getUid().toString()));
		p.add(Pair.of("session_id", social.getSession_id()));
		p.add(Pair.of("path", social.getPath()));
		p.add(Pair.of("form_id", social.getForm_id().toString()));
		p.add(Pair.of("url", social.getUrl()));
		p.add(Pair.of("login_account", social.getLogin_account()));
		p.add(Pair.of("login_password", social.getLogin_password()));

		log.debug("SocialSecurityRsp request begin:");
		Pair<StatusLine, ApiResult<SocialSecurityRsp>> r = post("/login/social/security", null, p, new TypeToken<ApiResult<SocialSecurityRsp>>() {
		}.getType());
		log.debug("SocialSecurityRsp response end:");
		if (null == r.getLeft()) {
			log.error("no response. uid={}, sessionId={}",
				social.getUid(), social.getSession_id());
			return null;
		}
		if (!Objects.equals(HttpStatus.SC_OK, r.getLeft().getStatusCode())) {
			log.error("r.getLeft().getStatusCode():" + r.getLeft().getStatusCode() + "no response. uid={}, sessionId={}",
				social.getUid().toString(), social.getSession_id());
			return null;
		}
		if (!r.getRight().isSuccess()) {
			log.error("no response. uid={}, sessionId={}, ",
				social.getUid().toString(), social.getSession_id());
			return null;
		}
		log.debug("response data:" + r.getRight().getData());

		return r.getRight().getData();
	}
}
