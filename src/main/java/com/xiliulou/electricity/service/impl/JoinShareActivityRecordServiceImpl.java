package com.xiliulou.electricity.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.JoinShareActivityRecordMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
	ElectricityPayParamsService electricityPayParamsService;

	@Autowired
	ShareActivityRecordService shareActivityRecordService;

	@Autowired
	UserInfoService userInfoService;

	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public JoinShareActivityRecord queryByIdFromDB(Long id) {
		return this.joinShareActivityRecordMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param joinShareActivityRecord 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public JoinShareActivityRecord insert(JoinShareActivityRecord joinShareActivityRecord) {
		this.joinShareActivityRecordMapper.insert(joinShareActivityRecord);
		return joinShareActivityRecord;
	}

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
	public R decryptScene(String scene) {

		JSONObject jsonObject = JSONObject.parseObject(scene);
		String tenantIdResult = jsonObject.getString("tenantId");
		String sign = jsonObject.getString("scene");

		if (StringUtils.isEmpty(tenantIdResult) || StringUtils.isEmpty(sign)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		Integer tenantId = Integer.valueOf(tenantIdResult);

		//获取小程序appId
		ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
		if (Objects.isNull(electricityPayParams)) {
			log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
			return R.fail("未配置支付参数!");
		}

		String result = shareActivityRecordService.decrypt(sign);

		JSONObject jsonResult = JSONObject.parseObject(scene);
		Integer uid = Integer.valueOf(jsonResult.getString("uid"));
		String activityId = jsonResult.getString("activityId");
		return R.ok();
	}

	@Override
	public R joinActivity(Integer activityId, Integer uid) {

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("joinActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//1、自己点自己的链接，则返回自己该活动的参与人数及领劵规则 TODO
		if (Objects.equals(uid, user.getUid())) {

		}

		//校验用户
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		if (Objects.isNull(userInfo)) {
			log.error("order  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}

		//2、别人点击链接登录

		//2.1 判断此人是否参与过活动


		//2.2 判断此人是否首次购买月卡
		Boolean result = checkUserIsCard(userInfo);

		//已购买月卡,则直接返回首页
		if (result) {
			return R.ok();
		}

		//未购买月卡则添加用户参与记录

		return null;

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
				|| Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
			return false;
		}

		return true;

	}
}
