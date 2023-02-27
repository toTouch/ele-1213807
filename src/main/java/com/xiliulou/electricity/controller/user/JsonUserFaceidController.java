package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FaceidResultQuery;
import com.xiliulou.electricity.service.FaceidService;
import com.xiliulou.faceid.service.FaceidTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人脸核身
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-01-15:58
 */
@RestController
@Slf4j
public class JsonUserFaceidController extends BaseController {

    @Autowired
    private FaceidService faceidService;

    /**
     * 获取人脸核身token
     */
    @GetMapping(value = "/user/faceid/getToken")
    public R getToken() {
        return returnTripleResult(faceidService.getEidToken());
    }

    /**
     * 人脸核身结果
     */
    @PostMapping(value = "/user/faceid/verifyEidResult")
    public R verifyEidResult(@RequestBody @Validated FaceidResultQuery faceidResultQuery) {
        return returnTripleResult(faceidService.verifyEidResult(faceidResultQuery));
    }


}
