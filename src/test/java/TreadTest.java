
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.time.LocalTime;


/**
 * @author: Miss.Li
 * @Date: 2021/8/23 13:34
 * @Description:
 */
public class TreadTest {


	public static void main(String[] args) {
		MyThread myThread = new MyThread();

		Thread thread1 = new Thread(myThread,"1");
		Thread thread2 = new Thread(myThread,"2");
		Thread thread3 = new Thread(myThread,"3");

		thread1.start();
		thread2.start();
		thread3.start();

	}

}

class MyThread implements Runnable {

	@Override
	public void run() {
		for (int i=0;i<500;i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			post();
		}
	}

	public void post() {
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
			String line;
			while ((line = br.readLine()) != null) {
				strber.append(line + "\n");
			}
			in.close();
			System.out.println("------------------------------------------");
			System.out.println(LocalTime.now());
			System.out.println(strber.toString());
			System.out.println("------------------------------------------");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}


