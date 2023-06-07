package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.vo.InvitationActivityJoinHistoryVO;

import java.util.List;

/**
 * (InvitationActivityJoinHistory)表服务接口
 *
 * @author zzlong
 * @since 2023-06-06 09:51:43
 */
public interface InvitationActivityJoinHistoryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityJoinHistory queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityJoinHistory queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityJoinHistory> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    InvitationActivityJoinHistory insert(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 修改数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    InvitationActivityJoinHistory selectByActivityAndInvitationUid(Long id, Long invitationUid, Long uid);

    List<InvitationActivityJoinHistoryVO> selectByPage(InvitationActivityJoinHistoryQuery query);

    Integer selectByPageCount(InvitationActivityJoinHistoryQuery query);

    Integer updateStatusByActivityId(Long activityId, Integer status);

    InvitationActivityJoinHistory selectByJoinIdAndStatus(Long uid, Integer statusInit);

    List<InvitationActivityJoinHistoryVO> selectUserByPage(InvitationActivityJoinHistoryQuery query);
}
