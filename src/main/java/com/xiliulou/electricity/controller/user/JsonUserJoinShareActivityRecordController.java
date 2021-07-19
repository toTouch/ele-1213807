package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.client.identify.Base64;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.pay.weixin.constant.WechatConstant;
import com.xiliulou.pay.weixin.entity.AccessTokenResult;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.entity.SharePictureQuery;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonUserJoinShareActivityRecordController {
	/**
	 * 服务对象
	 */
	@Resource
	private JoinShareActivityRecordService joinShareActivityRecordService;

	@Autowired
	GenerateShareUrlService generateShareUrlService;

	/**
	 * 解密分享图片
	 */
	@GetMapping(value = "/outer/joinShareActivityRecord/checkScene")
	public R checkScene(@RequestParam(value = "scene") String scene) {
		return joinShareActivityRecordService.checkScene(scene);
	}

	/**
	 * 点击分享链接进入活动
	 */
	@PostMapping(value = "/user/joinShareActivityRecord/joinActivity")
	public R joinActivity(@RequestParam(value = "activityId") Integer activityId, @RequestParam(value = "uid") Long uid) {
		return joinShareActivityRecordService.joinActivity(activityId, uid);
	}

	/**
	 * 点击分享链接进入活动
	 */
	@GetMapping(value = "/outer/test")
	public R test() {
		SharePicture sharePicture = new SharePicture();
		sharePicture.setPage("pages/start/index");
		sharePicture.setScene("12332231232123123123123123123123123123123123123123123123123123123123123");
		sharePicture.setAppId("wx76159ea6aa7a64bc");
		sharePicture.setAppSecret("b44586ca1b4ff8def2b4c869cdd8ea6a");
		Pair<Boolean, Object> getShareUrlPair = generateSharePicture(sharePicture);
		return R.ok(getShareUrlPair.getRight());

	}

	public Pair<Boolean, Object> generateSharePicture(SharePicture sharePicture) {

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


		//post请求得到返回数据（这里是封装过的，就是普通的java post请求）
		String response = sendPost(url,JsonUtil.toJson(sharePictureQuery));
		return Pair.of(false, response);

	}

	private Pair<Boolean, Object> getAccessToken(String appId, String appSecret) {

		String url = String.format(WechatConstant.GET_WX_ACCESS_TOKEN, appId, appSecret);
		String result = HttpUtil.get(url);
		String accessToken = JSONUtil.toBean(result, AccessTokenResult.class).getAccess_token();
		log.info("GET ACCESS_TOKEN  RESULT：" + result);
		if (ObjectUtil.isEmpty(accessToken)) {
			log.error("WX_PRO SEND_TEMPLATE ERROR,GET ACCESS_TOKEN ERROR,MSG:{},APPID:{},SECRET:{}",
					result, appId, appSecret);
			return Pair.of(false, "获取微信accessToken失败!");
		}

		return Pair.of(true, accessToken);
	}

	//post请求
	public static String sendPost( String url,String param) {
		PrintWriter out = null;
		InputStream in = null;
		String result = "";
		try {

			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			// out = new PrintWriter(conn.getOutputStream());
			out = new PrintWriter(new OutputStreamWriter(
					conn.getOutputStream(), "utf-8"));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			out = new PrintWriter(conn.getOutputStream());
			in = conn.getInputStream();
			byte[] data = null;
			// 读取图片字节数组
			try {
				ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
				byte[] buff = new byte[100];
				int rc = 0;
				while ((rc = in.read(buff, 0, 100)) > 0) {
					swapStream.write(buff, 0, rc);
				}
				data = swapStream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if(data.length>1000) {
				return new String(Base64.encodeBase64(data));
			}
			return JsonUtil.fetchObject(new String(data), "/errcode").toString();
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

}
