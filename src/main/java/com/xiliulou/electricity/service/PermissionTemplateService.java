package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.PermissionTemplate;
import com.xiliulou.electricity.query.PermissionTemplateQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (PermissionTemplate)表服务接口
 *
 * @author zzlong
 * @since 2022-09-19 16:34:06
 */
public interface PermissionTemplateService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    PermissionTemplate selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    PermissionTemplate selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<PermissionTemplate> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param permissionTemplate 实例对象
     * @return 实例对象
     */
    PermissionTemplate insert(PermissionTemplate permissionTemplate);

    /**
     * 修改数据
     *
     * @param permissionTemplate 实例对象
     * @return 实例对象
     */
    Integer update(PermissionTemplate permissionTemplate);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<Long> selectByType(Integer typeOperate);

    int insertPermissionTemplate(PermissionTemplateQuery permissionTemplateQuery);
}
