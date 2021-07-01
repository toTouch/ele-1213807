package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.EleDepositOrder;
import java.util.List;

import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 缴纳押金订单表(TEleDepositOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleDepositOrderMapper extends BaseMapper<EleDepositOrder>{

    /**
     * 查询指定行数据
     *
     */
    List<EleDepositOrder> queryList(@Param("query") EleDepositOrderQuery eleDepositOrderQuery);

	Integer queryCount(@Param("query")  EleDepositOrderQuery eleDepositOrderQuery);
}
