package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InsuranceInstruction;

/**
 * 换电柜保险说明(InsuranceInstruction)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
public interface InsuranceInstructionService {

    int insert(InsuranceInstruction insuranceInstruction);

    int update(InsuranceInstruction insuranceInstruction);

}
