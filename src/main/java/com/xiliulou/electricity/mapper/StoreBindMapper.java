package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.StoreBind;
import org.apache.ibatis.annotations.Delete;

/**
 * (StoreBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface StoreBindMapper extends BaseMapper<StoreBind> {


    @Delete("delete  FROM t_store_bind  WHERE store_id = #{storeId}")
    void deleteByStoreId(Integer storeId);
}