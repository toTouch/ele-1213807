package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.vo.InvitationActivityJoinHistoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (InvitationActivityJoinHistory)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-06 09:51:43
 */
public interface InvitationActivityJoinHistoryMapper extends BaseMapper<InvitationActivityJoinHistory> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityJoinHistory queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityJoinHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 对象列表
     */
    List<InvitationActivityJoinHistory> queryAll(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 新增数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 影响行数
     */
    int insertOne(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 修改数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 影响行数
     */
    int update(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<InvitationActivityJoinHistoryVO> selectByPage(InvitationActivityJoinHistoryQuery query);

    Integer selectByPageCount(InvitationActivityJoinHistoryQuery query);

    Integer updateStatusByActivityId(@Param("activityId") Long activityId, @Param("status") Integer status);

    InvitationActivityJoinHistory selectByJoinIdAndStatus(@Param("uid") Long uid, @Param("status") Integer status);

    InvitationActivityJoinHistory selectByJoinUid(@Param("uid") Long uid);

    InvitationActivityJoinHistory selectByActivityAndUid(@Param("activityId") Long activityId, @Param("uid") Long uid);

    Integer updateExpired(InvitationActivityJoinHistory invitationActivityJoinHistoryUpdate);
}
