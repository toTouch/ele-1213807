package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 租车押金的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/deposit")
public class JsonUserCarDepositController extends BasicController {

    /**
     * 退押申请
     * @param depositOrderNo
     * @return
     */
    @GetMapping("/refundDepositOrder")
    public R<Boolean> refundDepositOrder(String depositOrderNo) {
        if (StringUtils.isBlank(depositOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // TODO 退押逻辑
        // 没有设备、没有滞纳金
        // 优惠券除了保存订单来源之外，还需要记录套餐类型，方便统一失效

        return R.ok();

    }

}
