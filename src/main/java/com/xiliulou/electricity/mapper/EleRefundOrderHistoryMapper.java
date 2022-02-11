package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleRefundOrderHistory;
import com.xiliulou.electricity.query.EleRefundHistoryQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 退款订单表(TEleRefundOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
public interface EleRefundOrderHistoryMapper extends BaseMapper<EleRefundOrderHistory>{


    /**
     * 查询指定行数据
     *
     */
    List<Map<String,Object>> queryList(@Param("query") EleRefundHistoryQuery eleRefundHistoryQuery);

	Integer queryCount(@Param("query") EleRefundHistoryQuery eleRefundHistoryQuery);
}
