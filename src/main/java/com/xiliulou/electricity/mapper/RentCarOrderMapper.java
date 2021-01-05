package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.RentCarOrder;
import java.util.List;

import com.xiliulou.electricity.query.RentCarOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租车记录(TRentCarOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
public interface RentCarOrderMapper extends BaseMapper<RentCarOrder>{

    /**
     * 查询指定行数据
     *
     */
    List<RentCarOrder> queryList(@Param("query") RentCarOrderQuery rentCarOrderQuery);


}