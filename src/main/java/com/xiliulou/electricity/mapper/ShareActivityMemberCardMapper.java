package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareActivityMemberCard;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ShareActivityMemberCard)表数据库访问层
 *
 * @author zzlong
 * @since 2023-05-24 10:19:26
 */
public interface ShareActivityMemberCardMapper extends BaseMapper<ShareActivityMemberCard> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityMemberCard queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ShareActivityMemberCard> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param shareActivityMemberCard 实例对象
     * @return 对象列表
     */
    List<ShareActivityMemberCard> queryAll(ShareActivityMemberCard shareActivityMemberCard);

    /**
     * 新增数据
     *
     * @param shareActivityMemberCard 实例对象
     * @return 影响行数
     */
    int insertOne(ShareActivityMemberCard shareActivityMemberCard);

    /**
     * 修改数据
     *
     * @param shareActivityMemberCard 实例对象
     * @return 影响行数
     */
    int update(ShareActivityMemberCard shareActivityMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer batchInsert(List<ShareActivityMemberCard> shareActivityMemberCards);

    List<ShareActivityMemberCard> selectByActivityId(@Param("activityId") Integer activityId);

    List<ShareActivityMemberCard> selectByActivityIdAndPackageType(@Param("activityId") Integer activityId, @Param("packageType") Integer packageType);

    Integer deleteByActivityId(@Param("activityId") Integer id);

    List<Long> selectMemberCardIdsByActivityId(@Param("activityId") Integer id);
}
