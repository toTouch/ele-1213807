package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.query.JoinShareMoneyActivityHistoryExcelQuery;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.vo.FinalJoinShareMoneyActivityHistoryVo;
import com.xiliulou.electricity.vo.JoinShareMoneyActivityHistoryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareMoneyActivityHistoryMapper extends BaseMapper<JoinShareMoneyActivityHistory>{



    /**
     * 通过实体作为筛选条件查询
     *
     * @param jsonShareMoneyActivityHistoryQuery 实例对象
     * @return 对象列表
     */
    List<JoinShareMoneyActivityHistoryVO> queryList(
		    JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery);

	Integer queryCount(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery);

    @Update("update t_join_share_money_activity_history set status=#{status},update_time=#{updateTime} where activity_id=#{activityId} and status=1")
	void updateByActivityId(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory);

	@Update("update t_join_share_money_activity_history set status=#{status},update_time=#{updateTime} where expired_time <= #{updateTime} and status =1")
	void updateExpired(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory);
	
	FinalJoinShareMoneyActivityHistoryVo queryFinalHistoryByJoinUid(@Param("joinUid") Long joinUid,
			@Param("tenantId") Integer tenantId);
	
	//List<JoinShareMoneyActivityHistoryExcelQuery> queryExportExcel(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery);
}
