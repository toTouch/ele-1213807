package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;
import org.apache.ibatis.annotations.Delete;

/**
 * (ElectricityBatteryBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeBindElectricityBatteryMapper extends BaseMapper<FranchiseeBindElectricityBattery> {

    @Delete("delete  FROM t_electricity_battery_bind  WHERE franchisee_id = #{franchiseeId}")
    void deleteByFranchiseeId(Long franchiseeId);
}
