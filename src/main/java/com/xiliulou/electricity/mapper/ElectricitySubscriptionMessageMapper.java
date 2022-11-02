package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;

public interface ElectricitySubscriptionMessageMapper extends BaseMapper<ElectricitySubscriptionMessage> {

    int update(ElectricitySubscriptionMessage electricitySubscriptionMessage);
}
