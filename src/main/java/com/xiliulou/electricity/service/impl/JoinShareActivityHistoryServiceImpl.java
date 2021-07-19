package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.JoinShareActivityHistoryMapper;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.JoinShareActivityHistoryVO;
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
@Service("joinShareActivityHistoryService")
@Slf4j
public class JoinShareActivityHistoryServiceImpl implements JoinShareActivityHistoryService {
	@Resource
	private JoinShareActivityHistoryMapper joinShareActivityHistoryMapper;

	@Autowired
	UserService userService;


	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public JoinShareActivityHistory queryByIdFromDB(Long id) {
		return this.joinShareActivityHistoryMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param joinShareActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public JoinShareActivityHistory insert(JoinShareActivityHistory joinShareActivityHistory) {
		this.joinShareActivityHistoryMapper.insert(joinShareActivityHistory);
		return joinShareActivityHistory;
	}

	/**
	 * 修改数据
	 *
	 * @param joinShareActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(JoinShareActivityHistory joinShareActivityHistory) {
		return this.joinShareActivityHistoryMapper.updateById(joinShareActivityHistory);

	}

	@Override
	public JoinShareActivityHistory queryByRecordIdAndStatus(Long id) {
		return joinShareActivityHistoryMapper.selectOne(new LambdaQueryWrapper<JoinShareActivityHistory>()
				.eq(JoinShareActivityHistory::getRecordId,id).eq(JoinShareActivityHistory::getUpdateTime,JoinShareActivityHistory.STATUS_INIT));
	}

	@Override
	public R userList() {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("joinActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}


		List<JoinShareActivityHistory>  joinShareActivityHistoryList= joinShareActivityHistoryMapper.selectList(new LambdaQueryWrapper<JoinShareActivityHistory>()
				.eq(JoinShareActivityHistory::getUid,user.getUid()));

		if(ObjectUtil.isEmpty(joinShareActivityHistoryList)){
			return R.ok(joinShareActivityHistoryList);
		}

		List<JoinShareActivityHistoryVO>  joinShareActivityHistoryVOList=new ArrayList<>();

		for (JoinShareActivityHistory joinShareActivityHistory:joinShareActivityHistoryList) {

			JoinShareActivityHistoryVO joinShareActivityHistoryVO=new JoinShareActivityHistoryVO();
			BeanUtil.copyProperties(joinShareActivityHistory,joinShareActivityHistoryVO);


			User joinUser=userService.queryByUidFromCache(joinShareActivityHistory.getJoinUid());
			if(Objects.nonNull(joinUser)){
				joinShareActivityHistoryVO.setJoinPhone(joinUser.getPhone());
			}

			joinShareActivityHistoryVOList.add(joinShareActivityHistoryVO);
		}

		return R.ok(joinShareActivityHistoryVOList);
	}

}
