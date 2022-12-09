package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserBatteryDeposit;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserBatteryDeposit)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */
public interface UserBatteryDepositMapper extends BaseMapper<UserBatteryDeposit> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryDeposit selectByUid(@Param("uid") Long uid);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param userBatteryDeposit 实例对象
     * @return 对象列表
     */
    List<UserBatteryDeposit> selectByQuery(UserBatteryDeposit userBatteryDeposit);
    
    /**
     * 新增数据
     *
     * @param userBatteryDeposit 实例对象
     * @return 影响行数
     */
    int insertOne(UserBatteryDeposit userBatteryDeposit);
    
    /**
     * 修改数据
     *
     * @param userBatteryDeposit 实例对象
     * @return 影响行数
     */
    int updateByUid(UserBatteryDeposit userBatteryDeposit);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(Long uid);

    int insertOrUpdate(UserBatteryDeposit userBatteryDeposit);
}
