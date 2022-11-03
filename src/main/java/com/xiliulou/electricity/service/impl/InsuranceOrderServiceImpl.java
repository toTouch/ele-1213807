package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜保险用户绑定(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("insuranceOrderService")
@Slf4j
public class InsuranceOrderServiceImpl extends ServiceImpl<InsuranceOrderMapper, InsuranceOrder> implements InsuranceOrderService {

    @Resource
    InsuranceOrderMapper insuranceOrderMapper;


}
