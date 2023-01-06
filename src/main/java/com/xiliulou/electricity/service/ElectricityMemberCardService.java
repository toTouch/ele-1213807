package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;

import java.util.List;

public interface ElectricityMemberCardService {
    R add(ElectricityMemberCard electricityMemberCard);

    R update(ElectricityMemberCard electricityMemberCard);

    R delete(Integer id);

    ElectricityMemberCard queryByCache(Integer id);

    R queryList(Long offset, Long size, Integer status, Integer type, Integer tenantId, Integer cardModel, List<Long> franchiseeId);

    R queryUserList(Long offset, Long size, String productKey, String deviceName, Long franchiseeId);

    R queryFirstPayMemberCard(Long offset, Long size, String productKey, String deviceName, Long franchiseeId,Integer model);

	R queryRentCarMemberCardList(Long offset, Long size);

	List<ElectricityMemberCard> queryByFranchisee(Long id);

    List<ElectricityMemberCard> getElectricityUsableBatteryList(Long id,Integer tenantId);

    List<ElectricityMemberCard> selectByFranchiseeId(Long id,Integer tenantId);

    R queryCount(Integer status, Integer type, Integer tenantId, Integer cardModel, List<Long> franchiseeId);

    R listByFranchisee(Long offset, Long size, Integer status, Integer type, Integer tenantId, List<Long> franchiseeId);

    R listCountByFranchisee(Integer status, Integer type, Integer tenantId, List<Long> franchiseeId);

    ElectricityMemberCard selectUserMemberCardById(Integer id);

    void unbindActivity(Integer id);

    R queryDisableMemberCardList(Long offset, Long size);
}
