package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserAmount;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserAmountQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * (AgentAmount)表服务接口
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface UserAmountService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserAmount queryByUid(Long uid);



    /**
     * 新增数据
     *
     * @param userAmount 实例对象
     * @return 实例对象
     */
    UserAmount insert(UserAmount userAmount);

    /**
     * 修改数据
     *
     * @param userAmount 实例对象
     * @return 实例对象
     */
    Integer update(UserAmount userAmount);


    R queryByUid();

    void handleAmount(Long uid, Long joinUid,BigDecimal money,Integer tenantId);

    R queryList(UserAmountQuery userAmountQuery);

    R queryCount(UserAmountQuery userAmountQuery);

	void updateReduceIncome(Long uid, Double requestAmount);

    void updateRollBackIncome(Long uid, Double requestAmount);

    void handleInvitationActivityAmount(UserInfo userInfo, Long uid, BigDecimal rewardAmount);
}
