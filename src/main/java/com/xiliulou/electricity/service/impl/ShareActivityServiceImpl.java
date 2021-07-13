package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityRule;
import com.xiliulou.electricity.entity.ActivityBindUrl;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ShareActivityMapper;
import com.xiliulou.electricity.query.ActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ActivityBindUrlQuery;
import com.xiliulou.electricity.query.ActivityQuery;
import com.xiliulou.electricity.query.ActivityBindCouponQuery;
import com.xiliulou.electricity.service.ShareActivityRuleService;
import com.xiliulou.electricity.service.ActivityBindUrlService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ActivityBindUrlVO;
import com.xiliulou.electricity.vo.ActivityVO;
import com.xiliulou.electricity.vo.CouponVO;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("shareActivityService")
@Slf4j
public class ShareActivityServiceImpl implements ShareActivityService {
	@Resource
	private ShareActivityMapper shareActivityMapper;

	@Autowired
	RedisService redisService;

	@Autowired
	UserInfoService userInfoService;

	@Autowired
	FranchiseeService franchiseeService;

	@Autowired
	UserService userService;

	@Autowired
	private ShareActivityRuleService shareActivityRuleService;

	@Autowired
	private CouponService couponService;

	@Autowired
	ActivityBindUrlService activityBindUrlService;

	@Autowired
	ElectricityCabinetFileService electricityCabinetFileService;

	@Autowired
	StorageConfig storageConfig;

	@Qualifier("aliyunOssService")
	@Autowired
	StorageService storageService;


	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ShareActivity queryByIdFromDB(Integer id) {
		return this.shareActivityMapper.selectById(id);
	}

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ShareActivity queryByIdFromCache(Integer id) {
		//先查缓存
		ShareActivity shareActivityCache = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ACTIVITY_CACHE + id, ShareActivity.class);
		if (Objects.nonNull(shareActivityCache)) {
			return shareActivityCache;
		}

		//缓存没有再查数据库
		ShareActivity shareActivity = shareActivityMapper.selectById(id);
		if (Objects.isNull(shareActivity)) {
			return null;
		}

