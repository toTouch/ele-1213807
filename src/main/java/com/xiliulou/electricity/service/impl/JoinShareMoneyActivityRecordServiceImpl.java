package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.JoinShareMoneyActivityRecordMapper;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
	FranchiseeUserInfoService franchiseeUserInfoService;

	@Autowired
	ShareMoneyActivityService shareMoneyActivityService;

	@Autowired
	UserService userService;

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

	/*	//2、别人点击链接登录

		//2.1 判断此人是否首次购买月卡
		Boolean result = checkUserIsCard(userInfo);

		//已购买月卡,则直接返回首页
		if (result) {
			return R.fail("ELECTRICITY.00107", "您已购买过月卡");
		}*/

		//未购买月卡则添加用户参与记录
		//2.2 判断此人是否参与过活动
		JoinShareMoneyActivityRecord oldJoinShareMoneyActivityRecord = joinShareMoneyActivityRecordMapper.selectOne(new LambdaQueryWrapper<JoinShareMoneyActivityRecord>()
				.eq(JoinShareMoneyActivityRecord::getJoinUid, user.getUid()).eq(JoinShareMoneyActivityRecord::getTenantId, tenantId)
				.eq(JoinShareMoneyActivityRecord::getActivityId, activityId)
				.in(JoinShareMoneyActivityRecord::getStatus, JoinShareMoneyActivityRecord.STATUS_INIT));

		if (Objects.nonNull(oldJoinShareMoneyActivityRecord)) {
			if (Objects.equals(oldJoinShareMoneyActivityRecord.getUid(), uid)) {
				return R.ok();
			}
			//切换邀请用户
			oldJoinShareMoneyActivityRecord.setUid(uid);
			//过期时间可配置
			oldJoinShareMoneyActivityRecord.setStartTime(System.currentTimeMillis());
			oldJoinShareMoneyActivityRecord.setExpiredTime(System.currentTimeMillis() + shareMoneyActivity.getHours() * 60 * 60 * 1000L);
			oldJoinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
			joinShareMoneyActivityRecordMapper.updateById(oldJoinShareMoneyActivityRecord);

			//修改被替换掉的历史记录状态
			JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndStatus(oldJoinShareMoneyActivityRecord.getId());
			if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
				oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_REPLACE);
				oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
				joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
			}

			//新增邀请历史记录
			JoinShareMoneyActivityHistory joinShareMoneyActivityHistory = new JoinShareMoneyActivityHistory();
			joinShareMoneyActivityHistory.setRecordId(oldJoinShareMoneyActivityHistory.getId());
			joinShareMoneyActivityHistory.setUid(uid);
			joinShareMoneyActivityHistory.setJoinUid(user.getUid());
			joinShareMoneyActivityHistory.setCreateTime(System.currentTimeMillis());
			joinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
			joinShareMoneyActivityHistory.setStartTime(System.currentTimeMillis());
			joinShareMoneyActivityHistory.setExpiredTime(System.currentTimeMillis() + shareMoneyActivity.getHours() * 60 * 60 * 1000L);
			joinShareMoneyActivityHistory.setTenantId(tenantId);
			joinShareMoneyActivityHistory.setActivityId(oldJoinShareMoneyActivityRecord.getActivityId());
			joinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_INIT);
			joinShareMoneyActivityHistoryService.insert(joinShareMoneyActivityHistory);
			return R.ok();
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

	private Boolean checkUserIsCard(UserInfo userInfo) {

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(franchiseeUserInfo)) {
			return false;

		}

		//用户是否开通月卡
		if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
				&& Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
			return false;
		}

		return true;

	}
}
