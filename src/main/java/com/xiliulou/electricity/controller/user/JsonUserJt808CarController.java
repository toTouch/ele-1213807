package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CarControlQuery;
import com.xiliulou.electricity.service.Jt808CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zgw
 * @date 2023/4/10 10:11
 * @mood
 */
@RestController
public class JsonUserJt808CarController extends BaseController {
    
    @Autowired
    private Jt808CarService jt808CarService;
    
    @PostMapping("/user/jt808/car/control")
    public R controlCar(@RequestBody @Validated CarControlQuery query) {
        return returnTripleResult(jt808CarService.userControlCar(query));
    }
    
    @GetMapping("/user/jt808/car/control/check")
    public R controlCarCheck() {
        return returnTripleResult(jt808CarService.controlCarCheck());
    }
}
