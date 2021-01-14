package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import org.apache.ibatis.annotations.Param;

public interface ElectricityMemberCardMapper extends BaseMapper<ElectricityMemberCard> {

    IPage getElectricityMemberCardPage(Page page, @Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("type") Integer type);
}
