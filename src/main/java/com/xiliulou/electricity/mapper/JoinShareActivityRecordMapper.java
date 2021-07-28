package com.xiliulou.electricity.mapper;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import java.util.List;

import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareActivityRecordMapper  extends BaseMapper<JoinShareActivityRecord>{

	List<JoinShareActivityRecord> getJoinShareActivityExpired( @Param("expiredTime") long expiredTime,@Param("offset")  int offset, @Param("size") int size);

	@Update("update t_join_share_activity_record set status=#{status},update_time=#{updateTime} where activity_id=#{activityId} and status=1")
	void updateByActivityId(JoinShareActivityRecord joinShareActivityRecord);
}
