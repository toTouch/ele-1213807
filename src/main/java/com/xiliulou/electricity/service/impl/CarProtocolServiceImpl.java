package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.CarRentalAndRefundProtocol;
import com.xiliulou.electricity.mapper.CarRentalAndRefundProtocolMapper;
import com.xiliulou.electricity.query.CarProtocolQuery;
import com.xiliulou.electricity.service.CarProtocolService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.CarProtocolVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 13:37
 * @Description:
 */

@Service
public class CarProtocolServiceImpl implements CarProtocolService {
    
    @Resource
    CarRentalAndRefundProtocolMapper carRentalAndRefundProtocolMapper;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public CarProtocolVO findProtocolByQuery() {
        Long tenantId = TenantContextHolder.getTenantId().longValue();
        CarRentalAndRefundProtocol query = new CarRentalAndRefundProtocol();
        query.setTenantId(tenantId);
        List<CarRentalAndRefundProtocol> result = carRentalAndRefundProtocolMapper.selectProtocolByQuery(query);
        CarProtocolVO carProtocolVO = new CarProtocolVO();
        if (CollectionUtils.isNotEmpty(result) && result.size() > 0) {
            CarRentalAndRefundProtocol carRentalAndRefundProtocol = result.get(0);
            BeanUtils.copyProperties(carRentalAndRefundProtocol, carProtocolVO);
        }
        
        return carProtocolVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> update(CarProtocolQuery carProtocolQuery, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_CAR_RENTAL_REFUND_PROTOCOL_UPDATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        CarRentalAndRefundProtocol carRentalAndRefundProtocol = new CarRentalAndRefundProtocol();
        if (Objects.isNull(carProtocolQuery.getId())) {
            carRentalAndRefundProtocol.setContent(carProtocolQuery.getContent());
            carRentalAndRefundProtocol.setTenantId(TenantContextHolder.getTenantId().longValue());
            carRentalAndRefundProtocol.setCreateTime(System.currentTimeMillis());
            carRentalAndRefundProtocol.setUpdateTime(System.currentTimeMillis());
            
            carRentalAndRefundProtocolMapper.insertOne(carRentalAndRefundProtocol);
        } else {
            carRentalAndRefundProtocol.setId(carProtocolQuery.getId());
            carRentalAndRefundProtocol.setContent(carProtocolQuery.getContent());
            carRentalAndRefundProtocol.setTenantId(TenantContextHolder.getTenantId().longValue());
            carRentalAndRefundProtocol.setUpdateTime(System.currentTimeMillis());
            carRentalAndRefundProtocolMapper.update(carRentalAndRefundProtocol);
        }
        
        return Triple.of(true, null, null);
    }
}
