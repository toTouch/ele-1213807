package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserBatteryServiceFee;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserBatteryServiceFee)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-06 13:39:50
 */
public interface UserBatteryServiceFeeMapper extends BaseMapper<UserBatteryServiceFee> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryServiceFee selectByUid(@Param("uid") Long uid);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param userBatteryServiceFee 实例对象
     * @return 对象列表
     */
    List<UserBatteryServiceFee> selectByQuery(UserBatteryServiceFee userBatteryServiceFee);
    
    /**
     * 新增数据
     *
     * @param userBatteryServiceFee 实例对象
     * @return 影响行数
     */
    int insertOne(UserBatteryServiceFee userBatteryServiceFee);
    
    /**
     * 修改数据
     *
     * @param userBatteryServiceFee 实例对象
     * @return 影响行数
     */
    int updateByUid(UserBatteryServiceFee userBatteryServiceFee);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(@Param("uid") Long uid);

    int insertOrUpdate(UserBatteryServiceFee userBatteryServiceFee);
}
