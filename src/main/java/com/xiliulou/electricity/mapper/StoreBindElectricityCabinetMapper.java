package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.StoreBindElectricityCabinet;
import org.apache.ibatis.annotations.Delete;

/**
 * (FranchiseeBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface StoreBindElectricityCabinetMapper extends BaseMapper<StoreBindElectricityCabinet> {

    @Delete("delete  FROM t_store_bind_electricity_cabinet  WHERE store_id = #{storeId}")
    void deleteByStoreId(Integer storeId);
}