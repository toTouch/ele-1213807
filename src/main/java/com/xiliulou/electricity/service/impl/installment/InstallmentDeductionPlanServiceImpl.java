package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.electricity.mapper.installment.InstallmentDeductionPlanMapper;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:53
 */
@Service
@Slf4j
public class InstallmentDeductionPlanServiceImpl implements InstallmentDeductionPlanService {

    @Autowired
    private InstallmentDeductionPlanMapper installmentDeductionPlanMapper;
}
