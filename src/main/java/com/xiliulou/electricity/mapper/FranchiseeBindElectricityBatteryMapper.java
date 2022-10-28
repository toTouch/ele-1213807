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
@Deprecated
public interface FranchiseeBindElectricityBatteryMapper extends BaseMapper<FranchiseeBindElectricityBattery> {

    @Delete("delete  FROM t_franchisee_bind_electricity_battery  WHERE franchisee_id = #{franchiseeId}")
    void deleteByFranchiseeId(Integer franchiseeId);
}
