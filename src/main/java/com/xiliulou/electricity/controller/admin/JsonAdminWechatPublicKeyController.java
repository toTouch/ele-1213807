package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.admin.base.AbstractFranchiseeDataPermissionController;
import com.xiliulou.electricity.service.pay.WechatPublicKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:35
 **/
@Slf4j
@RestController
@RequestMapping("/admin/wechatPublicKey")
public class JsonAdminWechatPublicKeyController extends AbstractFranchiseeDataPermissionController {
    
    
    private final WechatPublicKeyService wechatPublicKeyService;
    
    public JsonAdminWechatPublicKeyController(WechatPublicKeyService wechatPublicKeyService) {
        this.wechatPublicKeyService = wechatPublicKeyService;
    }
    
    
    @PostMapping(value = "/save")
    public R<?> save(@RequestParam("file") MultipartFile file, @RequestParam("franchiseeId") Long franchiseeId) {
        return wechatPublicKeyService.uploadFile(file, franchiseeId);
    }
    
    @DeleteMapping
    public R<?> delete(@Validated @NotNull @RequestParam("id") Long id) {
        wechatPublicKeyService.delete(id);
        return R.ok();
    }
    
    
}
