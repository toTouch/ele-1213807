package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.MemberCardBatteryType;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (MemberCardBatteryType)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-07 14:07:42
 */
public interface MemberCardBatteryTypeMapper extends BaseMapper<MemberCardBatteryType> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    MemberCardBatteryType queryById(Long id);

    /**
     * 修改数据
     *
     * @param memberCardBatteryType 实例对象
     * @return 影响行数
     */
    int update(MemberCardBatteryType memberCardBatteryType);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
