package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.InsuranceInstruction;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.mapper.InsuranceInstructionMapper;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.service.InsuranceInstructionService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 换电柜保险用户绑定(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("insuranceInstructionService")
@Slf4j
public class InsuranceInstructionServiceImpl extends ServiceImpl<InsuranceInstructionMapper, InsuranceInstruction> implements InsuranceInstructionService {

    @Resource
    InsuranceInstructionMapper insuranceInstructionMapper;


    @Override
    public int insert(InsuranceInstruction insuranceInstruction) {
        return insuranceInstructionMapper.insert(insuranceInstruction);
    }

    @Override
    public int update(InsuranceInstruction insuranceInstruction) {
        return insuranceInstructionMapper.update(insuranceInstruction);
    }
}