		//放入缓存
		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ACTIVITY_CACHE + id, shareActivity);
		return shareActivity;
	}

	/**
	 * 新增数据
	 *
	 * @param activityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R insert(ActivityAddAndUpdateQuery activityAddAndUpdateQuery) {
		//创建账号
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("Coupon  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断参数
		if(Objects.equals(user.getType(),User.TYPE_USER_FRANCHISEE)){
			if(Objects.isNull(activityAddAndUpdateQuery.getFranchiseeId())){
				log.error("Activity  ERROR! not found FranchiseeId ");
				return R.fail("ELECTRICITY.0094", "加盟商不能为空");
			}
		}else {
			if(Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.FRANCHISEE)
			||Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.FRANCHISEE_OUT_URL))  {
				if(Objects.isNull(activityAddAndUpdateQuery.getFranchiseeId())){
					log.error("Activity  ERROR! not found FranchiseeId ");
					return R.fail("ELECTRICITY.0094", "加盟商不能为空");
				}
			}
		}


		if (Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.SYSTEM)
				|| Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.SYSTEM_OUT_URL)) {
			//判断是否有自营首页推荐
			if (Objects.equals(activityAddAndUpdateQuery.getShowWay(), ShareActivity.SHOW_HOME)) {
				Integer count = shareActivityMapper.selectCount(new LambdaQueryWrapper<ShareActivity>().eq(ShareActivity::getShowWay, ShareActivity.SHOW_HOME)
						.in(ShareActivity::getType, ShareActivity.SYSTEM, ShareActivity.SYSTEM_OUT_URL).eq(ShareActivity::getDelFlg, ShareActivity.DEL_NORMAL));
				if (count > 0) {
					return R.fail("ELECTRICITY.0078", "首页推荐已存在，不能重复添加");
				}
			}
		} else {
			//判断是否有加盟商首页推荐
			if (Objects.equals(activityAddAndUpdateQuery.getShowWay(), ShareActivity.SHOW_HOME)) {
				Integer count = shareActivityMapper.selectCount(new LambdaQueryWrapper<ShareActivity>().eq(ShareActivity::getShowWay, ShareActivity.SHOW_HOME)
						.in(ShareActivity::getType, ShareActivity.FRANCHISEE, ShareActivity.FRANCHISEE_OUT_URL).eq(ShareActivity::getDelFlg, ShareActivity.DEL_NORMAL));
				if (count > 0) {
					return R.fail("ELECTRICITY.0078", "首页推荐已存在，不能重复添加");
				}
			}
		}

		List<ActivityBindUrlQuery> activityBindUrlQueryList = activityAddAndUpdateQuery.getActivityBindUrlQueryList();
		List<ActivityBindCouponQuery> activityBindCouponQueryList = activityAddAndUpdateQuery.getActivityBindCouponQueryList();

		ShareActivity shareActivity = new ShareActivity();
		BeanUtil.copyProperties(activityAddAndUpdateQuery, shareActivity);
		shareActivity.setUid(user.getUid());
		shareActivity.setUserName(user.getUsername());
		shareActivity.setCreateTime(System.currentTimeMillis());
		shareActivity.setUpdateTime(System.currentTimeMillis());

		//加盟商活动需要申请
		if(Objects.equals(user.getType(),User.TYPE_USER_FRANCHISEE)){
			shareActivity.setStatus(ShareActivity.STATUS_PENDING);
		}

		int insert = shareActivityMapper.insert(shareActivity);
		DbUtils.dbOperateSuccessThen(insert, () -> {

			//添加优惠,只有小活动有优惠券
			if (ObjectUtil.isNotEmpty(activityBindCouponQueryList) && (Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.SYSTEM)
					|| Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.FRANCHISEE))) {
				for (ActivityBindCouponQuery activityBindCouponQuery : activityBindCouponQueryList) {
					ShareActivityRule.ActivityBindCouponBuilder activityBindCouponBuild = ShareActivityRule.builder()
							.activityId(shareActivity.getId())
							.couponId(activityBindCouponQuery.getCouponId())
							.couponCount(activityBindCouponQuery.getCouponCount())
							.discountType(activityBindCouponQuery.getDiscountType())
							.createTime(System.currentTimeMillis())
							.updateTime(System.currentTimeMillis());
					ShareActivityRule shareActivityRule = activityBindCouponBuild.build();
					shareActivityRuleService.insert(shareActivityRule);
				}
			}

			//外部活动添加链接
			if (ObjectUtil.isNotEmpty(activityBindUrlQueryList) && (Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.SYSTEM_OUT_URL)
					|| Objects.equals(activityAddAndUpdateQuery.getType(), ShareActivity.FRANCHISEE_OUT_URL))) {
				for (ActivityBindUrlQuery activityBindUrlQuery : activityBindUrlQueryList) {
					ActivityBindUrl activityBindUrl = new ActivityBindUrl();
					BeanUtil.copyProperties(activityBindUrlQuery, activityBindUrl);
					activityBindUrl.setActivityId(shareActivity.getId());
					activityBindUrl.setCreateTime(System.currentTimeMillis());
					activityBindUrl.setUpdateTime(System.currentTimeMillis());
					activityBindUrlService.insert(activityBindUrl);
				}
			}

			return null;
		});

		if (insert > 0) {
			return R.ok(shareActivity.getId());
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	/**
	 * 修改数据(暂只支持上下架）
	 *
	 * @param activityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R update(ActivityAddAndUpdateQuery activityAddAndUpdateQuery) {
		ShareActivity oldShareActivity = queryByIdFromCache(activityAddAndUpdateQuery.getId());
		if (Objects.isNull(oldShareActivity)) {
			log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", activityAddAndUpdateQuery.getId());
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		BeanUtil.copyProperties(activityAddAndUpdateQuery, oldShareActivity);
		oldShareActivity.setUpdateTime(System.currentTimeMillis());

		int update = shareActivityMapper.updateById(oldShareActivity);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ACTIVITY_CACHE + oldShareActivity.getId(), oldShareActivity);

			return null;
		});

		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R delete(Integer id) {
		ShareActivity oldShareActivity = queryByIdFromCache(id);
		if (Objects.isNull(oldShareActivity)) {
			log.error("delete Activity  ERROR! not found Activity ! ActivityId:{} ", id);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		int delete = shareActivityMapper.deleteById(id);
		DbUtils.dbOperateSuccessThen(delete, () -> {
			//删除缓存
			redisService.delete(ElectricityCabinetConstant.CACHE_ACTIVITY_CACHE + id);
			return null;
		});

		if (delete > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	public R queryList(ActivityQuery activityQuery) {
		List<ShareActivity> shareActivityList = shareActivityMapper.queryList(activityQuery);
		if (ObjectUtil.isEmpty(shareActivityList)) {
			return R.ok();
		}
		//活动图片
		List<ActivityVO> activityVOList = new ArrayList<>();
		for (ShareActivity shareActivity : shareActivityList) {
			ActivityVO activityVO = new ActivityVO();
			BeanUtil.copyProperties(shareActivity, activityVO);

			//图片
			List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService
					.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_ACTIVITY, storageConfig.getIsUseOSS());

			if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
				activityVOList.add(activityVO);
				continue;
			}

			List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
			getElectricityCabinetFile(activityVOList, activityVO, electricityCabinetFileList, electricityCabinetFiles);
		}
		return R.ok(activityVOList);
	}

	private void getElectricityCabinetFile(List<ActivityVO> activityVOList, ActivityVO activityVO, List<ElectricityCabinetFile> electricityCabinetFileList, List<ElectricityCabinetFile> electricityCabinetFiles) {
		getElectricityCabinetFiles(activityVO, electricityCabinetFileList, electricityCabinetFiles);
		activityVOList.add(activityVO);
	}

	private void getElectricityCabinetFiles(ActivityVO activityVO, List<ElectricityCabinetFile> electricityCabinetFileList, List<ElectricityCabinetFile> electricityCabinetFiles) {
		for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList) {
			if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
				electricityCabinetFile.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), electricityCabinetFile.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
			}
			electricityCabinetFiles.add(electricityCabinetFile);
		}
		activityVO.setElectricityCabinetFiles(electricityCabinetFiles);
	}

	@Override
	public R queryInfo(Integer id, Boolean flag) {
		ShareActivity shareActivity = queryByIdFromCache(id);
		if (Objects.isNull(shareActivity)) {
			log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		ActivityVO activityVO = new ActivityVO();
		BeanUtil.copyProperties(shareActivity, activityVO);

		//图片
		List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService
				.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_ACTIVITY, storageConfig.getIsUseOSS());

		if (ObjectUtil.isNotEmpty(electricityCabinetFileList)) {

			List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
			getElectricityCabinetFiles(activityVO, electricityCabinetFileList, electricityCabinetFiles);
		}

		//小活动
		if (Objects.equals(shareActivity.getType(), ShareActivity.SYSTEM)
				|| Objects.equals(shareActivity.getType(), ShareActivity.FRANCHISEE)) {
			getCouponVOList(activityVO, flag);
			return R.ok(activityVO);
		}

		//外部活动
		if (Objects.equals(shareActivity.getType(), ShareActivity.SYSTEM_OUT_URL)
				|| Objects.equals(shareActivity.getType(), ShareActivity.FRANCHISEE_OUT_URL)) {
			List<ActivityBindUrl> activityBindUrlList = activityBindUrlService.queryByActivityId(shareActivity.getId());
			if (ObjectUtil.isEmpty(activityBindUrlList)) {
				return R.ok(activityVO);
			}

			//图片
			List<ElectricityCabinetFile> electricityCabinetFileList2 = electricityCabinetFileService
					.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_OUT_ACTIVITY, storageConfig.getIsUseOSS());

			List<ActivityBindUrlVO> activityBindUrlVOList = new ArrayList<>();
			for (ActivityBindUrl activityBindUrl : activityBindUrlList) {
				ActivityBindUrlVO activityBindUrlVO = new ActivityBindUrlVO();
				BeanUtil.copyProperties(activityBindUrl, activityBindUrlVO);

				if (ObjectUtil.isEmpty(electricityCabinetFileList2)) {
					activityBindUrlVOList.add(activityBindUrlVO);
					continue;
				}
				List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
				for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList2) {
					if (Objects.equals(electricityCabinetFile.getName(), activityBindUrl.getPageBannerImageName())) {
						if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
							electricityCabinetFile.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), electricityCabinetFile.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
						}
						electricityCabinetFiles.add(electricityCabinetFile);
					}
				}
				activityBindUrlVO.setElectricityCabinetFiles(electricityCabinetFiles);
				activityBindUrlVOList.add(activityBindUrlVO);
			}

			activityVO.setActivityBindUrlVOList(activityBindUrlVOList);
			return R.ok(activityVO);

		}


		return R.ok(activityVO);
	}

	private void getCouponVOList(ActivityVO activityVO, Boolean flag) {
		List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(activityVO.getId());
		if (ObjectUtil.isEmpty(shareActivityRuleList)) {
			return;
		}

		List<CouponVO> couponVOList = new ArrayList<>();
		for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
			//优惠券名称
			Coupon coupon = couponService.queryByIdFromCache(shareActivityRule.getCouponId());
			if (Objects.isNull(coupon)) {
				continue;
			}
			CouponVO couponVO = new CouponVO();
			BeanUtil.copyProperties(coupon, couponVO);
			couponVO.setCouponCount(shareActivityRule.getCouponCount());
			couponVO.setReceiveCount(shareActivityRule.getReceiveCount());
			if (flag) {
				if (Objects.equals(coupon.getStatus(), Coupon.STATUS_ON)) {
					if (Objects.equals(Coupon.TYPE_TIME_DAY, coupon.getTimeType())) {
						couponVOList.add(couponVO);
					} else if (coupon.getEndTime() > System.currentTimeMillis()) {
						couponVOList.add(couponVO);
					}
				}
			} else {
				couponVOList.add(couponVO);
			}

		}
		activityVO.setCouponVOList(couponVOList);
	}

	@Override
	public R queryCount(ActivityQuery activityQuery) {
		Integer count = shareActivityMapper.queryCount(activityQuery);
		return R.ok(count);
	}



	@Override
	public R franchiseeHome() {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//用户加盟商
		Franchisee franchisee = franchiseeService.queryByCid(user.getCid());
		if (Objects.isNull(franchisee)) {
			log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ", user.getCid());
			//未找到加盟商默认西安，西安也找不到再提示找不到 其余客服需要换  TODO
			franchisee = franchiseeService.queryByCid(283);
			if (Objects.isNull(franchisee)) {
				return R.ok();
			}
		}

		Long now = System.currentTimeMillis();
		//查询加盟活动
		ShareActivity shareActivity = shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>()
				.in(ShareActivity::getType, ShareActivity.FRANCHISEE, ShareActivity.FRANCHISEE_OUT_URL)
				.eq(ShareActivity::getShowWay, ShareActivity.SHOW_HOME)
				.eq(ShareActivity::getDelFlg, ShareActivity.DEL_NORMAL)
				.eq(ShareActivity::getStatus, ShareActivity.STATUS_ON)
				.eq(ShareActivity::getFranchiseeId,franchisee.getId())
				.lt(ShareActivity::getStartTime, now)
				.gt(ShareActivity::getEndTime, now));
		if (Objects.isNull(shareActivity)) {
			return R.ok();
		}
		ActivityVO activityVO = new ActivityVO();
		BeanUtil.copyProperties(shareActivity, activityVO);

		//图片
		List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService
				.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_ACTIVITY, storageConfig.getIsUseOSS());

		if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
			return R.ok(activityVO);
		}

		List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
		getElectricityCabinetFiles(activityVO, electricityCabinetFileList, electricityCabinetFiles);
		return R.ok(activityVO);
	}

	@Override
	public R systemHome() {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		Long now = System.currentTimeMillis();
		//查询系统活动
		ShareActivity shareActivity = shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>()
				.in(ShareActivity::getType, ShareActivity.SYSTEM, ShareActivity.SYSTEM_OUT_URL)
				.eq(ShareActivity::getShowWay, ShareActivity.SHOW_HOME)
				.eq(ShareActivity::getDelFlg, ShareActivity.DEL_NORMAL)
				.eq(ShareActivity::getStatus, ShareActivity.STATUS_ON)
				.lt(ShareActivity::getStartTime, now)
				.gt(ShareActivity::getEndTime, now));
		if (Objects.isNull(shareActivity)) {
			return R.ok();
		}
		ActivityVO activityVO = new ActivityVO();
		BeanUtil.copyProperties(shareActivity, activityVO);

		//图片
		List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService
				.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_ACTIVITY, storageConfig.getIsUseOSS());

		if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
			return R.ok(activityVO);
		}

		List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
		getElectricityCabinetFiles(activityVO, electricityCabinetFileList, electricityCabinetFiles);
		return R.ok(activityVO);
	}

	@Override
	public R franchiseeCenter() {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//用户加盟商
		Franchisee franchisee = franchiseeService.queryByCid(user.getCid());
		if (Objects.isNull(franchisee)) {
			log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ", user.getCid());
			//未找到加盟商默认西安，西安也找不到再提示找不到 其余客服需要换  TODO
			franchisee = franchiseeService.queryByCid(283);
			if (Objects.isNull(franchisee)) {
				return R.ok();
			}
		}

		Long now = System.currentTimeMillis();
		//查询系统活动
		List<ShareActivity> shareActivityList = shareActivityMapper.selectList(new LambdaQueryWrapper<ShareActivity>()
				.in(ShareActivity::getType, ShareActivity.FRANCHISEE, ShareActivity.FRANCHISEE_OUT_URL)
				.eq(ShareActivity::getDelFlg, ShareActivity.DEL_NORMAL)
				.eq(ShareActivity::getStatus, ShareActivity.STATUS_ON)
				.eq(ShareActivity::getFranchiseeId,franchisee.getId())
				.lt(ShareActivity::getStartTime, now)
				.gt(ShareActivity::getEndTime, now));
		if (ObjectUtil.isEmpty(shareActivityList)) {
			return R.ok();
		}

		List<ActivityVO> activityVOList = new ArrayList<>();
		for (ShareActivity shareActivity : shareActivityList) {

			ActivityVO activityVO = new ActivityVO();
			BeanUtil.copyProperties(shareActivity, activityVO);

			//图片
			List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService
					.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_ACTIVITY, storageConfig.getIsUseOSS());

			if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
				activityVOList.add(activityVO);
				continue;
			}

			List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
			getElectricityCabinetFiles(activityVO, electricityCabinetFileList, electricityCabinetFiles);
			activityVOList.add(activityVO);
		}
		return R.ok(activityVOList);
	}

	@Override
	public R systemCenter() {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		Long now = System.currentTimeMillis();
		//查询系统活动
		List<ShareActivity> shareActivityList = shareActivityMapper.selectList(new LambdaQueryWrapper<ShareActivity>()
				.in(ShareActivity::getType, ShareActivity.SYSTEM, ShareActivity.SYSTEM_OUT_URL)
				.eq(ShareActivity::getDelFlg, ShareActivity.DEL_NORMAL)
				.eq(ShareActivity::getStatus, ShareActivity.STATUS_ON)
				.lt(ShareActivity::getStartTime, now)
				.gt(ShareActivity::getEndTime, now));
		if (ObjectUtil.isEmpty(shareActivityList)) {
			return R.ok();
		}

		List<ActivityVO> activityVOList = new ArrayList<>();
		for (ShareActivity shareActivity : shareActivityList) {

			ActivityVO activityVO = new ActivityVO();
			BeanUtil.copyProperties(shareActivity, activityVO);

			//图片
			List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService
					.queryByActivityId(activityVO.getId(), ElectricityCabinetFile.TYPE_ACTIVITY, storageConfig.getIsUseOSS());

			if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
				activityVOList.add(activityVO);
				continue;
			}

			List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
			getElectricityCabinetFiles(activityVO, electricityCabinetFileList, electricityCabinetFiles);
			activityVOList.add(activityVO);
		}
		return R.ok(activityVOList);
	}



	@Override
	public R activityInfo(Integer id) {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//活动浏览次数
		Long count = (Long) redisTemplate.opsForValue().get(ElectricityCabinetConstant.CACHE_LOOK_ACTIVITY + id);
		if (Objects.isNull(count)) {
			count = 0L;
		}
		redisTemplate.opsForValue().set(ElectricityCabinetConstant.CACHE_LOOK_ACTIVITY + id, count + 1L);

		return queryInfo(id, true);
	}

	@Override
	public void handelActivityExpired() {
		List<ShareActivity> shareActivityList = shareActivityMapper.getExpiredActivity(System.currentTimeMillis());
		if (!DataUtil.collectionIsUsable(shareActivityList)) {
			return;
		}
		for (ShareActivity shareActivity : shareActivityList) {
			shareActivity.setStatus(Coupon.STATUS_OFF);
			shareActivity.setUpdateTime(System.currentTimeMillis());
			int update = shareActivityMapper.updateById(shareActivity);
			DbUtils.dbOperateSuccessThen(update, () -> {
				redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ACTIVITY_CACHE + shareActivity.getId(), shareActivity);
				return null;
			});
		}
	}

}

