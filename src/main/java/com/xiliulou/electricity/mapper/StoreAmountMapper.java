package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.StoreAmount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (StoreAmount)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:25
 */
public interface StoreAmountMapper extends BaseMapper<StoreAmount> {

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteByStoreId(Long id);

    int updateIdEmpontent(@Param("old") StoreAmount storeAmount, @Param("fresh") StoreAmount updateStoreAmount);

    List<StoreAmount> accountList(@Param("size") Integer size,
                                 @Param("offset") Integer offset,
                                 @Param("startTime") Long startTime,
                                 @Param("endTime") Long endTime,
                                 @Param("storeIds") List<Long> storeIds,
                                 @Param("tenantId") Integer tenantId);


}
