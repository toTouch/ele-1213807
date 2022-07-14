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

    R queryList(Long offset, Long size, Integer status, Integer type,Integer tenantId);

	R queryUserList(Long offset, Long size,String productKey, String deviceName,Long franchiseeId);

	List<ElectricityMemberCard> queryByFranchisee(Long id);

	List<ElectricityMemberCard> getElectricityUsableBatteryList(Long id);

	List<ElectricityMemberCard> selectByFranchiseeId(Long id);

	R queryCount(Integer status, Integer type, Integer tenantId);

	R listByFranchisee(Long offset, Long size, Integer status, Integer type, Integer tenantId, Long franchiseeId);

	R listCountByFranchisee(Integer status, Integer type, Integer tenantId, Long franchiseeId);

	ElectricityMemberCard queryByStatus(Integer id);

	void unbindActivity(Integer id);

	R queryDisableMemberCardList(Long offset, Long size);
}
