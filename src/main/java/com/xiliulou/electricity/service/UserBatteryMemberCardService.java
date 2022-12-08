package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * (UserBatteryMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2022-12-06 13:38:52
 */
public interface UserBatteryMemberCardService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryMemberCard selectByUidFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryMemberCard selectByUidFromCache(Long uid);
    
    /**
     * 新增数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    UserBatteryMemberCard insert(UserBatteryMemberCard userBatteryMemberCard);

    UserBatteryMemberCard insertOrUpdate(UserBatteryMemberCard userBatteryMemberCard);

    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserBatteryMemberCard userBatteryMemberCard);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long id);

    /**
     * 用户套餐减次数
     * @param id
     * @return
     */
    Integer minCount(Long id);

    /**
     * 离线换电扣除套餐
     * @param id
     * @return
     */
    Integer minCountForOffLineEle(Long id);

    Integer plusCount(Long id);

    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    Integer updateByUidForDisableCard(UserBatteryMemberCard userBatteryMemberCard);

    List<UserBatteryMemberCard> selectByMemberCardId(Integer id, Integer tenantId);
    
}
