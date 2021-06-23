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

	/**
	 * 通过ID查询单条数据
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	RolePermission queryById(Long id);

	/**
	 * 查询指定行数据
	 *
	 * @param offset 查询起始位置
	 * @param limit  查询条数
	 * @return 对象列表
	 */
	List<RolePermission> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


	/**
	 * 通过主键删除数据
	 *
	 * @param id 主键
	 * @return 影响行数
	 */
	int deleteById(Long id);


	@Delete("delete from electricity.t_role_permission where role_id = #{rid}")
	int deleteByRoleId(@Param("rid")Long roleId);
}
