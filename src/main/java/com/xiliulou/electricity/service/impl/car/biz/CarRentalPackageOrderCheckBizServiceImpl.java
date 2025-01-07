package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.UserInfoExtraMapper;
import com.xiliulou.electricity.reqparam.opt.carpackage.FreezeRentOrderOptReq;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderCheckBizService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/11/20 09:39
 */
@Slf4j
@Service
@AllArgsConstructor
public class CarRentalPackageOrderCheckBizServiceImpl implements CarRentalPackageOrderCheckBizService {
    
    
    private final UserInfoExtraService userInfoExtraService;
    
    private final ElectricityConfigService electricityConfigService;
    
    @Override
    public R<Boolean> checkFreezeLimit(Integer tenantId, Long uid, Integer freezeDays) {
        
        // 校验次数限制
        try {
            R<Object> checkFreezeCount = userInfoExtraService.checkFreezeCount(tenantId, uid);
            if (!checkFreezeCount.isSuccess()) {
                return R.fail(checkFreezeCount.getErrCode(), checkFreezeCount.getErrMsg());
            }
            
            //校验冻结天数
            Boolean autoReview = electricityConfigService.checkFreezeAutoReviewAndDays(tenantId, freezeDays, uid);
            
            return R.ok(autoReview);
        } catch (BizException e) {
            log.info("checkFreezeLimit BizException:", e);
            return R.fail(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            log.error("checkFreezeLimit Exception:", e);
            return R.fail("000102", "系统异常！");
        }
    }
}
