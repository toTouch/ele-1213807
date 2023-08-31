package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.PxzConfigQuery;
import com.xiliulou.electricity.service.PxzConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2023/2/16 11:17
 */
@RestController
public class JsonAdminPxzController extends BaseController {
    
    @Autowired
    PxzConfigService pxzConfigService;
    
    @GetMapping("/admin/pxz/info")
    public R queryConfig() {
        return returnPairResult(pxzConfigService.queryByInfo());
    }
    
    @PostMapping("/admin/pxz/save")
    public R saveConfig(@RequestBody @Validated PxzConfigQuery pxzConfigQuery) {
        return returnPairResult(pxzConfigService.save(pxzConfigQuery));
    }
    
    @PutMapping("/admin/pxz/update")
    public R modifyConfig(@RequestBody @Validated PxzConfigQuery pxzConfigQuery) {
        return returnPairResult(pxzConfigService.modify(pxzConfigQuery));
    }

    /**
     * 校验当前用户是否可免押
     */
    @GetMapping("/admin/pxz/check")
    public R check() {
        return returnPairResult(pxzConfigService.check());
    }

}
