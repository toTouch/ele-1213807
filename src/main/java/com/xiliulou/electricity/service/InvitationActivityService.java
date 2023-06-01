package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.query.InvitationActivityQuery;

import java.util.List;

/**
 * (InvitationActivity)表服务接口
 *
 * @author zzlong
 * @since 2023-06-01 15:55:48
 */
public interface InvitationActivityService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivity queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivity queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivity> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    InvitationActivity insert(InvitationActivity invitationActivity);

    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivity invitationActivity);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<InvitationActivity> selectByPage(InvitationActivityQuery query);

    Integer selectByPageCount(InvitationActivityQuery query);
}
