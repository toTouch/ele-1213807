package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.JoinShareActivityRecordMapper;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareActivityRecordService")
@Slf4j
public class JoinShareActivityRecordServiceImpl implements JoinShareActivityRecordService {
	@Resource
	private JoinShareActivityRecordMapper joinShareActivityRecordMapper;

	@Autowired
	private JoinShareActivityHistoryService joinShareActivityHistoryService;

	@Autowired
	UserInfoService userInfoService;

	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;

	@Autowired
	ShareActivityService shareActivityService;

	@Autowired
	UserService userService;

	/**
	 * 修改数据
	 *
	 * @param joinShareActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(JoinShareActivityRecord joinShareActivityRecord) {
		return this.joinShareActivityRecordMapper.updateById(joinShareActivityRecord);

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
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("joinActivity  ERROR! not found userInfo,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//查找活动
		ShareActivity shareActivity = shareActivityService.queryByStatus(activityId);
		if (Objects.isNull(shareActivity)) {
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

		//2、别人点击链接登录

		//2.1 判断此人是否首次购买月卡
		Boolean result = checkUserIsCard(userInfo);

		//已购买月卡,则直接返回首页
		if (result) {
			return R.fail("ELECTRICITY.00107", "您已购买过月卡");
		}

		//未购买月卡则添加用户参与记录
		//2.2 判断此人是否参与过活动
		JoinShareActivityRecord oldJoinShareActivityRecord = joinShareActivityRecordMapper.selectOne(new LambdaQueryWrapper<JoinShareActivityRecord>()
				.eq(JoinShareActivityRecord::getJoinUid, user.getUid()).eq(JoinShareActivityRecord::getTenantId, tenantId)
				.eq(JoinShareActivityRecord::getActivityId, activityId)
				.in(JoinShareActivityRecord::getStatus, JoinShareActivityRecord.STATUS_INIT));

		if (Objects.nonNull(oldJoinShareActivityRecord)) {
			if (Objects.equals(oldJoinShareActivityRecord.getUid(), uid)) {
				return R.ok();
			}
			//切换邀请用户
			oldJoinShareActivityRecord.setUid(uid);
			//过期时间可配置
			oldJoinShareActivityRecord.setStartTime(System.currentTimeMillis());
			oldJoinShareActivityRecord.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
			oldJoinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
			joinShareActivityRecordMapper.updateById(oldJoinShareActivityRecord);

			//修改被替换掉的历史记录状态
			JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndStatus(oldJoinShareActivityRecord.getId());
			if (Objects.nonNull(oldJoinShareActivityHistory)) {
				oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_REPLACE);
				oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
				joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
			}

			//新增邀请历史记录
			JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
			joinShareActivityHistory.setRecordId(oldJoinShareActivityRecord.getId());
			joinShareActivityHistory.setUid(uid);
			joinShareActivityHistory.setJoinUid(user.getUid());
			joinShareActivityHistory.setCreateTime(System.currentTimeMillis());
			joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
			joinShareActivityHistory.setStartTime(System.currentTimeMillis());
			joinShareActivityHistory.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
			joinShareActivityHistory.setTenantId(tenantId);
			joinShareActivityHistory.setActivityId(oldJoinShareActivityRecord.getActivityId());
			joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_INIT);
			joinShareActivityHistoryService.insert(joinShareActivityHistory);
			return R.ok();
		}

		JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
		joinShareActivityRecord.setUid(uid);
		joinShareActivityRecord.setJoinUid(user.getUid());
		joinShareActivityRecord.setCreateTime(System.currentTimeMillis());
		joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
		joinShareActivityRecord.setStartTime(System.currentTimeMillis());
		joinShareActivityRecord.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
		joinShareActivityRecord.setTenantId(tenantId);
		joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_INIT);
		joinShareActivityRecord.setActivityId(activityId);
		joinShareActivityRecordMapper.insert(joinShareActivityRecord);

		//新增邀请历史记录
		JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
		joinShareActivityHistory.setRecordId(joinShareActivityRecord.getId());
		joinShareActivityHistory.setUid(uid);
		joinShareActivityHistory.setJoinUid(user.getUid());
		joinShareActivityHistory.setCreateTime(System.currentTimeMillis());
		joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
		joinShareActivityHistory.setStartTime(System.currentTimeMillis());
		joinShareActivityHistory.setExpiredTime(System.currentTimeMillis() + shareActivity.getHours() * 60 * 60 * 1000L);
		joinShareActivityHistory.setTenantId(tenantId);
		joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_INIT);
		joinShareActivityHistory.setActivityId(joinShareActivityRecord.getActivityId());
		joinShareActivityHistoryService.insert(joinShareActivityHistory);

		return R.ok();

	}

	@Override
	public JoinShareActivityRecord queryByJoinUid(Long uid) {
		return joinShareActivityRecordMapper.selectOne(new LambdaQueryWrapper<JoinShareActivityRecord>()
				.eq(JoinShareActivityRecord::getJoinUid, uid).gt(JoinShareActivityRecord::getExpiredTime, System.currentTimeMillis())
				.eq(JoinShareActivityRecord::getStatus, JoinShareActivityRecord.STATUS_INIT));
	}

	@Override
	public void handelJoinShareActivityExpired() {
		//
		JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
		joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_FAIL);
		joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
		joinShareActivityRecordMapper.updateExpired(joinShareActivityRecord);

		JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
		joinShareActivityHistory.setStatus(JoinShareActivityRecord.STATUS_FAIL);
		joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
		joinShareActivityHistoryService.updateExpired(joinShareActivityHistory);

	}

	@Override
	public void updateByActivityId(JoinShareActivityRecord joinShareActivityRecord) {
		joinShareActivityRecordMapper.updateByActivityId(joinShareActivityRecord);
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
