package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;

import java.util.List;

/**
 * (BatteryMemberCard)表服务接口
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
public interface BatteryMemberCardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMemberCard queryByIdFromDB(Integer id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMemberCard queryByIdFromCache(Integer id);

    /**
     * 修改数据
     *
     * @param batteryMemberCard 实例对象
     * @return 实例对象
     */
    Integer update(BatteryMemberCard batteryMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

}
