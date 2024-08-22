package com.xiliulou.electricity.service.handler;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.enums.FreeDepositServiceWayEnums;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName: FreeDepositFactory
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:54
 */
@Component
public class FreeDepositFactory {
    
    @Resource
    private ApplicationContext applicationContext;
    
    public BaseFreeDepositService getFreeDepositWay(Integer pxzFreeDepositNum, Integer fyFreeDepositNum) {
        if (pxzFreeDepositNum > NumberConstant.ZERO) {
            // 拍小租
            return applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(FreeDepositServiceWayEnums.PXZ.getChannel()), BaseFreeDepositService.class);
        }
        if (fyFreeDepositNum > NumberConstant.ZERO) {
            //  蜂云
            return applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(FreeDepositServiceWayEnums.FY.getChannel()), BaseFreeDepositService.class);
        }
        throw new CustomBusinessException("免押次数未充值，请联系管理员");
    }
}
