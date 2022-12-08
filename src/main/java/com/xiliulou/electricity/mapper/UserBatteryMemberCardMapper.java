package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserBatteryMemberCard;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * (UserBatteryMemberCard)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-06 13:38:52
 */
public interface UserBatteryMemberCardMapper extends BaseMapper<UserBatteryMemberCard> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryMemberCard selectByUid(@Param("uid") Long uid);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param userBatteryMemberCard 实例对象
     * @return 对象列表
     */
    List<UserBatteryMemberCard> selectByQuery(UserBatteryMemberCard userBatteryMemberCard);
    
    /**
     * 新增数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 影响行数
     */
    int insertOne(UserBatteryMemberCard userBatteryMemberCard);
    
    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 影响行数
     */
    int updateByUid(UserBatteryMemberCard userBatteryMemberCard);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(@Param("uid") Long uid);

    Integer minCount(Long id);

    Integer minCountForOffLineEle(Long id);

    Integer plusCount(Long id);

    int updateByUidForDisableCard(UserBatteryMemberCard userBatteryMemberCard);

    int insertOrUpdate(UserBatteryMemberCard userBatteryMemberCard);
}
