package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ElectricityMemberCardMapper extends BaseMapper<ElectricityMemberCard> {

    List<ElectricityMemberCard> queryList(@Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("type") Integer type,@Param("tenantId") Integer tenantId);

	List<ElectricityMemberCard> queryUserList(@Param("offset") Long offset, @Param("size") Long size, @Param("id") Long id, @Param("batteryType") String batteryType);

	Integer queryCount(@Param("status") Integer status, @Param("type") Integer type,@Param("tenantId") Integer tenantId);

	List<ElectricityMemberCard> listByFranchisee(@Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("type") Integer type,@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);

	Integer listCountByFranchisee(@Param("status") Integer status, @Param("type") Integer type,@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);

	void unbindActivity(@Param("id") Integer id);

}
