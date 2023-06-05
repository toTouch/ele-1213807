package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.InvitationActivity;

import java.util.List;

import com.xiliulou.electricity.query.InvitationActivityQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (InvitationActivity)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-01 15:55:47
 */
public interface InvitationActivityMapper extends BaseMapper<InvitationActivity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivity queryById(Long id);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param invitationActivity 实例对象
     * @return 对象列表
     */
    List<InvitationActivity> queryAll(InvitationActivity invitationActivity);

    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 影响行数
     */
    int insertOne(InvitationActivity invitationActivity);

    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 影响行数
     */
    int update(InvitationActivity invitationActivity);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<InvitationActivity> selectByPage(InvitationActivityQuery query);

    Integer selectByPageCount(InvitationActivityQuery query);

    Integer findUsableActivity(@Param("tenantId") Integer tenantId);

    InvitationActivity selectUsableActivity(Integer tenantId);
}
