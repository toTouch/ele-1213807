package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.JoinShareMoneyActivityRecordMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareMoneyActivityRecordService")
@Slf4j
public class JoinShareMoneyActivityRecordServiceImpl implements JoinShareMoneyActivityRecordService {
	@Resource
	private JoinShareMoneyActivityRecordMapper joinShareMoneyActivityRecordMapper;

	@Autowired
	private JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;

	@Autowired
	UserInfoService userInfoService;

	@Autowired
	ShareMoneyActivityService shareMoneyActivityService;

	@Autowired
	UserService userService;
	@Autowired
	UserBatteryMemberCardService userBatteryMemberCardService;
	@Autowired
	JoinShareActivityHistoryService joinShareActivityHistoryService;

	/**
	 * 修改数据
	 *
	 * @param joinShareMoneyActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(JoinShareMoneyActivityRecord joinShareMoneyActivityRecord) {
		return this.joinShareMoneyActivityRecordMapper.updateById(joinShareMoneyActivityRecord);

	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public R joinActivity(Integer activityId, Long uid) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("joinActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户是否可用
		UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("joinActivity  ERROR! not found userInfo,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//查找活动
		ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByStatus(activityId);
		if (Objects.isNull(shareMoneyActivity)) {
			log.error("joinActivity  ERROR! not found Activity ! ActivityId:{} ", activityId);
			return R.fail("ELECTRICITY.00106", "活动已下架");
		}

		//查找分享的用户
		User oldUser = userService.queryByUidFromCache(uid);
		if (Objects.isNull(oldUser)) {
			log.error("joinActivity  ERROR! not found oldUser ,uid :{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//1、自己点自己的链接，则返回自己该活动的参与人数及领劵规则
		if (Objects.equals(uid, user.getUid())) {
			return R.ok();
		}

		//检查是否重复扫描同一邀请人的二维码
		Pair<Boolean, String> sameInviterResult = joinShareMoneyActivityHistoryService.checkJoinedActivityFromSameInviter(user.getUid(), oldUser.getUid(), activityId.longValue());
		if(sameInviterResult.getLeft()){
			if(sameInviterResult.getRight().isEmpty()){
				return R.ok();
			}else{
				return R.fail("110208", sameInviterResult.getRight());
			}
		}
		log.info("start join share money activity, join uid = {}, inviter uid = {}, activity id = {}", user.getUid(), oldUser.getUid(), activityId);

		//3.0版本修改为邀请返券和邀请返现活动只能参加一个，且只是针对新用户参加
		//查询当前用户是否参与了邀请返现活动
		List<JoinShareMoneyActivityHistory> joinShareMoneyActivityHistories = joinShareMoneyActivityHistoryService.queryUserJoinedActivity(user.getUid(), tenantId);
		if(CollectionUtils.isNotEmpty(joinShareMoneyActivityHistories)){
			return R.fail("110206", "已参加过邀请返现活动");
		}
		//检查是否有参与邀请返券的活动
		List<JoinShareActivityHistory> joinShareActivityHistories = joinShareActivityHistoryService.queryUserJoinedActivity(user.getUid(), tenantId);
		if(CollectionUtils.isNotEmpty(joinShareActivityHistories)){
			return R.fail("110207", "已参加过邀请返现活动");
		}

		JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = new JoinShareMoneyActivityRecord();
		joinShareMoneyActivityRecord.setUid(uid);
		joinShareMoneyActivityRecord.setJoinUid(user.getUid());
		joinShareMoneyActivityRecord.setCreateTime(System.currentTimeMillis());
		joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
		joinShareMoneyActivityRecord.setStartTime(System.currentTimeMillis());
		joinShareMoneyActivityRecord.setExpiredTime(System.currentTimeMillis() + shareMoneyActivity.getHours() * 60 * 60 * 1000L);
		joinShareMoneyActivityRecord.setTenantId(tenantId);
		joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_INIT);
		joinShareMoneyActivityRecord.setActivityId(activityId);
		joinShareMoneyActivityRecordMapper.insert(joinShareMoneyActivityRecord);

		//新增邀请历史记录
		JoinShareMoneyActivityHistory joinShareMoneyActivityHistory = new JoinShareMoneyActivityHistory();
		joinShareMoneyActivityHistory.setRecordId(joinShareMoneyActivityRecord.getId());
		joinShareMoneyActivityHistory.setUid(uid);
		joinShareMoneyActivityHistory.setJoinUid(user.getUid());
		joinShareMoneyActivityHistory.setCreateTime(System.currentTimeMillis());
		joinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
		joinShareMoneyActivityHistory.setStartTime(System.currentTimeMillis());
		joinShareMoneyActivityHistory.setExpiredTime(System.currentTimeMillis() + shareMoneyActivity.getHours() * 60 * 60 * 1000L);
		joinShareMoneyActivityHistory.setTenantId(tenantId);
		joinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_INIT);
		joinShareMoneyActivityHistory.setActivityId(joinShareMoneyActivityRecord.getActivityId());
		joinShareMoneyActivityHistoryService.insert(joinShareMoneyActivityHistory);

		return R.ok();

	}

	@Override
	public JoinShareMoneyActivityRecord queryByJoinUid(Long uid) {
		return joinShareMoneyActivityRecordMapper.selectOne(new LambdaQueryWrapper<JoinShareMoneyActivityRecord>()
				.eq(JoinShareMoneyActivityRecord::getJoinUid, uid).gt(JoinShareMoneyActivityRecord::getExpiredTime, System.currentTimeMillis())
				.eq(JoinShareMoneyActivityRecord::getStatus, JoinShareMoneyActivityRecord.STATUS_INIT));
	}

	@Override
	public void handelJoinShareMoneyActivityExpired() {
		//
		JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = new JoinShareMoneyActivityRecord();
		joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_FAIL);
		joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
		joinShareMoneyActivityRecordMapper.updateExpired(joinShareMoneyActivityRecord);

		JoinShareMoneyActivityHistory joinShareMoneyActivityHistory = new JoinShareMoneyActivityHistory();
		joinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityRecord.STATUS_FAIL);
		joinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
		joinShareMoneyActivityHistoryService.updateExpired(joinShareMoneyActivityHistory);

	}

	@Override
	public void updateByActivityId(JoinShareMoneyActivityRecord joinShareMoneyActivityRecord) {
		joinShareMoneyActivityRecordMapper.updateByActivityId(joinShareMoneyActivityRecord);
	}

	@Slave
	@Override
	public List<JoinShareMoneyActivityRecord> queryByUidAndActivityId(Long uid, Long activityId) {
		List<JoinShareMoneyActivityRecord> joinShareMoneyActivityRecords = joinShareMoneyActivityRecordMapper.selectList(new LambdaQueryWrapper<JoinShareMoneyActivityRecord>().eq(JoinShareMoneyActivityRecord::getUid, uid)
				.eq(JoinShareMoneyActivityRecord::getActivityId, activityId));

		return joinShareMoneyActivityRecords;
	}

	private Boolean checkUserIsCard(UserInfo userInfo) {

		//是否缴纳押金，是否绑定电池
//		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

		//未找到用户
		if (Objects.isNull(userBatteryMemberCard)) {
			return false;

		}

		//用户是否开通月卡
		if (Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
				&& Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
			return false;
		}

		return true;

	}
}
