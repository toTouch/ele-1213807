package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.pay.WechatPublicKeyBO;
import com.xiliulou.electricity.controller.admin.base.AbstractFranchiseeDataPermissionController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.payparams.WechatPublicKeyRequest;
import com.xiliulou.electricity.service.pay.WechatPublicKeyService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.vo.WechatPublicKeyVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:35
 **/
@Slf4j
@RestController
@RequestMapping("/admin/electricityPayParams/wechatPublicKey")
public class JsonAdminWechatPublicKeyController extends AbstractFranchiseeDataPermissionController {
    
    
    private final WechatPublicKeyService wechatPublicKeyService;
    
    public JsonAdminWechatPublicKeyController(WechatPublicKeyService wechatPublicKeyService) {
        this.wechatPublicKeyService = wechatPublicKeyService;
    }
    
    
    @PostMapping
    public R<?> save(@RequestBody @Validated(value = CreateGroup.class) WechatPublicKeyRequest request) {
        checkUserDataScope();
        WechatPublicKeyBO build = WechatPublicKeyBO.builder().payParamsId(request.getPayParamsId()).pubKey(request.getPubKey())
                .franchiseeId(request.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).pubKeyId(request.getPubKeyId()).uploadTime(System.currentTimeMillis()).build();
        return wechatPublicKeyService.saveOrUpdate(build);
    }
    
    @PutMapping
    public R<?> edit(@RequestBody @Validated(value = UpdateGroup.class) WechatPublicKeyRequest request) {
        checkUserDataScope();
        WechatPublicKeyBO build = WechatPublicKeyBO.builder().id(request.getId()).payParamsId(request.getPayParamsId()).pubKey(request.getPubKey())
                .franchiseeId(request.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).pubKeyId(request.getPubKeyId()).uploadTime(System.currentTimeMillis()).build();
        return wechatPublicKeyService.saveOrUpdate(build);
    }
    
    //根据加盟商查询微信公钥信息
    @GetMapping
    public R<?> queryByTenantId(@Validated @NotNull @RequestParam("franchiseeId") Long franchiseeId) {
        checkUserDataScope();
        WechatPublicKeyBO wechatPublicKeyBO = wechatPublicKeyService.queryByTenantIdFromCache(TenantContextHolder.getTenantId(), franchiseeId);
        return R.ok(convertVO(wechatPublicKeyBO));
    }
    
    @DeleteMapping
    public R<?> remove(@Validated @NotNull @RequestParam("id") Long id) {
        wechatPublicKeyService.remove(id);
        return R.ok();
    }
    
    
    private WechatPublicKeyVO convertVO(WechatPublicKeyBO wechatPublicKeyBO){
        if (Objects.isNull(wechatPublicKeyBO)){
            return null;
        }
        return WechatPublicKeyVO
                .builder()
                .id(wechatPublicKeyBO.getId())
                .pubKey(wechatPublicKeyBO.getPubKey())
                .pubKeyId(wechatPublicKeyBO.getPubKeyId())
                .uploadTime(wechatPublicKeyBO.getUploadTime())
                .updateTime(wechatPublicKeyBO.getUpdateTime())
                .createTime(wechatPublicKeyBO.getCreateTime())
                .franchiseeId(wechatPublicKeyBO.getFranchiseeId())
                .tenantId(wechatPublicKeyBO.getTenantId())
                .payParamsId(wechatPublicKeyBO.getPayParamsId())
                .build();
    }
}
