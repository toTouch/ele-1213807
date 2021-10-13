package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.OrderProtocolQuery;
import com.xiliulou.electricity.service.OrderProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonAdminOrderProtocolController extends BaseController {
    @Autowired
    OrderProtocolService orderProtocolService;

    @GetMapping("/admin/orderProtocol")
    public R queryUserNotice() {

        return orderProtocolService.queryOrderProtocol();
    }



    @PutMapping("/admin/depositProtocol")
    public R update(@Validated @RequestBody OrderProtocolQuery orderProtocolQuery) {
        return returnTripleResult(orderProtocolService.update(orderProtocolQuery));

    }

}
