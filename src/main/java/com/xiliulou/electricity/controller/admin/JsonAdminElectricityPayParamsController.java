package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:35
 **/
@Slf4j
@RestController
public class JsonAdminElectricityPayParamsController {
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    /**
     * 新增/修改支付参数
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/electricityPayParams")
    @Log(title = "修改支付参数")
    public R save(@RequestBody @Validated ElectricityPayParams electricityPayParams) {
        return electricityPayParamsService.saveOrUpdateElectricityPayParams(electricityPayParams);
    }

    @PostMapping(value = "/admin/electricityPayParams/uploadFile")
    @Log(title = "上传支付文件")
    public R save(@RequestParam("file") MultipartFile file,@RequestParam(value = "type", required = false) Integer type) {
        return electricityPayParamsService.uploadFile(file,type);
    }

    /**
     * 获取支付参数
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/electricityPayParams")
    public R get() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(electricityPayParamsService.queryFromCache(tenantId));
    }
    
    
    
    @PostMapping(value = "/admin/electricityPayParams")
    @Log(title = "新增支付参数")
    public R insert(@RequestBody ElectricityPayParamsRequest request) {
        return electricityPayParamsService.saveOrUpdateElectricityPayParams(request);
    }
    

}
