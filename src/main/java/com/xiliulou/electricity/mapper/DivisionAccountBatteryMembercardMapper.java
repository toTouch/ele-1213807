package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.DivisionAccountBatteryMembercard;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (DivisionAccountBatteryMembercard)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-23 17:59:54
 */
public interface DivisionAccountBatteryMembercardMapper extends BaseMapper<DivisionAccountBatteryMembercard> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountBatteryMembercard queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<DivisionAccountBatteryMembercard> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 对象列表
     */
    List<DivisionAccountBatteryMembercard> queryAll(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard);

    /**
     * 新增数据
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 影响行数
     */
    int insertOne(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard);

    /**
     * 修改数据
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 影响行数
     */
    int update(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer batchInsert(List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList);

    List<Long> selectByDivisionAccountConfigId(@Param("id") Long id);

    Long selectByBatteryMembercardId(@Param("membercardId") Long membercardId);

    List<Long> selectByTenantId(@Param("tenantId")Integer tenantId);

    Integer deleteByDivisionAccountId(@Param("divisionAccountId") Long divisionAccountId);
}
