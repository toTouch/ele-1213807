package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.WechatTemplateAdminNotification;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (WechatTemplateAdminNotification)表数据库访问层
 *
 * @author Hardy
 * @since 2021-11-25 16:50:57
 */
public interface WechatTemplateAdminNotificationMapper  extends BaseMapper<WechatTemplateAdminNotification>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    WechatTemplateAdminNotification queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<WechatTemplateAdminNotification> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 对象列表
     */
    List<WechatTemplateAdminNotification> queryAll(WechatTemplateAdminNotification wechatTemplateAdminNotification);

    /**
     * 新增数据
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 影响行数
     */
    int insertOne(WechatTemplateAdminNotification wechatTemplateAdminNotification);

    /**
     * 修改数据
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 影响行数
     */
    int update(WechatTemplateAdminNotification wechatTemplateAdminNotification);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    WechatTemplateAdminNotification queryByTenant(@Param("tenantId") Integer tenantId);
}
