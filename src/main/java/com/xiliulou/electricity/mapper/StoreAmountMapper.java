package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.StoreAmount;
import com.xiliulou.electricity.query.StoreAccountQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (StoreAmount)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:25
 */
public interface StoreAmountMapper extends BaseMapper<StoreAmount> {


    int updateIdEmpontent(@Param("old") StoreAmount storeAmount, @Param("fresh") StoreAmount updateStoreAmount);

    List<StoreAmount> queryList(StoreAccountQuery storeAccountQuery);

    Integer queryCount(StoreAccountQuery storeAccountQuery);
}
