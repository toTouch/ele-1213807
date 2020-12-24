package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ElectricityMemberCardMapper extends BaseMapper<ElectricityMemberCard> {

    List<ElectricityMemberCard> getElectricityMemberCardPage(@Param("offset") Long offset, @Param("size") Long size, @Param("agentId") Integer agentId, @Param("status") Integer status, @Param("type") Integer type);
}
