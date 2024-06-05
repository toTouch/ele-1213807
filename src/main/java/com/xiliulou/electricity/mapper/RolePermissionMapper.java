package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.GrantRolePermission;
import com.xiliulou.electricity.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * (RolePermission)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-09 14:36:22
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    @Delete("delete from t_role_permission where role_id = #{rid}")
    int deleteByRoleId(@Param("rid") Long roleId);
    
    List<GrantRolePermission> selectRepeatGrant(@Param("roleIds") List<Long> roleIds);
    
    int batchInsert(@Param("list") Collection<GrantRolePermission> rolePermissions);
}
