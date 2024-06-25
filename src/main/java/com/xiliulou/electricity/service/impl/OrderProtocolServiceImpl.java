package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.OrderProtocol;
import com.xiliulou.electricity.mapper.OrderProtocolMapper;
import com.xiliulou.electricity.query.OrderProtocolQuery;
import com.xiliulou.electricity.service.OrderProtocolService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/10/9 16:22
 * @Description:
 */

@Service
public class OrderProtocolServiceImpl implements OrderProtocolService {
    
    @Resource
    OrderProtocolMapper orderProtocolMapper;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public R queryOrderProtocol() {
        //tenant
        Integer tenantId = TenantContextHolder.getTenantId();
        
        OrderProtocol orderProtocol = orderProtocolMapper.selectLatest(tenantId);
        return R.ok(orderProtocol);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> update(OrderProtocolQuery orderProtocolQuery, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_ORDER_PROTOCOL_UPDATE_LOCK + uid, "1", 2 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        if (Objects.isNull(orderProtocolQuery.getId())) {
            
            OrderProtocol orderProtocol = new OrderProtocol();
            orderProtocol.setContent(orderProtocolQuery.getContent());
            orderProtocol.setCreateTime(System.currentTimeMillis());
            orderProtocol.setUpdateTime(System.currentTimeMillis());
            orderProtocol.setTenantId(TenantContextHolder.getTenantId());
            orderProtocolMapper.insert(orderProtocol);
        } else {
            
            OrderProtocol orderProtocol = new OrderProtocol();
            orderProtocol.setId(orderProtocolQuery.getId());
            orderProtocol.setContent(orderProtocolQuery.getContent());
            orderProtocol.setUpdateTime(System.currentTimeMillis());
            orderProtocol.setTenantId(TenantContextHolder.getTenantId());
            orderProtocolMapper.update(orderProtocol);
        }
        return Triple.of(true, null, null);
    }
}
