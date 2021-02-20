package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.EleAuthEntryService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 实名认证资料项(TEleAuthEntry)表控制层
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
@RestController
public class EleAuthEntryController {
    /**
     * 服务对象
     */
    @Resource
    private EleAuthEntryService eleAuthEntryService;

}