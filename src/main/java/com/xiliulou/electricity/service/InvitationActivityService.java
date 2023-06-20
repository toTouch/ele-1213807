package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.vo.InvitationActivityVO;
import org.apache.commons.lang3.tuple.Triple;

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
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    Integer insert(InvitationActivity invitationActivity);

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
    Integer deleteById(Long id);

    List<InvitationActivityVO> selectByPage(InvitationActivityQuery query);

    Integer selectByPageCount(InvitationActivityQuery query);

    Triple<Boolean, String, Object> save(InvitationActivityQuery query);

    Triple<Boolean, String, Object> modify(InvitationActivityQuery query);

    Triple<Boolean, String, Object> updateStatus(InvitationActivityStatusQuery query);

    List<InvitationActivity> selectUsableActivity(Integer tenantId);

    List<InvitationActivity> selectBySearch(InvitationActivityQuery query);

    Integer checkUsableActivity(Integer tenantId);

    Triple<Boolean, String, Object> activityInfo();
}
