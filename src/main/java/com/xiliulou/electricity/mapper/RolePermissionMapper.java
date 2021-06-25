package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.RolePermission;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (RolePermission)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-09 14:36:22
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

	@Delete("delete from electricity.t_role_permission where role_id = #{rid}")
	int deleteByRoleId(@Param("rid")Long roleId);
}
