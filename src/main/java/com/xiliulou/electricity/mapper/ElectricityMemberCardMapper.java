package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.vo.ElectricityMemberCardVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ElectricityMemberCardMapper extends BaseMapper<ElectricityMemberCard> {

    List<ElectricityMemberCardVO> queryList(@Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("type") Integer type, @Param("tenantId") Integer tenantId, @Param("cardModel") Integer cardModel,@Param("franchiseeIds") List<Long> franchiseeIds,@Param("carModelId") Integer carModelId);

    List<ElectricityMemberCard> queryUserList(@Param("offset") Long offset, @Param("size") Long size, @Param("id") Long id, @Param("batteryType") String batteryType, @Param("cardModel") Integer cardModel);

	Integer queryCount(@Param("status") Integer status, @Param("type") Integer type,@Param("tenantId") Integer tenantId,@Param("cardModel") Integer cardModel,@Param("franchiseeIds") List<Long> franchiseeIds,@Param("name") String name);

    List<ElectricityMemberCard> listByFranchisee(@Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("type") Integer type, @Param("tenantId") Integer tenantId, @Param("franchiseeIds") List<Long> franchiseeIds);

    Integer listCountByFranchisee(@Param("status") Integer status, @Param("type") Integer type, @Param("tenantId") Integer tenantId, @Param("franchiseeIds") List<Long> franchiseeIds);

    void unbindActivity(@Param("id") Integer id);

    int update(ElectricityMemberCard electricityMemberCard);

    Integer isMemberCardBindFranchinsee(@Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);

    Integer batchInsert(List<ElectricityMemberCard> newElectricityMemberCards);
}
