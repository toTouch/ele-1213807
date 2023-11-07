package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (City)表数据库访问层
 *
 * @author makejava
 * @since 2021-01-21 18:05:41
 */
public interface BatteryOtherPropertiesMapper extends BaseMapper<BatteryOtherProperties>{
    
    /**
     * 通过电池sn获取list
     *
     * @param snList 实例对象
     * @return 对象列表
     */
    List<BatteryOtherProperties> selectBatteryOtherPropertiesList(@Param("snList") List<String> snList);
}
