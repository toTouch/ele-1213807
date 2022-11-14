package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.PermissionResource;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * (PermissionResource)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-09 15:38:23
 */
public interface PermissionResourceMapper  extends BaseMapper<PermissionResource>{


    List<PermissionResource> queryListByIds(List<Long> pids);

    List<PermissionResource> queryAll();

    @Select("SELECT id,name,type,uri,method,sort,parent,permission,create_time,update_time,del_flag FROM t_permission_resource WHERE id=#{id}")
    PermissionResource queryById(Long id);

    Integer insertOne(PermissionResource permissionResource);

    Integer update(PermissionResource permissionResource);
}
