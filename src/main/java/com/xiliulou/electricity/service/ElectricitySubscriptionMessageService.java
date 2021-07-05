package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;

public interface ElectricitySubscriptionMessageService {
    R saveElectricitySubscriptionMessage(ElectricitySubscriptionMessage electricitySubscriptionMessage);

    ElectricitySubscriptionMessage getSubscriptionMessageByType(Integer type,Integer tenantId);


    R updateElectricitySubscriptionMessage(ElectricitySubscriptionMessage electricitySubscriptionMessage);

    R getElectricitySubscriptionMessagePage(Integer type,Integer tenantId);
}
