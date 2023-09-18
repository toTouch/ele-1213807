package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.vo.FinalJoinShareActivityHistoryVo;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareActivityHistoryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    JoinShareActivityHistory queryByIdFromDB(Long id);

    /**
     * 新增数据
     *
     * @param joinShareActivityHistory 实例对象
     * @return 实例对象
     */
    JoinShareActivityHistory insert(JoinShareActivityHistory joinShareActivityHistory);

    /**
     * 修改数据
     *
     * @param joinShareActivityHistory 实例对象
     * @return 实例对象
     */
    Integer update(JoinShareActivityHistory joinShareActivityHistory);

    //    JoinShareActivityHistory queryByRecordIdAndStatus(Long id);
    JoinShareActivityHistory queryByRecordIdAndJoinUid(Long rid, Long joinId);

	R userList(Integer activityId);

	R queryList(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

	void updateByActivityId(JoinShareActivityHistory joinShareActivityHistory);

	void updateExpired(JoinShareActivityHistory joinShareActivityHistory);
    
    FinalJoinShareActivityHistoryVo queryFinalHistoryByJoinUid(Long uid, Integer tenantId);
    
    R queryCount(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);
    
    void queryExportExcel(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery, HttpServletResponse response);

    R queryParticipants(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

    R queryParticipantsCount(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery);

    List<JoinShareActivityHistory> queryUserJoinedActivity(Long joinUid, Integer tenantId);

    Pair<Boolean, String> checkTheActivityFromSameInviter(Long joinUid, Long inviterUid, Long activityId);
}
