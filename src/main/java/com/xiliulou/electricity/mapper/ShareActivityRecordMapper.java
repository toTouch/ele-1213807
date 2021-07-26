package com.xiliulou.electricity.mapper;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import java.util.List;

import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * 发起邀请活动记录(ShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareActivityRecordMapper  extends BaseMapper<ShareActivityRecord>{


    @Update("update t_share_activity_record set count=count+1,available_count=available_count+1 where uid =#{uid}")
	void addCountByUid(@Param("uid") Long uid);

	@Update("update t_share_activity_record set available_count=available_count-#{count} where uid =#{uid}")
	void reduceAvailableCountByUid(@Param("uid") Long uid, @Param("count") Integer count);

	List<ShareActivityRecord> queryList(ShareActivityRecordQuery shareActivityRecordQuery);

	Integer queryCount(ShareActivityRecordQuery shareActivityRecordQuery);
}
