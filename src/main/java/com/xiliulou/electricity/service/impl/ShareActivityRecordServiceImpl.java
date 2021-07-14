package com.xiliulou.electricity.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.AESUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.mapper.ShareActivityRecordMapper;
import com.xiliulou.electricity.query.SharePictureQuery;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 发起邀请活动记录(ShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@Service("shareActivityRecordService")
@Slf4j
public class ShareActivityRecordServiceImpl implements ShareActivityRecordService {
	@Resource
	private ShareActivityRecordMapper shareActivityRecordMapper;

	@Autowired
	RedisService redisService;

	@Autowired
	GenerateShareUrlService generateShareUrlService;

	@Autowired
	ElectricityPayParamsService electricityPayParamsService;

	@Value("${security.encode.key:xiliu&lo@u%12345}")
	private String encodeKey;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ShareActivityRecord queryByIdFromDB(Long id) {
		return this.shareActivityRecordMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param shareActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ShareActivityRecord insert(ShareActivityRecord shareActivityRecord) {
		this.shareActivityRecordMapper.insert(shareActivityRecord);
		return shareActivityRecord;
	}

	/**
	 * 修改数据
	 *
	 * @param shareActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(ShareActivityRecord shareActivityRecord) {
		return this.shareActivityRecordMapper.updateById(shareActivityRecord);

	}

	/**
	 * 1、判断是否分享过
	 * 2、生成分享记录
	 * 3、加密scene
	 * 4、调起微信
	 *
	 */
	@Override
	public R generateSharePicture(Integer activityId, String page) {

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("order  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//限频
		boolean result = redisService.setNx(ElectricityCabinetConstant.SHARE_ACTIVITY_UID + user.getUid(), "1", 15 * 1000L, false);
		if (!result) {
			return R.fail("ELECTRICITY.0034", "操作频繁");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//获取小程序appId
		ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
		if (Objects.isNull(electricityPayParams)) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
			return R.failMsg("未配置支付参数!");
		}

		//参数page
		if (Objects.isNull(page)) {
			page = "pages/start/index";
		}

		//1、判断是否分享过
		ShareActivityRecord oldShareActivityRecord = shareActivityRecordMapper.selectOne(new LambdaQueryWrapper<ShareActivityRecord>()
				.eq(ShareActivityRecord::getUid, user.getUid()).eq(ShareActivityRecord::getActivityId, activityId));

		Long id;

		//第一次分享
		if (Objects.isNull(oldShareActivityRecord)) {
			//2、生成分享记录
			//2.1 、生成code
			String code = RandomUtil.randomNumbers(16);

			//2.2、生成分享记录
			ShareActivityRecord shareActivityRecord = new ShareActivityRecord();
			shareActivityRecord.setActivityId(activityId);
			shareActivityRecord.setUid(user.getUid());
			shareActivityRecord.setTenantId(tenantId);
			shareActivityRecord.setCode(code);
			shareActivityRecord.setCreateTime(System.currentTimeMillis());
			shareActivityRecord.setUpdateTime(System.currentTimeMillis());
			shareActivityRecord.setStatus(ShareActivityRecord.STATUS_INIT);
			shareActivityRecordMapper.insert(shareActivityRecord);
			id = shareActivityRecord.getId();
		} else {
			id = oldShareActivityRecord.getId();
		}

		//3、加密scene
		SharePictureQuery sharePictureQuery=new SharePictureQuery();
		sharePictureQuery.setUid(user.getUid());
		sharePictureQuery.setActivityId(activityId);
		sharePictureQuery.setCode(oldShareActivityRecord.getCode());


		String str = JSONObject.toJSONString(sharePictureQuery);
		String scene= AESUtil.encrypt(AESUtil.SECRET_KEY,str);


		//修改分享状态
		ShareActivityRecord newShareActivityRecord = new ShareActivityRecord();
		newShareActivityRecord.setId(id);
		newShareActivityRecord.setUpdateTime(System.currentTimeMillis());


		//4、调起微信
		SharePicture sharePicture = new SharePicture();
		sharePicture.setPage(page);
		sharePicture.setScene(scene);
		sharePicture.setAppId(electricityPayParams.getMerchantMinProAppId());
		sharePicture.setAppSecret(electricityPayParams.getMerchantMinProAppSecert());
		Pair<Boolean, Object> getShareUrlPair = generateShareUrlService.generateSharePicture(sharePicture);

		//分享失败
		if (!getShareUrlPair.getLeft()) {
			newShareActivityRecord.setStatus(ShareActivityRecord.STATUS_FAIL);
			shareActivityRecordMapper.updateById(newShareActivityRecord);
			return R.fail(getShareUrlPair.getRight());
		}

		//分享成功
		newShareActivityRecord.setStatus(ShareActivityRecord.STATUS_SUCCESS);
		shareActivityRecordMapper.updateById(newShareActivityRecord);
		return R.ok(getShareUrlPair.getRight());

	}




}
