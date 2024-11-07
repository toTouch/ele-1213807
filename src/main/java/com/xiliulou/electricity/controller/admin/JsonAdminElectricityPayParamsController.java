package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.controller.admin.base.AbstractFranchiseeDataPermissionController;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import com.xiliulou.electricity.vo.FranchiseeIdNameVO;
import com.xiliulou.electricity.vo.FranchiseeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:35
 **/
@Slf4j
@RestController
public class JsonAdminElectricityPayParamsController extends AbstractFranchiseeDataPermissionController {
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    
    @PostMapping(value = "/admin/electricityPayParams/uploadFile")
    @Log(title = "上传支付文件")
    public R save(@RequestParam("file") MultipartFile file, @RequestParam(value = "type", required = false) Integer type, @RequestParam("franchiseeId") Long franchiseeId) {
        return electricityPayParamsService.uploadFile(file, type, franchiseeId);
    }
    
    
    @GetMapping(value = "/admin/electricityPayParams")
    public R queryList() {
        Integer tenantId = TenantContextHolder.getTenantId();
        List<ElectricityPayParamsVO> vos = electricityPayParamsService.queryByTenantId(tenantId);
        return R.ok(vos);
    }
    
    
    @PostMapping(value = "/admin/electricityPayParams")
    @Log(title = "新增支付参数")
    public R insert(@RequestBody @Validated(CreateGroup.class) ElectricityPayParamsRequest request) {
        return electricityPayParamsService.insert(request);
    }
    
    
    @PutMapping(value = "/admin/electricityPayParams")
    @Log(title = "更新支付参数")
    public R update(@RequestBody @Validated(UpdateGroup.class) ElectricityPayParamsRequest request) {
        return electricityPayParamsService.update(request);
    }
    
    
    @DeleteMapping(value = "/admin/electricityPayParams/{id}")
    @Log(title = "删除支付参数")
    public R delete(@PathVariable("id") Integer id) {
        return electricityPayParamsService.delete(id);
    }
    
    
    
    @GetMapping(value = "/admin/electricityPayParams/queryFranchisee")
    public R queryFranchisee() {
        List<FranchiseeIdNameVO> franchiseeVOS = electricityPayParamsService.queryFranchisee(TenantContextHolder.getTenantId(),checkFranchiseeDataPermission());
        return R.ok(franchiseeVOS);
    }
    
    
}
