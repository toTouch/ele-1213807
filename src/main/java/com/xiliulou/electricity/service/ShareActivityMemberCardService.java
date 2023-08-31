package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityMemberCard;

import java.util.List;

/**
 * (ShareActivityMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2023-05-24 10:19:26
 */
public interface ShareActivityMemberCardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityMemberCard queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityMemberCard queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ShareActivityMemberCard> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param shareActivityMemberCard 实例对象
     * @return 实例对象
     */
    ShareActivityMemberCard insert(ShareActivityMemberCard shareActivityMemberCard);

    /**
     * 修改数据
     *
     * @param shareActivityMemberCard 实例对象
     * @return 实例对象
     */
    Integer update(ShareActivityMemberCard shareActivityMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer batchInsert(List<ShareActivityMemberCard> shareActivityMemberCards);

    List<ShareActivityMemberCard> selectByActivityId(Integer id);

    List<ShareActivityMemberCard> selectByActivityIdAndPackageType(Integer id, Integer packageType);

    List<Long> selectMemberCardIdsByActivityId(Integer id);

    Integer deleteByActivityId(Integer id);
}
