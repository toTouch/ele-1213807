package com.xiliulou.electricity.service.car.v2;

import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;

import java.math.BigDecimal;

/**
 * 租车套餐押金业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRenalPackageDepositV2BizService {
    
    /**
     * 创建免押订单，生成二维码<br /> 创建押金缴纳订单、生成免押记录
     *
     * @param tenantId          租户ID
     * @param uid               C端用户ID
     * @param freeDepositOptReq 免押申请数据
     */
    String createFreeDeposit(Integer tenantId, Long uid, FreeDepositOptReq freeDepositOptReq);
    
}
