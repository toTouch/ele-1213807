package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-10-15:45
 */
@Slf4j
@RestController
public class JsonUserBatteryMemberCardController extends BaseController {


    @Autowired
    private BatteryMemberCardService batteryMemberCardService;

    /**
     * 用户端获取套餐列表
     */
    @GetMapping("/user/battery/memberCard/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "franchiseeId") Long franchiseeId,
                  @RequestParam(value = "batteryV") String batteryV,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "rentType", required = false) Integer rentType,
                  @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .batteryV(batteryV)
                .status(BatteryMemberCard.STATUS_UP)
                .rentType(rentType)
                .name(name)
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .build();

        return R.ok(batteryMemberCardService.selectByPageForUser(query));
    }

    /**
     * 用户端获取加盟商套餐电池电压
     */
    @GetMapping("/user/battery/memberCard/batteryV")
    public R selectMembercardBatteryV(@RequestParam(value = "franchiseeId") Long franchiseeId) {
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .status(BatteryMemberCard.STATUS_UP)
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .build();

        return R.ok(batteryMemberCardService.selectMembercardBatteryV(query));
    }
}
