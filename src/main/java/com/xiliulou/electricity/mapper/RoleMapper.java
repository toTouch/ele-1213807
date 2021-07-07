package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Role;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * (Role)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-09 14:34:00
 */
public interface RoleMapper  extends BaseMapper<Role>{




    /**
     * 通过实体作为筛选条件查询
     *
     * @return 对象列表
     */
    List<Role> queryAll(Integer tenantId);


    List<Role> queryByRoleIds(List<Long> roleIds);

    @Select("SELECT id,name,code,create_time,update_time,tenant_id FROM t_role WHERE id=#{id}")
	Role queryById(Long id);


	Integer insertOne(Role role);

	Integer update(Role role);

	@Select("SELECT id,name,code,create_time,update_time,tenant_id FROM t_role WHERE name=#{name} and tenant_id=#{tenantId}")
	Role queryByName(@Param("name") String name,@Param("tenantId") Integer tenantId);
}
