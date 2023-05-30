package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.vo.ShareActivityRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发起邀请活动记录(ShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareActivityRecordMapper  extends BaseMapper<ShareActivityRecord>{

	void addCountByUid(@Param("uid") Long uid, @Param("activityId") Integer activityId);

	void reduceAvailableCountByUid(@Param("uid") Long uid, @Param("count") Integer count, @Param("activityId") Integer activityId);

	List<ShareActivityRecordVO> queryList(ShareActivityRecordQuery shareActivityRecordQuery);

	Integer queryCount(ShareActivityRecordQuery shareActivityRecordQuery);
}
