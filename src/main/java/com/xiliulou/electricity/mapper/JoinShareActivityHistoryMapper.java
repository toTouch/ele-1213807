package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.query.ElectricityCabinetOrderExcelQuery;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.vo.FinalJoinShareActivityHistoryVo;
import com.xiliulou.electricity.vo.JoinShareActivityHistoryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareActivityHistoryMapper extends BaseMapper<JoinShareActivityHistory>{



    /**
     * 通过实体作为筛选条件查询
     *
     * @param jsonShareActivityHistoryQuery 实例对象
     * @return 对象列表
     */
    List<JoinShareActivityHistoryVO> queryList(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

	@Update("update t_join_share_activity_history set status=#{status},update_time=#{updateTime} where activity_id=#{activityId} and status=1")
	void updateByActivityId(JoinShareActivityHistory joinShareActivityHistory);

	@Update("update t_join_share_activity_history set status=#{status},update_time=#{updateTime} where expired_time <= #{updateTime} and status =1")
	void updateExpired(JoinShareActivityHistory joinShareActivityHistory);
	
	FinalJoinShareActivityHistoryVo queryFinalHistoryByJoinUid(@Param("joinUid") Long joinUid,
			@Param("tenantId") Integer tenantId);
	
	Long queryCount(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);
	
	List<ElectricityCabinetOrderExcelQuery> queryExportExcel(
			JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

	List<JoinShareActivityHistoryVO> queryParticipants(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

	Long queryParticipantsCount(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

	List<JoinShareActivityHistory> queryUserJoinedActivity(@Param("joinUid") Long joinUid, @Param("tenantId") Integer tenantId);
}
