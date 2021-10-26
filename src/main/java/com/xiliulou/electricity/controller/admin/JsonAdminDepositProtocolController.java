package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.DepositProtocolQuery;
import com.xiliulou.electricity.service.DepositProtocolService;
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
public class JsonAdminDepositProtocolController extends BaseController {
    @Autowired
    DepositProtocolService depositProtocolService;

    @GetMapping("/admin/depositProtocol")
    public R queryUserNotice() {

        return depositProtocolService.queryDepositProtocol();
    }



    @PutMapping("/admin/depositProtocol")
    public R update(@Validated @RequestBody DepositProtocolQuery depositProtocolQuery) {
        return returnTripleResult(depositProtocolService.update(depositProtocolQuery));

    }

}
