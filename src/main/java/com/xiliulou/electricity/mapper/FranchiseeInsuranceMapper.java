package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.vo.BatteryStatisticalVo;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface FranchiseeInsuranceMapper extends BaseMapper<FranchiseeInsurance> {



}
