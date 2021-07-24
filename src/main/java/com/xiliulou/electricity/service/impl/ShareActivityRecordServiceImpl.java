package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.ShareActivityRecordMapper;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ShareActivityRecordVO;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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

	@Autowired
	ShareActivityService shareActivityService;

	@Autowired
	UserService userService;


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


		//第一次分享
		if (Objects.isNull(oldShareActivityRecord)) {
			//2、生成分享记录
			//2.1 、生成code
			String code = RandomUtil.randomNumbers(6);

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
		}

		ShareActivityRecord shareActivityRecord = shareActivityRecordMapper.selectOne(new LambdaQueryWrapper<ShareActivityRecord>()
				.eq(ShareActivityRecord::getUid, user.getUid()).eq(ShareActivityRecord::getActivityId, activityId));

		//3、scene
		String scene = "uid:"+user.getUid()+",id:"+activityId+",code:"+shareActivityRecord.getCode();

		log.info("scene is -->{}",scene);


		//修改分享状态
		ShareActivityRecord newShareActivityRecord = new ShareActivityRecord();
		newShareActivityRecord.setId(shareActivityRecord.getId());
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

	@Override
	public ShareActivityRecord queryByUid(Long uid) {
		return shareActivityRecordMapper.selectOne(new LambdaQueryWrapper<ShareActivityRecord>()
				.eq(ShareActivityRecord::getUid, uid));
	}

	@Override
	public void addCountByUid(Long uid) {
		shareActivityRecordMapper.addCountByUid(uid);
	}

	@Override
	public void reduceAvailableCountByUid(Long uid, Integer count) {
		shareActivityRecordMapper.reduceAvailableCountByUid(uid,count);
	}

	@Override
	public R queryList(ShareActivityRecordQuery shareActivityRecordQuery) {
		List<ShareActivityRecord> shareActivityRecordList= shareActivityRecordMapper.queryList(shareActivityRecordQuery);
		if(ObjectUtil.isEmpty(shareActivityRecordList)) {
			return R.ok(shareActivityRecordList);
		}

		List<ShareActivityRecordVO> shareActivityRecordVOList=new ArrayList<>();
		for (ShareActivityRecord shareActivityRecord:shareActivityRecordList) {

			ShareActivityRecordVO shareActivityRecordVO=new ShareActivityRecordVO();
			BeanUtil.copyProperties(shareActivityRecord,shareActivityRecordVO);

			ShareActivity shareActivity=shareActivityService.queryByIdFromCache(shareActivityRecord.getActivityId());
			if(Objects.nonNull(shareActivity)){
				shareActivityRecordVO.setActivityName(shareActivity.getName());
			}

			User user=userService.queryByUidFromCache(shareActivityRecord.getUid());
			if(Objects.nonNull(user)){
				shareActivityRecordVO.setPhone(user.getPhone());
			}

			shareActivityRecordVOList.add(shareActivityRecordVO);
		}

		return R.ok(shareActivityRecordVOList);
	}

}
