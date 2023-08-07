package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.CarRentalAndRefundProtocol;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 11:31
 * @Description:
 */
public interface CarRentalAndRefundProtocolMapper extends BaseMapper<CarRentalAndRefundProtocol> {
    CarRentalAndRefundProtocol selectProtocolByQuery(CarRentalAndRefundProtocol carRentalAndRefundProtocol);

    Integer insertOne(CarRentalAndRefundProtocol carRentalAndRefundProtocol);

    Integer update(CarRentalAndRefundProtocol carRentalAndRefundProtocol);

}
