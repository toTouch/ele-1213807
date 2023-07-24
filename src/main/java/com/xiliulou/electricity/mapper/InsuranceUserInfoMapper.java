package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜电池表(InsuranceUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface InsuranceUserInfoMapper extends BaseMapper<InsuranceUserInfo> {

    int update(InsuranceUserInfo insuranceUserInfo);

    InsuranceUserInfoVo queryByUidAndTenantId(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);

    InsuranceUserInfo selectByUidAndTypeFromDB(@Param("uid") Long uid, @Param("type") Integer type);
}
