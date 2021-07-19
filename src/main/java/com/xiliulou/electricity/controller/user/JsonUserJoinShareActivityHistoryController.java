package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.client.identify.Base64;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.pay.weixin.constant.WechatConstant;
import com.xiliulou.pay.weixin.entity.AccessTokenResult;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.entity.SharePictureQuery;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
public class JsonUserJoinShareActivityHistoryController {
	/**
	 * 服务对象
	 */
	@Resource
	private JoinShareActivityHistoryService joinShareActivityHistoryService;



	/**
	 * 用户邀请记录
	 */
	@GetMapping(value = "/user/joinShareActivityHistory/list")
	public R joinActivity() {
		return joinShareActivityHistoryService.userList();
	}

}
