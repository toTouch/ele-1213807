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
    
    Integer batchInsert(List<UserBatteryType> userBatteryType);
    
    Integer deleteByUid(Long uid);
    
    List<String> selectByUid(Long uid);
    
    String selectOneByUid(Long uid);
    
    int updateByUid(UserBatteryType userBatteryType);
    
    List<UserBatteryType> selectListByUid(Long uid);
    
    Integer deleteByUidAndBatteryTypes(@Param("uid") Long uid, @Param("batteryTypes") List<String> batteryTypes);
}
