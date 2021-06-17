package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;

import java.util.List;

public interface ElectricityMemberCardService {
    R saveElectricityMemberCard(ElectricityMemberCard electricityMemberCard);

    R updateElectricityMemberCard(ElectricityMemberCard electricityMemberCard);

    R deleteElectricityMemberCard(Integer id);

    void deleteElectricityMemberCardCache(Integer id);

    ElectricityMemberCard getElectricityMemberCard(Integer id);

    R getElectricityMemberCardPage(Long offset, Long size, Integer status, Integer type);

	R queryElectricityMemberCard(Long offset, Long size);

	List<ElectricityMemberCard> queryByFranchisee(Integer id);
}
