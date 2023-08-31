package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.CarRentalAndRefundProtocol;
import com.xiliulou.electricity.mapper.CarRentalAndRefundProtocolMapper;
import com.xiliulou.electricity.query.CarProtocolQuery;
import com.xiliulou.electricity.service.CarProtocolService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.CarProtocolVO;
import org.apache.commons.collections4.CollectionUtils;
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

    @Slave
    @Override
    public CarProtocolVO findProtocolByQuery() {
        Long tenantId = TenantContextHolder.getTenantId().longValue();
        CarRentalAndRefundProtocol query = new CarRentalAndRefundProtocol();
        query.setTenantId(tenantId);
        List<CarRentalAndRefundProtocol> result = carRentalAndRefundProtocolMapper.selectProtocolByQuery(query);
        CarProtocolVO carProtocolVO = new CarProtocolVO();
        if(CollectionUtils.isNotEmpty(result) && result.size() > 0){
            CarRentalAndRefundProtocol carRentalAndRefundProtocol = result.get(0);
            BeanUtils.copyProperties(carRentalAndRefundProtocol, carProtocolVO);
        }

        return carProtocolVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarProtocolQuery carProtocolQuery) {
        CarRentalAndRefundProtocol carRentalAndRefundProtocol = new CarRentalAndRefundProtocol();
        Integer result;
        if(Objects.isNull(carProtocolQuery.getId())){
            carRentalAndRefundProtocol.setContent(carProtocolQuery.getContent());
            carRentalAndRefundProtocol.setTenantId(TenantContextHolder.getTenantId().longValue());
            carRentalAndRefundProtocol.setCreateTime(System.currentTimeMillis());
            carRentalAndRefundProtocol.setUpdateTime(System.currentTimeMillis());

            result = carRentalAndRefundProtocolMapper.insertOne(carRentalAndRefundProtocol);
        }else{
            carRentalAndRefundProtocol.setId(carProtocolQuery.getId());
            carRentalAndRefundProtocol.setContent(carProtocolQuery.getContent());
            carRentalAndRefundProtocol.setTenantId(TenantContextHolder.getTenantId().longValue());
            carRentalAndRefundProtocol.setUpdateTime(System.currentTimeMillis());
            result = carRentalAndRefundProtocolMapper.update(carRentalAndRefundProtocol);
        }

        return result;
    }
}
