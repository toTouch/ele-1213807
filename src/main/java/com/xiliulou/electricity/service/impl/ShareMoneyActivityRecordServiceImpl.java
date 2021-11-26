package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.mapper.ShareActivityRecordMapper;
import com.xiliulou.electricity.mapper.ShareMoneyActivityRecordMapper;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityRecordQuery;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ShareActivityRecordVO;
import com.xiliulou.electricity.vo.ShareMoneyActivityRecordVO;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 发起邀请活动记录(ShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@Service("shareMoneyActivityRecordService")
@Slf4j
public class ShareMoneyActivityRecordServiceImpl implements ShareMoneyActivityRecordService {

	@Resource
	private ShareMoneyActivityRecordMapper shareMoneyActivityRecordMapper;

	@Autowired
	RedisService redisService;

	@Autowired
	GenerateShareUrlService generateShareUrlService;

	@Autowired
	ElectricityPayParamsService electricityPayParamsService;

	@Autowired
	ShareMoneyActivityService shareMoneyActivityService;

	@Autowired
	UserService userService;


	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ShareMoneyActivityRecord queryByIdFromDB(Long id) {
		return this.shareMoneyActivityRecordMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param shareMoneyActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ShareMoneyActivityRecord insert(ShareMoneyActivityRecord shareMoneyActivityRecord) {
		this.shareMoneyActivityRecordMapper.insert(shareMoneyActivityRecord);
		return shareMoneyActivityRecord;
	}

	/**
	 * 修改数据
	 *
	 * @param shareMoneyActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(ShareMoneyActivityRecord shareMoneyActivityRecord) {
		return this.shareMoneyActivityRecordMapper.updateById(shareMoneyActivityRecord);

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
		boolean result = redisService.setNx(ElectricityCabinetConstant.SHARE_ACTIVITY_UID + user.getUid(), "1", 5 * 1000L, false);
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
		ShareMoneyActivityRecord oldShareMoneyActivityRecord = shareMoneyActivityRecordMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivityRecord>()
				.eq(ShareMoneyActivityRecord::getUid, user.getUid()).eq(ShareMoneyActivityRecord::getActivityId, activityId));


		//第一次分享
		if (Objects.isNull(oldShareMoneyActivityRecord)) {
			//2、生成分享记录
			//2.1 、生成code
			String code = RandomUtil.randomNumbers(6);

			//2.2、生成分享记录
			ShareMoneyActivityRecord shareMoneyActivityRecord = new ShareMoneyActivityRecord();
			shareMoneyActivityRecord.setActivityId(activityId);
			shareMoneyActivityRecord.setUid(user.getUid());
			shareMoneyActivityRecord.setTenantId(tenantId);
			shareMoneyActivityRecord.setCode(code);
			shareMoneyActivityRecord.setCreateTime(System.currentTimeMillis());
			shareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
			shareMoneyActivityRecord.setStatus(ShareMoneyActivityRecord.STATUS_INIT);
			shareMoneyActivityRecordMapper.insert(shareMoneyActivityRecord);
		}

		ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivityRecord>()
				.eq(ShareMoneyActivityRecord::getUid, user.getUid()).eq(ShareMoneyActivityRecord::getActivityId, activityId));

		//3、scene
		String scene = "uid:"+user.getUid()+",id:"+activityId+",code:"+shareMoneyActivityRecord.getCode()+",type:2";


		//修改分享状态
		ShareMoneyActivityRecord newShareMoneyActivityRecord = new ShareMoneyActivityRecord();
		newShareMoneyActivityRecord.setId(shareMoneyActivityRecord.getId());
		newShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());


		//4、调起微信
		SharePicture sharePicture = new SharePicture();
		sharePicture.setPage(page);
		sharePicture.setScene(scene);
		sharePicture.setAppId(electricityPayParams.getMerchantMinProAppId());
		sharePicture.setAppSecret(electricityPayParams.getMerchantMinProAppSecert());
		Pair<Boolean, Object> getShareUrlPair = generateShareUrlService.generateSharePicture(sharePicture);

		//分享失败
		if (!getShareUrlPair.getLeft()) {
			newShareMoneyActivityRecord.setStatus(ShareMoneyActivityRecord.STATUS_FAIL);
			shareMoneyActivityRecordMapper.updateById(newShareMoneyActivityRecord);
			return R.fail(getShareUrlPair.getRight());
		}

		//分享成功
		newShareMoneyActivityRecord.setStatus(ShareMoneyActivityRecord.STATUS_SUCCESS);
		shareMoneyActivityRecordMapper.updateById(newShareMoneyActivityRecord);
		return R.ok(getShareUrlPair.getRight());

	}

	@Override
	public ShareMoneyActivityRecord queryByUid(Long uid,Integer activityId) {
		return shareMoneyActivityRecordMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivityRecord>()
				.eq(ShareMoneyActivityRecord::getUid, uid).eq(ShareMoneyActivityRecord::getActivityId,activityId));
	}

	@Override
	public void addCountByUid(Long uid) {
		shareMoneyActivityRecordMapper.addCountByUid(uid);
	}



	@Override
	public R queryList(ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery) {

		List<ShareMoneyActivityRecordVO> shareMoneyActivityRecordVOList= shareMoneyActivityRecordMapper.queryList(shareMoneyActivityRecordQuery);
		return R.ok(shareMoneyActivityRecordVOList);
	}

	@Override
	public R queryCount(ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery) {
		return R.ok(shareMoneyActivityRecordMapper.queryCount(shareMoneyActivityRecordQuery));
	}
}
