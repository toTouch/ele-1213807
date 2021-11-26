package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserAmount;
import com.xiliulou.electricity.mapper.UserAmountMapper;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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


	@Override
	public UserAmount queryByAgentFromDB(Long id) {
		return null;
	}

	@Override
	public UserAmount insert(UserAmount userAmount) {
		return null;
	}

	@Override
	public Integer update(UserAmount userAmount) {
		return null;
	}

	@Override
	public R queryByUid() {

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("order  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		UserAmount userAmount=userAmountMapper.selectOne(new LambdaQueryWrapper<UserAmount>().eq(UserAmount::getUid,user.getUid()));
		return R.ok(userAmount);
	}
}
