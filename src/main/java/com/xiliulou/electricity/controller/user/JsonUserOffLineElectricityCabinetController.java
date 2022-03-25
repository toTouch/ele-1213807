package com.xiliulou.electricity.controller.user;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.OffLineElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 离线换电控制层
 *
 * @author HRP
 * @since 2022-03-02 14:03:36
 */

@RestController
@Slf4j
public class JsonUserOffLineElectricityCabinetController {

    @Autowired
    OffLineElectricityCabinetService offLineElectricityCabinetService;

    /**
     * 获取离线换电验证码
     * @return
     */
    @GetMapping("/user/offLineElectricityCabinet/verificationCode")
    public R queryVerificationCode(){
        return offLineElectricityCabinetService.generateVerificationCode();
    }

}
