package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.OrderProtocol;

/**
 * (Faq)表数据库访问层
 *
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface OrderProtocolMapper extends BaseMapper<OrderProtocol> {
    
    Integer update(OrderProtocol orderProtocol);
}
