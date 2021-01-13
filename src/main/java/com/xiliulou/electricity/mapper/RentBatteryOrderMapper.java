package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 租电池记录(TRentBatteryOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
public interface RentBatteryOrderMapper extends BaseMapper<RentBatteryOrder> {


    /**
     * 查询指定行数据
     */
    IPage queryList(Page page, @Param("query") RentBatteryOrderQuery rentBatteryOrderQuery);


}