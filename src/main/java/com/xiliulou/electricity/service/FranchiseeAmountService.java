package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeAmount;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;

import java.util.List;

/**
 * (FranchiseeAmount)表服务接口
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface FranchiseeAmountService {


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeAmount queryByAgentIdFromCache(Long id);



    /**
     * 修改数据
     *
     * @param franchiseeAmount 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeAmount franchiseeAmount);


	void handleSplitAccount(Franchisee franchisee, ElectricityTradeOrder payAmount, int agentPercent);

    R queryList(FranchiseeAccountQuery franchiseeAccountQuery);

    R queryCount(FranchiseeAccountQuery franchiseeAccountQuery);

    void insert(FranchiseeAmount franchiseeAmount);
}
