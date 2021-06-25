package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Role;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
    List<Role> queryAll();


    List<Role> queryListByCondition(@Param("offset") Integer offset, @Param("size")Integer size);

    List<Role> queryByRoleIds(List<Long> roleIds);
}
