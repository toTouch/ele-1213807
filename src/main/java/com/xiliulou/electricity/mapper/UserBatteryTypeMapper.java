package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserBatteryType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (UserBatteryType)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-14 16:02:42
 */
public interface UserBatteryTypeMapper extends BaseMapper<UserBatteryType> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBatteryType queryById(Long id);
    
    /**
     * 修改数据
     *
     * @param userBatteryType 实例对象
     * @return 影响行数
     */
    int update(UserBatteryType userBatteryType);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    Integer batchInsert(List<UserBatteryType> userBatteryType);
    
    Integer deleteByUid(Long uid);
    
    List<String> selectByUid(Long uid);
    
    String selectOneByUid(Long uid);
    
    int updateByUid(UserBatteryType userBatteryType);
    
    List<UserBatteryType> selectListByUid(Long uid);
    
    Integer deleteByUidAndBatteryTypes(@Param("uid") Long uid, @Param("batteryTypes") List<String> batteryTypes);
}
