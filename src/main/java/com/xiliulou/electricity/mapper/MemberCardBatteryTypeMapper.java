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

    Integer batchInsert(List<MemberCardBatteryType> memberCardBatteryTypeList);

    List<String> selectBatteryTypeByMid(@Param("mid") Long mid);
    
    List<MemberCardBatteryType> selectListByMemberCardIds(@Param("tenantId") Integer tenantId, @Param("memberCardIds") List<Long> memberCardIds);

    List<Long> selectMemberCardIdsByBatteryType(@Param("tenantId") Integer tenantId,@Param("batteryType")  String batteryType);

    List<MemberCardBatteryType> selectListByMemberCardIdsAndModel(@Param("tenantId") Integer tenantId, @Param("memberCardIds") List<Long> memberCardIds, @Param("model")  String model);
}
