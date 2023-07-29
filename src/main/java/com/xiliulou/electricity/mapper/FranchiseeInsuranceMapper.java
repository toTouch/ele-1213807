package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.*;
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

    Integer queryCount(@Param("status") Integer status, @Param("insuranceType") Integer insuranceType, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId, @Param("name") String name);

    int update(FranchiseeInsurance franchiseeInsurance);

    List<FranchiseeInsuranceVo> queryList(@Param("offset") Long offset, @Param("size") Long size, @Param("status") Integer status, @Param("insuranceType") Integer insuranceType, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);

    FranchiseeInsurance queryByFranchiseeIdAndBatteryType(@Param("franchiseeId") Long franchiseeId, @Param("batteryType") String batteryType, @Param("tenantId") Integer tenantId);

    Integer batchInsert(List<FranchiseeInsurance> franchiseeInsuranceList);

    List<FranchiseeInsuranceVo> queryInsuranceList( @Param("status") Integer status, @Param("insuranceType") Integer insuranceType, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId, @Param("batteryType") String batteryType);

    FranchiseeInsurance selectByFranchiseeIdAndType(@Param("franchiseeId") Long franchiseeId, @Param("insuranceType") int insuranceType, @Param("simpleBatteryType") String simpleBatteryType);

    Integer checkInsuranceExist(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate);

    Integer selectPageCount(FranchiseeInsuranceQuery query);

    List<FranchiseeInsuranceVo> selectByPage(FranchiseeInsuranceQuery query);

    FranchiseeInsurance selectInsuranceByType(FranchiseeInsuranceQuery query);
}
