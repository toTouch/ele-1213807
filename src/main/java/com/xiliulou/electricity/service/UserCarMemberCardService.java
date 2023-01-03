package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.vo.FailureMemberCardVo;

import java.util.List;

/**
 * (UserCarMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2022-12-07 17:36:11
 */
public interface UserCarMemberCardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCarMemberCard selectByUidFromDB(Long uid);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCarMemberCard selectByUidFromCache(Long uid);

    /**
     * 新增数据
     *
     * @param userCarMemberCard 实例对象
     * @return 实例对象
     */
    UserCarMemberCard insert(UserCarMemberCard userCarMemberCard);

    UserCarMemberCard insertOrUpdate(UserCarMemberCard userCarMemberCard);

    /**
     * 修改数据
     *
     * @param userCarMemberCard 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserCarMemberCard userCarMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);

    void carMemberCardExpireReminder();

    List<FailureMemberCardVo> queryMemberCardExpireUser(int offset, int size, long nowTime);
}
