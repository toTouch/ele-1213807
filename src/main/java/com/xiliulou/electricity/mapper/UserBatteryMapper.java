package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserBattery;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserBattery)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-06 13:39:24
 */
public interface UserBatteryMapper extends BaseMapper<UserBattery> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBattery selectByUid(@Param("uid") Long uid);
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param userBattery 实例对象
     * @return 对象列表
     */
    List<UserBattery> selectByQuery(UserBattery userBattery);
    
    /**
     * 新增数据
     *
     * @param userBattery 实例对象
     * @return 影响行数
     */
    int insertOne(UserBattery userBattery);
    
    /**
     * 修改数据
     *
     * @param userBattery 实例对象
     * @return 影响行数
     */
    int updateByUid(UserBattery userBattery);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(@Param("uid") Long uid);

    int insertOrUpdate(UserBattery userBattery);
}
