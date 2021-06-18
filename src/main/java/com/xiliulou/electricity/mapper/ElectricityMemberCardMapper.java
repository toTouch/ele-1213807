package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ElectricityMemberCardMapper extends BaseMapper<ElectricityMemberCard> {

    List<ElectricityMemberCard> electricityMemberCardList(@Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("type") Integer type);

	List<ElectricityMemberCard>  queryElectricityMemberCard(@Param("offset") Long offset, @Param("size") Long size, @Param("id") Integer id);
}
