package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.vo.FinalJoinShareMoneyActivityHistoryVo;

import javax.servlet.http.HttpServletResponse;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareMoneyActivityHistoryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    JoinShareMoneyActivityHistory queryByIdFromDB(Long id);

    /**
     * 新增数据
     *
     * @param joinShareMoneyActivityHistory 实例对象
     * @return 实例对象
     */
    JoinShareMoneyActivityHistory insert(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory);

    /**
     * 修改数据
     *
     * @param joinShareMoneyActivityHistory 实例对象
     * @return 实例对象
     */
    Integer update(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory);

	JoinShareMoneyActivityHistory queryByRecordIdAndStatus(Long id);

	R userList(Integer activityId);

	R queryList(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery);

	R queryCount(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery);

	void updateByActivityId(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory);

	void updateExpired(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory);
    
    FinalJoinShareMoneyActivityHistoryVo queryFinalHistoryByJoinUid(Long uid, Integer tenantId);
    
    void queryExportExcel(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery,
            HttpServletResponse response);
}
