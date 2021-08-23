/**
 * @author: Miss.Li
 * @Date: 2021/7/14 19:30
 * @Description:
 */

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.client.identify.Base64;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.pay.weixin.constant.WechatConstant;
import com.xiliulou.pay.weixin.entity.AccessTokenResult;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.entity.SharePictureQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EleTest {

	@Test
	public void test1() {
		RestTemplate restTemplate = new RestTemplate();
		SharePicture sharePicture = new SharePicture();
		sharePicture.setPage("pages/start/index");
		sharePicture.setScene("id:1");
		sharePicture.setAppId("wx76159ea6aa7a64bc");
		sharePicture.setAppSecret("b44586ca1b4ff8def2b4c869cdd8ea6a");
		Pair<Boolean, Object> getShareUrlPair = generateSharePicture(sharePicture, restTemplate);
		System.out.println(getShareUrlPair.getRight());
	}

	public Pair<Boolean, Object> generateSharePicture(SharePicture sharePicture, RestTemplate restTemplate) {

		//获取AccessToken
		Pair<Boolean, Object> getAccessTokenPair =
				getAccessToken(sharePicture.getAppId(), sharePicture.getAppSecret());
		if (!getAccessTokenPair.getLeft()) {
			return getAccessTokenPair;
		}

		String accessToken = getAccessTokenPair.getRight().toString();

		String url = String.format(WechatConstant.GET_WX_SHARE_PICTURE, accessToken);

		//发送给微信服务器的数据

		SharePictureQuery sharePictureQuery = new SharePictureQuery();
		sharePictureQuery.setPage(sharePicture.getPage());
		sharePictureQuery.setScene(sharePicture.getScene());

		String response = sendPost(url, JsonUtil.toJson(sharePictureQuery));

		return Pair.of(false, response);

	}

	private Pair<Boolean, Object> getAccessToken(String appId, String appSecret) {

		String url = String.format(WechatConstant.GET_WX_ACCESS_TOKEN, appId, appSecret);
		String result = HttpUtil.get(url);
		String accessToken = JSONUtil.toBean(result, AccessTokenResult.class).getAccess_token();
		if (ObjectUtil.isEmpty(accessToken)) {
			log.error("WX_PRO SEND_TEMPLATE ERROR,GET ACCESS_TOKEN ERROR,MSG:{},APPID:{},SECRET:{}",
					result, appId, appSecret);
			return Pair.of(false, "获取微信accessToken失败!");
		}

		return Pair.of(true, accessToken);
	}

	//post请求
	public static String sendPost(String url, String params) {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(20);
		cm.setDefaultMaxPerRoute(20);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).setConnectionRequestTimeout(10 * 1000).setSocketTimeout(10 * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();

		try {
			URIBuilder b = new URIBuilder(url);
			URI uri = b.build();
			HttpPost request = new HttpPost(uri);
			if (null != params) {
				request.setEntity(new StringEntity(params));
			}

			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					EntityUtils.consume(response.getEntity());
					if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500)
						log.warn("url={}, status={}, reason={}", url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					else
						log.error("url={}, status={}, reason={}", url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
					return response.getStatusLine().getReasonPhrase();
				}
				InputStream in = response.getEntity().getContent();


				// 读取图片字节数组
				ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
				byte[] buff = new byte[100];
				int rc = 0;
				while ((rc = in.read(buff, 0, 100)) > 0) {
					swapStream.write(buff, 0, rc);
				}
				byte[] data =  swapStream.toByteArray();

				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (data.length > 1000) {
					return new String(Base64.encodeBase64(data));
				}
				return JsonUtil.fetchObject(new String(data), "/errmsg").toString();
			} catch (Exception e) {
				log.error("call url error! url={}", url, e);
			}

		} catch (Exception e) {
			log.error("url={}", url, e);
		}
		return "";
	}
}
