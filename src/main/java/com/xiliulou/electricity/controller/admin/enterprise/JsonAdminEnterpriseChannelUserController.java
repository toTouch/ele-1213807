package com.xiliulou.electricity.controller.admin.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.enterprise.EnterpriseUserAdminExitCheckRequest;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/5/13 9:51
 * @desc
 */

@Slf4j
@RestController
public class JsonAdminEnterpriseChannelUserController extends BaseController {
    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    /**
     * 骑手自主续费
     * @param request
     * @return
     */
    @PostMapping( "/admin/enterprise/channelUserExit")
    public R channelUserExit(@RequestBody @Validated EnterpriseUserAdminExitCheckRequest request) {
    
        return returnTripleResult(enterpriseChannelUserService.channelUserExitForAdmin(request));
    }
    
    
}
