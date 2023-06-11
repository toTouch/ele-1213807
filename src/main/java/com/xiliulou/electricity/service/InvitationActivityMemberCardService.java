package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InvitationActivityMemberCard;

import java.util.List;

/**
 * (InvitationActivityMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2023-06-05 15:31:55
 */
public interface InvitationActivityMemberCardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityMemberCard queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityMemberCard queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityMemberCard> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 实例对象
     */
    InvitationActivityMemberCard insert(InvitationActivityMemberCard invitationActivityMemberCard);

    /**
     * 修改数据
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivityMemberCard invitationActivityMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer batchInsert(List<InvitationActivityMemberCard> shareActivityMemberCards);

    List<Long> selectMemberCardIdsByActivityId(Long id);

    Integer deleteByActivityId(Long id);
}
