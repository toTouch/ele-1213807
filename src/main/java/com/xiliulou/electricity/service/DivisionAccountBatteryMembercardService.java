package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.DivisionAccountBatteryMembercard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (DivisionAccountBatteryMembercard)表服务接口
 *
 * @author zzlong
 * @since 2023-04-23 17:59:54
 */
public interface DivisionAccountBatteryMembercardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountBatteryMembercard queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountBatteryMembercard queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<DivisionAccountBatteryMembercard> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 实例对象
     */
    DivisionAccountBatteryMembercard insert(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard);

    /**
     * 修改数据
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 实例对象
     */
    Integer update(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer batchInsert(List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardLIst);

    Integer batchInsertMemberCards(List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList);

    List<Long> selectByDivisionAccountConfigId(Long id);

    List<DivisionAccountBatteryMembercard> selectMemberCardsByDAConfigId(Long divisionAccountId);

    Long selectByBatteryMembercardId(Long membercardId);

    List<Long> selectByTenantId(Integer tenantId);

    Integer deleteByDivisionAccountId(Long id);
}
