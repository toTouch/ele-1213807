package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.pay.weixin.constant.WechatConstant;
import com.xiliulou.pay.weixin.entity.AccessTokenResult;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.entity.SharePictureQuery;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
		sharePicture.setScene("1");
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

		log.info("sharePictureQuery1 is -->{}", sharePictureQuery);
		log.info("sharePictureQuery2 is -->{}", JsonUtil.toJson(sharePictureQuery));

		//post请求得到返回数据（这里是封装过的，就是普通的java post请求）
		String response = HttpUtil.post(url, JsonUtil.toJson(sharePictureQuery));
		Object object = JsonUtil.fetchObject(response, "/errcode");
		if (Objects.isNull(object)) {
			return Pair.of(true, Base64.encodeBase64(response.getBytes()));
		}
		return Pair.of(false, JsonUtil.fetchObject(response, "/errmsg"));

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

}
