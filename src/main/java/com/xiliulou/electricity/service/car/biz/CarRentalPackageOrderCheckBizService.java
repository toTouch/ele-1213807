package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.core.web.R;


/**
 * description: 租车套餐校验服务
 *
 * @author caobotao.cbt
 * @date 2024/11/20 09:20
 */
public interface CarRentalPackageOrderCheckBizService {
    
    /**
     * name: <br/> description:
     *
     * @param tenantId   租户id
     * @param uid        用户id
     * @param freezeDays 冻结天数
     * @return 是否可以自动审核
     * @author caobotao.cbt
     * @date 2024/11/20 09:44
     */
    R<Boolean> checkFreezeLimit(Integer tenantId, Long uid, Integer freezeDays, boolean hasAssets);
    
}