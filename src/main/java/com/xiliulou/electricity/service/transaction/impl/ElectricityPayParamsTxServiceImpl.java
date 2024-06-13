/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/13
 */

package com.xiliulou.electricity.service.transaction.impl;

import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.mapper.WechatPaymentCertificateMapper;
import com.xiliulou.electricity.mapper.WechatWithdrawalCertificateMapper;
import com.xiliulou.electricity.service.transaction.ElectricityPayParamsTxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/13 10:24
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ElectricityPayParamsTxServiceImpl implements ElectricityPayParamsTxService {
    
    @Resource
    private ElectricityPayParamsMapper electricityPayParamsMapper;
    
    @Resource
    private WechatPaymentCertificateMapper wechatPaymentCertificateMapper;
    
    @Resource
    private WechatWithdrawalCertificateMapper wechatWithdrawalCertificateMapper;
    
    @Override
    public void delete(Long id, Integer tenantId) {
        electricityPayParamsMapper.logicalDelete(id, tenantId);
        wechatPaymentCertificateMapper.logicalDeleteByPayParamsId(id, tenantId);
        wechatWithdrawalCertificateMapper.logicalDeleteByPayParamsId(id, tenantId);
    }
    
    @Override
    public void update(ElectricityPayParams update, List<Integer> franchiseePayParamIds) {
        electricityPayParamsMapper.update(update);
        
        electricityPayParamsMapper.updateSync(update,franchiseePayParamIds);
    }
}
