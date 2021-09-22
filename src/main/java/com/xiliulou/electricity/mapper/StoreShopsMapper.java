package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.StoreShops;
import com.xiliulou.electricity.query.StoreShopsQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/9/18 17:31
 * @Description:
 */
public interface StoreShopsMapper extends BaseMapper<StoreShops> {

	List<StoreShops> queryList(@Param("query") StoreShopsQuery storeShopsQuery);

	Integer queryCount(@Param("query") StoreShopsQuery storeShopsQuery);
}
