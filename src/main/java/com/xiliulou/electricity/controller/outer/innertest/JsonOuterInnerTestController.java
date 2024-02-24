package com.xiliulou.electricity.controller.outer.innertest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Ant
 * @className: JsonOuterInnerTestController
 * @description: 内部测试接口
 **/
@Slf4j
@RestController
@RequestMapping("/outer/inner/test")
public class JsonOuterInnerTestController {
    
    @GetMapping("/wx/transferBatches")
    public String openAccountNotify() {
        return "OK";
    }
    
}
