package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.InsuranceInstruction;
import com.xiliulou.electricity.entity.InsuranceOrder;

/**
 * 换电柜保险说明(InsuranceInstruction)表数据库访问层
 *
 * @author makejava
 * @since 2022-11-03 14:44:12
 */
public interface InsuranceInstructionMapper extends BaseMapper<InsuranceInstruction> {

    int update(InsuranceInstruction insuranceInstruction);
}
