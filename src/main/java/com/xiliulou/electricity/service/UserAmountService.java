package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserAmount;

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
     * @param id 主键
     * @return 实例对象
     */
    UserAmount queryByAgentFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserAmount queryByAgentIdFromCache(Long id);



    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserAmount> queryAllByLimit(int offset, int limit);

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

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteByAgentId(Long id);


    List<UserAmount> accountList(Integer size, Integer offset, Long startTime, Long endTime, Long agentId);

    int updateIdempotent(UserAmount userAmount, UserAmount updateUserAmount);

    UserAmount queryByUid(Long uid);

    void updateReduceIncome(Long agentId,Long uid, double income);

    void updateRollBackIncome(Long agentId,Long uid, double income);
}
