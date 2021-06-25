package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.PermissionResource;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * (PermissionResource)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-09 15:38:23
 */
public interface PermissionResourceMapper  extends BaseMapper<PermissionResource>{


    List<PermissionResource> queryListByIds(List<Long> pids);

    List<PermissionResource> queryAll();
}
