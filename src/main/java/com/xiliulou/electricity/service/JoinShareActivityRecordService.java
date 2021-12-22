package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;

import java.util.List;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareActivityRecordService {

    /**
     * 修改数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(JoinShareActivityRecord joinShareActivityRecord);


    R joinActivity(Integer activityId, Long uid);

    JoinShareActivityRecord queryByJoinUid(Long uid);

	void handelJoinShareActivityExpired();

	void updateByActivityId(JoinShareActivityRecord joinShareActivityRecord);
}
