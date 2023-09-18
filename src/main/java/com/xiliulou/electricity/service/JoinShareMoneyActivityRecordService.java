package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityRecord;

import java.util.List;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareMoneyActivityRecordService {

    /**
     * 修改数据
     *
     * @param joinShareMoneyActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(JoinShareMoneyActivityRecord joinShareMoneyActivityRecord);


    R joinActivity(Integer activityId, Long uid);

	JoinShareMoneyActivityRecord queryByJoinUid(Long uid);

	void handelJoinShareMoneyActivityExpired();

	void updateByActivityId(JoinShareMoneyActivityRecord joinShareMoneyActivityRecord);

    List<JoinShareMoneyActivityRecord> queryByUidAndActivityId(Long uid, Long activityId);
}
