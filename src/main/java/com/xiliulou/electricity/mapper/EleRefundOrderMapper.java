package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleRefundOrder;
import java.util.List;

import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
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
    List<EleRefundOrderVO> queryList(@Param("query") EleRefundQuery eleRefundQuery);


}
