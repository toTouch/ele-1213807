package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.WechatTemplateAdminNotification;
import com.xiliulou.electricity.query.WechatTemplateAdminNotificationQuery;
import com.xiliulou.electricity.vo.WechatTemplateAdminNotificationVo;

import java.util.List;

/**
 * (WechatTemplateAdminNotification)表服务接口
 *
 * @author Hardy
 * @since 2021-11-25 16:50:58
 */
public interface WechatTemplateAdminNotificationService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    WechatTemplateAdminNotification queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    WechatTemplateAdminNotification queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<WechatTemplateAdminNotification> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 实例对象
     */
    WechatTemplateAdminNotification insert(WechatTemplateAdminNotification wechatTemplateAdminNotification);

    /**
     * 修改数据
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 实例对象
     */
    Integer update(WechatTemplateAdminNotification wechatTemplateAdminNotification);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    R saveOne(WechatTemplateAdminNotificationQuery wechatTemplateAdminNotificationQuery);

    R updateOne(WechatTemplateAdminNotificationQuery wechatTemplateAdminNotificationQuery);

    WechatTemplateAdminNotification queryByTenant(Integer tenantId);
}
