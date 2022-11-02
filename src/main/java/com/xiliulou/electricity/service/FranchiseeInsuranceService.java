package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.FranchiseeInsurance;

import java.util.List;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
public interface FranchiseeInsuranceService {


    R add(FranchiseeInsurance franchiseeInsurance);

    R update(FranchiseeInsurance franchiseeInsurance);

    R delete(Integer id);

    R queryList(Long offset, Long size, Integer status, Integer type,Integer tenantId,Long franchiseeId);

}
