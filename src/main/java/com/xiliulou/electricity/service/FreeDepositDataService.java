package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.query.FreeDepositDataQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (FreeDepositData)表服务接口
 *
 * @author zzlong
 * @since 2023-02-20 15:46:34
 */
public interface FreeDepositDataService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositData selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositData selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FreeDepositData> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param freeDepositData 实例对象
     * @return 实例对象
     */
    FreeDepositData insert(FreeDepositData freeDepositData);

    /**
     * 修改数据
     *
     * @param freeDepositData 实例对象
     * @return 实例对象
     */
    Integer update(FreeDepositData freeDepositData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    FreeDepositData selectByTenantId(Integer tenantId);

    Triple<Boolean, String, Object> recharge(FreeDepositDataQuery freeDepositDataQuery);
}
