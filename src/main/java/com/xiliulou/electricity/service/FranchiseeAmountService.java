package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeAmount;
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
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FranchiseeAmount> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param franchiseeAmount 实例对象
     * @return 实例对象
     */
    FranchiseeAmount insert(FranchiseeAmount franchiseeAmount);

    /**
     * 修改数据
     *
     * @param franchiseeAmount 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeAmount franchiseeAmount);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteByAgentId(Long id);

	void handleSplitAccount(Franchisee franchisee, ElectricityTradeOrder payAmount, int agentPercent);


}
