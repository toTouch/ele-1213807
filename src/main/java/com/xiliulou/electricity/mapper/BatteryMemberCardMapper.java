package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryMemberCard;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryMemberCard)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
public interface BatteryMemberCardMapper extends BaseMapper<BatteryMemberCard> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMemberCard queryById(Integer id);

    /**
     * 修改数据
     *
     * @param batteryMemberCard 实例对象
     * @return 影响行数
     */
    int update(BatteryMemberCard batteryMemberCard);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}
