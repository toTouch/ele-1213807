package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityRecord;
import org.apache.ibatis.annotations.Update;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareMoneyActivityRecordMapper extends BaseMapper<JoinShareMoneyActivityRecord>{



	@Update("update t_join_share_money_activity_record set status=#{status},update_time=#{updateTime} where activity_id=#{activityId} and status=1")
	void updateByActivityId(JoinShareMoneyActivityRecord joinShareMoneyActivityRecord);

	@Update("update t_join_share_money_activity_record set status=#{status},update_time=#{updateTime} where expired_time <= #{updateTime} and status =1")
	void updateExpired(JoinShareMoneyActivityRecord joinShareMoneyActivityRecord);
}
