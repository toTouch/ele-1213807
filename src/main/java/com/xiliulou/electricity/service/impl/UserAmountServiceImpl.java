package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserAmount;
import com.xiliulou.electricity.entity.UserAmountHistory;
import com.xiliulou.electricity.mapper.UserAmountMapper;
import com.xiliulou.electricity.query.UserAmountQuery;
import com.xiliulou.electricity.service.UserAmountHistoryService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserAmountVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * (AgentAmount)表服务实现类
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
@Service("userAmountService")
@Slf4j
public class UserAmountServiceImpl implements UserAmountService {

	@Resource
	UserAmountMapper userAmountMapper;

	@Autowired
	UserAmountHistoryService userAmountHistoryService;

	@Override
	public UserAmount queryByUid(Long uid) {
		return userAmountMapper.selectOne(new LambdaQueryWrapper<UserAmount>().eq(UserAmount::getUid, uid)
				.eq(UserAmount::getDelFlg, UserAmount.DEL_NORMAL));
	}

	@Override
	public UserAmount insert(UserAmount userAmount) {
		userAmountMapper.insert(userAmount);
		return userAmount;
	}

	@Override
	public Integer update(UserAmount userAmount) {
		Integer update=userAmountMapper.updateById(userAmount);
		return update;
	}

	@Override
	public R queryByUid() {

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("order  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		UserAmount userAmount = userAmountMapper.selectOne(new LambdaQueryWrapper<UserAmount>().eq(UserAmount::getUid, user.getUid()));
		return R.ok(userAmount);
	}

	@Override
	public void handleAmount(Long uid, Long joinUid, BigDecimal money,Integer tenantId) {


		UserAmount userAmount=queryByUid(uid);
		if(Objects.isNull(userAmount)){
			userAmount=new UserAmount();
			userAmount.setUid(uid);
			userAmount.setBalance(money);
			userAmount.setTotalIncome(money);
			userAmount.setTenantId(tenantId);
			userAmount.setCreateTime(System.currentTimeMillis());
			userAmount.setUpdateTime(System.currentTimeMillis());
			insert(userAmount);


		}else {

			userAmount.setBalance(userAmount.getBalance().add(money));
			userAmount.setTotalIncome(userAmount.getTotalIncome().add(money));
			userAmount.setUpdateTime(System.currentTimeMillis());
			update(userAmount);
		}

		//新增余额流水
		UserAmountHistory userAmountHistory=new UserAmountHistory();
		userAmountHistory.setType(UserAmountHistory.TYPE_SHARE_ACTIVITY);
		userAmountHistory.setUid(uid);
		userAmountHistory.setJoinUid(joinUid);
		userAmountHistory.setAmount(money);
		userAmountHistory.setCreateTime(System.currentTimeMillis());
		userAmountHistory.setTenantId(userAmount.getTenantId());
		userAmountHistoryService.insert(userAmountHistory);
	}

	@Override
	public R queryList(UserAmountQuery userAmountQuery) {
		List<UserAmountVO> userAmountVOList=userAmountMapper.queryList(userAmountQuery);
		return R.ok(userAmountVOList);
	}

	@Override
	public R queryCount(UserAmountQuery userAmountQuery) {
		Integer count=userAmountMapper.queryCount(userAmountQuery);
		return R.ok(count);
	}
}
