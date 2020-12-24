package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;

public interface ElectricityMemberCardService {
    R saveElectricityMemberCard(ElectricityMemberCard electricityMemberCard);

    R updateElectricityMemberCard(ElectricityMemberCard electricityMemberCard);

    R deleteElectricityMemberCard(Integer id);

    void deleteElectricityMemberCardCache(Integer id);

    ElectricityMemberCard getElectricityMemberCard(Integer id);

    R getElectricityMemberCardPage(Long offset, Long size, Integer agentId, Integer status,Integer type);
}
