package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.service.EleWarnMsgService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表控制层
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
@RestController
public class EleWarnMsgController {
    /**
     * 服务对象
     */
    @Resource
    private EleWarnMsgService eleWarnMsgService;

}