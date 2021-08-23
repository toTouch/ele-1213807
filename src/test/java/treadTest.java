import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.pay.weixin.entity.SharePicture;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * @author: Miss.Li
 * @Date: 2021/8/23 13:34
 * @Description:
 */
public class treadTest {

	@Test
	public void test1() {
		Thread thread1 =new Thread(() -> {
			System.out.println("线程1开始执行");
			String result=post();
			System.out.println("线程1执行中");
			System.out.println(result);
			System.out.println("线程1执行完毕");
		});
		Thread thread2 =new Thread(() -> {
			System.out.println("线程2开始执行");
			String result=post();
			System.out.println("线程2执行中");
			System.out.println(result);
			System.out.println("线程2执行完毕");
		});
		Thread thread3 =new Thread(() -> {
			System.out.println("线程3开始执行");
			String result=post();
			System.out.println("线程3执行中");
			System.out.println(result);
			System.out.println("线程3执行完毕");
		});

			thread1.start();
			thread2.start();
			thread3.start();
	}

	public String post() {
		//加密
		Long sTime = System.currentTimeMillis();
		String appId = "20212c29d393";
		String secKey = "bd5e9693f46347f3a4a7626328b902b4";
		String requestHeaderInfos = sTime + appId + secKey;
		byte[] bytes = new byte[0];
		try {
			bytes = MessageDigest.getInstance("SHA-512").digest(requestHeaderInfos.getBytes("UTF-8"));
		} catch (Exception e) {
			System.out.println(e);
		}
		String sign = Base64.encodeBase64URLSafeString(bytes);
        String result=null;
		//发送请求
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("https://wm.xiliulou.com/cupboard/outer/new/cupboard/command");
			post.setHeader("Content-Type", "application/json");
			post.addHeader("sTime", String.valueOf(sTime));
			post.addHeader("appId", appId);
			post.addHeader("sign", sign);

			StringEntity s = new StringEntity("{\"productKey\":\"111\",\"deviceName\":\"222\"}", "utf-8");
			post.setEntity(s);
			HttpResponse httpResponse = client.execute(post);
			InputStream in = httpResponse.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
			StringBuilder strber = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				strber.append(line + "\n");
			}
			in.close();
			result=LocalTime.now() + ":" + strber.toString();
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e);
		}
		return result;
	}
}
