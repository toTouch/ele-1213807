package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.JoinShareMoneyActivityHistoryMapper;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.JoinShareMoneyActivityHistoryVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityHistory)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareMoneyActivityHistoryService")
@Slf4j
public class JoinShareMoneyActivityHistoryServiceImpl implements JoinShareMoneyActivityHistoryService {
	@Resource
	private JoinShareMoneyActivityHistoryMapper joinShareMoneyActivityHistoryMapper;

	@Autowired
	UserService userService;


	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public JoinShareMoneyActivityHistory queryByIdFromDB(Long id) {
		return this.joinShareMoneyActivityHistoryMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param joinShareMoneyActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public JoinShareMoneyActivityHistory insert(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		this.joinShareMoneyActivityHistoryMapper.insert(joinShareMoneyActivityHistory);
		return joinShareMoneyActivityHistory;
	}

	/**
	 * 修改数据
	 *
	 * @param joinShareMoneyActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		return this.joinShareMoneyActivityHistoryMapper.updateById(joinShareMoneyActivityHistory);

	}

	@Override
	public JoinShareMoneyActivityHistory queryByRecordIdAndStatus(Long id) {
		return joinShareMoneyActivityHistoryMapper.selectOne(new LambdaQueryWrapper<JoinShareMoneyActivityHistory>()
				.eq(JoinShareMoneyActivityHistory::getRecordId,id).eq(JoinShareMoneyActivityHistory::getStatus,JoinShareMoneyActivityHistory.STATUS_INIT));
	}

	@Override
	public R userList(Integer activityId) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("joinActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery=new JsonShareMoneyActivityHistoryQuery();
		jsonShareMoneyActivityHistoryQuery.setActivityId(activityId);
		jsonShareMoneyActivityHistoryQuery.setUid(user.getUid());

		List<JoinShareMoneyActivityHistory>  joinShareMoneyActivityHistoryList= joinShareMoneyActivityHistoryMapper.queryList(jsonShareMoneyActivityHistoryQuery);

		if(ObjectUtil.isEmpty(joinShareMoneyActivityHistoryList)){
			return R.ok(joinShareMoneyActivityHistoryList);
		}

		List<JoinShareMoneyActivityHistoryVO>  joinShareMoneyActivityHistoryVOList=new ArrayList<>();

		for (JoinShareMoneyActivityHistory joinShareMoneyActivityHistory:joinShareMoneyActivityHistoryList) {

			JoinShareMoneyActivityHistoryVO joinShareMoneyActivityHistoryVO=new JoinShareMoneyActivityHistoryVO();
			BeanUtil.copyProperties(joinShareMoneyActivityHistory,joinShareMoneyActivityHistoryVO);


			User joinUser=userService.queryByUidFromCache(joinShareMoneyActivityHistory.getJoinUid());
			if(Objects.nonNull(joinUser)){
				joinShareMoneyActivityHistoryVO.setJoinPhone(joinUser.getPhone());
			}

			joinShareMoneyActivityHistoryVOList.add(joinShareMoneyActivityHistoryVO);
		}

		return R.ok(joinShareMoneyActivityHistoryVOList);
	}

	@Override
	public R queryList(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery) {
		List<JoinShareMoneyActivityHistory>  joinShareMoneyActivityHistoryList= joinShareMoneyActivityHistoryMapper.queryList(jsonShareMoneyActivityHistoryQuery);

		if(ObjectUtil.isEmpty(joinShareMoneyActivityHistoryList)){
			return R.ok(joinShareMoneyActivityHistoryList);
		}

		List<JoinShareMoneyActivityHistoryVO>  joinShareMoneyActivityHistoryVOList=new ArrayList<>();

		for (JoinShareMoneyActivityHistory joinShareMoneyActivityHistory:joinShareMoneyActivityHistoryList) {

			JoinShareMoneyActivityHistoryVO joinShareMoneyActivityHistoryVO=new JoinShareMoneyActivityHistoryVO();
			BeanUtil.copyProperties(joinShareMoneyActivityHistory,joinShareMoneyActivityHistoryVO);


			User joinUser=userService.queryByUidFromCache(joinShareMoneyActivityHistory.getJoinUid());
			if(Objects.nonNull(joinUser)){
				joinShareMoneyActivityHistoryVO.setJoinPhone(joinUser.getPhone());
			}

			joinShareMoneyActivityHistoryVOList.add(joinShareMoneyActivityHistoryVO);
		}

		return R.ok(joinShareMoneyActivityHistoryVOList);
	}

	@Override
	public void updateByActivityId(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		joinShareMoneyActivityHistoryMapper.updateByActivityId(joinShareMoneyActivityHistory);
	}

	@Override
	public void updateExpired(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		joinShareMoneyActivityHistoryMapper.updateExpired(joinShareMoneyActivityHistory);
	}


}
