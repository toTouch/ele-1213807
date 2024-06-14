package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRequest;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @description 小程序-场地费
 * @date 2024/2/20 14:10:49
 */

@RestController
@Slf4j
public class JsonUserMerchantCabinetPlaceController extends BaseController {
    
    @Resource
    MerchantCabinetPowerService merchantCabinetPowerService;
    
    @Resource
    private MerchantPlaceFeeService merchantPlaceFeeService;
    
    @Resource
    private MerchantService merchantService;
    
    /**
     * 是否显示场地费页面：0-不显示，1-显示
     */
    @GetMapping("/merchant/place/isShowPlacePage")
    public R isShowPlacePage() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place is Show Place Page merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantPlaceFeeService.isShowPlacePage(merchant.getId()));
    }
    
    /**
     * 筛选条件：场地列表/柜机列表
     */
    @GetMapping("/merchant/place/placeAndCabinetList")
    public R placeAndCabinetList() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place is Show Place Page merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantCabinetPowerService.listPlaceAndCabinetByMerchantId(merchant.getUid()));
    }
    
    /**
     * 筛选条件：根据场地id查询柜机列表
     */
    @GetMapping("/merchant/place/cabinetListByPlace")
    public R cabinetListByPlace(@RequestParam Long placeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place is Show Place Page merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantCabinetPowerService.listCabinetByPlaceId(merchant.getUid(), placeId));
    }
    
    /**
     * 统计上月，本月，累计场地费 统计设备数量
     */
    @GetMapping("/merchant/place/getFeeData")
    public R getFeeData(@RequestParam(value = "placeId", required = false) Long placeId, @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place get fee data merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchant.getId()).tenantId(TenantContextHolder.getTenantId()).placeId(placeId)
                .cabinetId(cabinetId).build();
        
        return R.ok(merchantPlaceFeeService.getFeeData(request));
    }
    
    /**
     * 统计分析-折线图
     */
    @GetMapping("/merchant/place/getLineData")
    public R lineData(@RequestParam(value = "placeId", required = false) Long placeId, @RequestParam(value = "cabinetId", required = false) Long cabinetId,
            @RequestParam(value = "startTime") Long startTime, @RequestParam(value = "endTime") Long endTime) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place is Show Place Page merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().tenantId(TenantContextHolder.getTenantId()).merchantId(merchant.getId()).placeId(placeId)
                .cabinetId(cabinetId).startTime(startTime).endTime(endTime).build();
        
        return R.ok(merchantPlaceFeeService.lineData(request));
    }
    
    /**
     * 根据柜机id获取场地费
     */
    @GetMapping("/merchant/place/getPlaceDetailByCabinetId")
    public R getPlaceDetailByCabinetId(@RequestParam("month") String month, @RequestParam(value = "cabinetId") Long cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place is Show Place Page merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchant.getId()).month(month).tenantId(TenantContextHolder.getTenantId())
                .cabinetId(cabinetId).build();
        
        return R.ok(merchantPlaceFeeService.getPlaceDetailByCabinetId(request));
    }
    
    /**
     * 柜机场地费详情
     */
    @GetMapping("/merchant/place/getCabinetPlaceDetail")
    public R getCabinetPlaceDetail(@RequestParam(value = "placeId", required = false) Long placeId, @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant place is Show Place Page merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchant.getId()).placeId(placeId).tenantId(TenantContextHolder.getTenantId())
                .cabinetId(cabinetId).build();
        
        return R.ok(merchantPlaceFeeService.getCabinetPlaceDetail(request));
    }
    
    
}
