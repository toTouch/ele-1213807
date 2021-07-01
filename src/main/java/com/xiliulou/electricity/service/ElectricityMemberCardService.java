package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ElectricityMemberCardService {
    R add(ElectricityMemberCard electricityMemberCard);

    R update(ElectricityMemberCard electricityMemberCard);

    R delete(Integer id);

    ElectricityMemberCard queryByCache(Integer id);

    R queryList(Long offset, Long size, Integer status, Integer type);

	R queryUserList(Long offset, Long size,String productKey, String deviceName);

	List<ElectricityMemberCard> queryByFranchisee(Integer id);

}
