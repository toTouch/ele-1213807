package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.RentBatteryOrder;
import java.util.List;

import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租电池记录(TRentBatteryOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
public interface RentBatteryOrderMapper extends BaseMapper<RentBatteryOrder>{


    /**
     * 查询指定行数据
     *
     */
    List<RentBatteryOrder> queryList(@Param("query") RentBatteryOrderQuery rentBatteryOrderQuery);


}