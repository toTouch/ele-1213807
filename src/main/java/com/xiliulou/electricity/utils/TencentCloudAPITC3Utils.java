package com.xiliulou.electricity.utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;


public class TencentCloudAPITC3Utils {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final static String SECRET_ID = "AKIDlIYXcQqNoumNhcTsKvHNBiuoSpIUdBey";
    private final static String SECRET_KEY = "Vj3QXNwHTP5U2MQCQVKtDah5D53kAOba";
    private final static String CT_JSON = "application/json; charset=utf-8";

    public static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(UTF8));
    }

    public static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(UTF8));
        return DatatypeConverter.printHexBinary(d).toLowerCase();
    }

    public static String liveDetection (String name, String idCard, MultipartFile file) throws Exception {
        String service = "faceid";
        String host = "faceid.tencentcloudapi.com";
        String action = "LivenessRecognition";
        String version = "2018-03-01";
        String algorithm = "TC3-HMAC-SHA256";
        String timestamp =String.valueOf(System.currentTimeMillis() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 注意时区，否则容易出错
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.valueOf(timestamp + "000")));

        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n" + "host:" + host + "\n";
        String signedHeaders = "content-type;host";

        String payload = "{\"IdCard\": "+idCard+", \"Name\": "+name+", \"VideoBase64\": "+name+", \"LivenessType\": "+name+"}";
        String hashedRequestPayload = sha256Hex(payload);
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;

        // ************* 步骤 2：拼接待签名字符串 *************
        String credentialScope = date + "/" + service + "/" + "tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        // ************* 步骤 3：计算签名 *************
        byte[] secretDate = hmac256(("TC3" + SECRET_KEY).getBytes(UTF8), date);
        byte[] secretService = hmac256(secretDate, service);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();

        // ************* 步骤 4：拼接 Authorization *************
        String authorization = algorithm + " " + "Credential=" + SECRET_ID + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;

        // ************* 步骤 5：发送请求 *************
        HttpClient client=new DefaultHttpClient();
        HttpPost post=new HttpPost("https://"+host+"/"+"?IdCard="+idCard+"&Name="+name+"&VideoBase64="+name+"&LivenessType=SILENT");
        post.setHeader("Authorization", authorization);
        post.setHeader("Content-Type", CT_JSON);
        post.setHeader("Host", host);
        post.setHeader("X-TC-Action", action);
        post.setHeader("X-TC-Timestamp", timestamp);
        post.setHeader("X-TC-Version", version);

        HttpResponse httpResponse=client.execute(post);
        InputStream in=httpResponse.getEntity().getContent();
        BufferedReader br=new BufferedReader(new InputStreamReader(in, "utf-8"));
        StringBuilder strber=new StringBuilder();
        String line=null;
        while ((line=br.readLine())!=null) {
            strber.append(line+"\n");
        }
        in.close();
        String result=strber.toString();
        return result;
    }

}
