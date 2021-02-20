package com.xiliulou.electricity.controller.user;

import com.xiliulou.electricity.service.EleUserAuthService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 实名认证信息(TEleUserAuth)表控制层
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
@RestController
public class EleUserAuthController {
    /**
     * 服务对象
     */
    @Resource
    private EleUserAuthService eleUserAuthService;

}