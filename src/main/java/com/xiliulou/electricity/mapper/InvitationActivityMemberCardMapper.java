package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.InvitationActivityMemberCard;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (InvitationActivityMemberCard)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-05 15:31:55
 */
public interface InvitationActivityMemberCardMapper extends BaseMapper<InvitationActivityMemberCard> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityMemberCard queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityMemberCard> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 对象列表
     */
    List<InvitationActivityMemberCard> queryAll(InvitationActivityMemberCard invitationActivityMemberCard);

    /**
     * 新增数据
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 影响行数
     */
    int insertOne(InvitationActivityMemberCard invitationActivityMemberCard);

    /**
     * 修改数据
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 影响行数
     */
    int update(InvitationActivityMemberCard invitationActivityMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer batchInsert(List<InvitationActivityMemberCard> shareActivityMemberCards);

    List<Long> selectMemberCardIdsByActivityId(@Param("activityId") Long activityId);

    Integer deleteByActivityId(@Param("activityId") Long activityId);
}
