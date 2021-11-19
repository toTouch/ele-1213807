package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserAmount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (AgentAmount)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface UserAmountMapper extends BaseMapper<UserAmount> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserAmount queryByAgentId(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserAmount> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param userAmount 实例对象
     * @return 对象列表
     */
    List<UserAmount> queryAll(UserAmount userAmount);

    /**
     * 新增数据
     *
     * @param userAmount 实例对象
     * @return 影响行数
     */
    int insertOne(UserAmount userAmount);

    /**
     * 修改数据
     *
     * @param userAmount 实例对象
     * @return 影响行数
     */
    int update(UserAmount userAmount);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteByAgentId(Long id);

    List<UserAmount> accountList(@Param("size") Integer size,
                                  @Param("offset") Integer offset,
                                  @Param("startTime") Long startTime,
                                  @Param("endTime") Long endTime,
                                  @Param("agentId") Long agentId,
                                  @Param("tenantId") Integer tenantId);

    int updateIdempontent(@Param("old") UserAmount userAmount, @Param("fresh") UserAmount updateUserAmount);

	void updateReduceIncome(@Param("uid") Long uid, @Param("income") double income);

    void updateRollBackIncome(@Param("uid") Long uid,@Param("income") double income);
}
