package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 小程序-电费
 * @date 2024/2/20 14:10:49
 */

@RestController
@Slf4j
public class JsonUserMerchantCabinetPowerController extends BaseController {
    
    @Resource
    private MerchantCabinetPowerService merchantCabinetPowerService;
    
    /**
     * 是否显示电费页面：0-不显示，1-显示
     */
    @GetMapping("/merchant/power/isShowPowerPage")
    public R isShowPowerPage() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantCabinetPowerService.isShowPowerPage(user.getUid()));
    }
    
    /**
     * 筛选条件：场地列表/柜机列表
     */
    @GetMapping("/merchant/power/placeAndCabinetList")
    public R placeAndCabinetList() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        log.info("==============uid={}", user.getUid());
        
        return R.ok(merchantCabinetPowerService.listPlaceAndCabinetByMerchantId(user.getUid()));
    }
    
    /**
     * 筛选条件：根据场地id查询柜机列表
     */
    @GetMapping("/merchant/power/cabinetListByPlace")
    public R cabinetListByPlace(@RequestParam Long placeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantCabinetPowerService.listCabinetByPlaceId(user.getUid(), placeId));
    }
    
    /**
     * 电量/电费：今日、昨日、本月、上月、累计、柜机列表
     */
    @GetMapping("/merchant/power/powerData")
    public R powerData(@RequestParam(value = "placeId", required = false) Long placeId, @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().uid(user.getUid()).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.powerData(request));
    }
    
    /**
     * 统计分析-折线图 近N个自然月（不包含本月）
     */
    @GetMapping("/merchant/power/lineData")
    public R lineData(@RequestParam(value = "placeId", required = false) Long placeId, @RequestParam(value = "cabinetId", required = false) Long cabinetId,
            @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().uid(user.getUid()).placeId(placeId).cabinetId(cabinetId).startTime(startTime).endTime(endTime)
                .build();
        
        return R.ok(merchantCabinetPowerService.lineData(request));
    }
    
    /**
     * 柜机电费列表
     */
    @GetMapping("/merchant/power/cabinetPowerList")
    public R cabinetPowerList(@RequestParam(value = "placeId", required = false) Long placeId, @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().uid(user.getUid()).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.cabinetPowerList(request));
    }
    
    /**
     * 柜机电费详情
     */
    @GetMapping("/merchant/power/cabinetPowerDetail")
    public R cabinetPowerDetail(@RequestParam(value = "cabinetId") Long cabinetId, @RequestParam(value = "monthDate") String monthDate) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().uid(user.getUid()).cabinetId(cabinetId).monthDate(monthDate).build();
        
        return R.ok(merchantCabinetPowerService.cabinetPowerDetail(request));
    }
}
