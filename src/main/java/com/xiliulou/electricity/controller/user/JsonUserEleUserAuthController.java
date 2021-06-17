package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 实名认证信息(TEleUserAuth)表控制层
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
@RestController
@Slf4j
public class JsonUserEleUserAuthController {
	/**
	 * 服务对象
	 */
	@Autowired
	EleUserAuthService eleUserAuthService;

	@Autowired
	EleAuthEntryService eleAuthEntryService;

	@Autowired
	RedisService redisService;

	//实名认证
	@PostMapping("/user/auth")
	public R webAuth(@RequestBody List<EleUserAuth> eleUserAuthList) {
		if (!DataUtil.collectionIsUsable(eleUserAuthList)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//限频
		Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_AUTH_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
		if (!getLockSuccess) {
			return R.fail("ELECTRICITY.0034", "操作频繁");
		}

		eleUserAuthService.webAuth(eleUserAuthList);
		return R.ok();

	}


	/**
	 * 获取需要实名认证资料项
	 */
	@GetMapping(value = "/user/authEntry/list")
	public R getEleAuthEntriesList() {
		return R.ok(eleAuthEntryService.getUseEleAuthEntriesList());
	}

	/**
	 * 获取当前用户的具体审核状态
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/authStatus")
	public R getEleUserAuthSpecificStatus() {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		return eleUserAuthService.getEleUserAuthSpecificStatus(uid);
	}

	/**
	 * 获取当前的用户资料项
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/current/authEntry/list")
	public R getCurrentEleAuthEntriesList() {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		return eleUserAuthService.selectCurrentEleAuthEntriesList(uid);
	}

	/**
	 * 获取当前用户的具体状态
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/serviceStatus")
	public R getEleUserServiceStatus() {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		return eleUserAuthService.getEleUserServiceStatus(uid);
	}

	/**
	 * 获取上传身份证照片所需的签名
	 */
	@GetMapping(value = "/user/acquire/upload/idcard/file/sign")
	public R getUploadIdcardFileSign() {
        return eleUserAuthService.acquireIdcardFileSign();
	}

}
