package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleRefundOrder;
import java.util.List;
import java.util.Map;

import com.xiliulou.electricity.query.EleRefundQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 退款订单表(TEleRefundOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
public interface EleRefundOrderMapper extends BaseMapper<EleRefundOrder>{


    /**
     * 查询指定行数据
     *
     */
    List<Map<String,Object>> queryList(@Param("query") EleRefundQuery eleRefundQuery);

	Integer queryCount(@Param("query") EleRefundQuery eleRefundQuery);
}
