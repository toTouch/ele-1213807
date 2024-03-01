package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetBindSaveRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetPageRequest;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindTimeCheckVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/16 20:20
 * @desc
 */
@Slf4j
@RestController
public class JsonMerchantPlaceCabinetBindController extends BaseController {
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private MerchantService merchantService;
    
    /**
     * 绑定换电柜
     *
     * @param placeCabinetBindSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/place/cabinet/bind")
    public R bind(@RequestBody @Validated(value = CreateGroup.class) MerchantPlaceCabinetBindSaveRequest placeCabinetBindSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean, String, Object> r = merchantPlaceCabinetBindService.bind(placeCabinetBindSaveRequest);
        
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
    
        if (Objects.isNull(r.getRight())) {
            return R.ok();
        }
        
        // 删除商户缓存
        Merchant merchant = (Merchant) r.getRight();
        merchantService.deleteCacheById(merchant.getId());
        
        return R.ok();
    }
    
    /**
     * 绑定换电柜
     *
     * @param placeCabinetBindSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/place/cabinet/unBind")
    public R unBind(@RequestBody @Validated(value = UpdateGroup.class) MerchantPlaceCabinetBindSaveRequest placeCabinetBindSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean, String, Object> r = merchantPlaceCabinetBindService.unBind(placeCabinetBindSaveRequest);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        return R.ok();
    }
    
    /**
     * 删除场地
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/admin/merchant/place/cabinet/delete")
    @Log(title = "删除")
    public R delete(@RequestParam(value = "id", required = true) Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean, String, Object> r = merchantPlaceCabinetBindService.remove(id);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        return R.ok();
    }
    
    /**
     * @param
     * @description 商户列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/cabinet/pageCount")
    public R pageCount(@RequestParam("placeId") Long placeId, @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "status", required = false) Integer status) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        Integer tenantId = null;
        if (!SecurityUtils.isAdmin()) {
            tenantId = TenantContextHolder.getTenantId();
        }
    
        MerchantPlaceCabinetPageRequest placeCabinetPageRequest = MerchantPlaceCabinetPageRequest.builder().sn(sn).status(status).tenantId(tenantId).build();
        
        return R.ok(merchantPlaceCabinetBindService.countTotal(placeCabinetPageRequest));
    }
    
    /**
     * @param
     * @description 商户列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/cabinet/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam("placeId") Long placeId, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "status", required = false) Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        Integer tenantId = null;
        if (!SecurityUtils.isAdmin()) {
            tenantId = TenantContextHolder.getTenantId();
        }
        
        MerchantPlaceCabinetPageRequest placeCabinetPageRequest = MerchantPlaceCabinetPageRequest.builder().placeId(placeId).sn(sn).size(size).offset(offset).tenantId(tenantId)
                .status(status).build();
        
        return R.ok(merchantPlaceCabinetBindService.listByPage(placeCabinetPageRequest));
    }
    
    /**
     * 检测场地的绑定和解绑时间是否重复
     *
     * @param placeId
     * @return
     */
    @GetMapping("/admin/merchant/place/cabinet/checkBindTime")
    public R checkBindTime(@RequestParam("placeId") Long placeId,@RequestParam("cabinetId") Integer cabinetId, @RequestParam("time") Long time) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        MerchantPlaceCabinetBindTimeCheckVo vo = merchantPlaceCabinetBindService.checkBindTime(placeId, time, cabinetId);
        
        return R.ok(vo);
    }
}
