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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;


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
	@Autowired
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

}
