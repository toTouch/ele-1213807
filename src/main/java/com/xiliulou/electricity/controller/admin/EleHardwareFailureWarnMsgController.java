package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:02
 * @desc
 */

@RestController
@Slf4j
public class EleHardwareFailureWarnMsgController {
    @Resource
    private EleHardwareFailureWarnMsgService failureWarnMsgService;
}
