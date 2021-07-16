package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.mapper.JoinShareActivityHistoryMapper;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;


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

}
