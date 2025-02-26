package com.xiliulou.electricity.controller.user.lostuser;


import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.lostuser.LostUserBizService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 流失用户记录控制层
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:01
 */
@RestController
public class JsonUserLostUserController extends BaseController {
    @Resource
    private LostUserBizService lostUserBizService;
    
    /**
     * 检测骑手是否为流失用户
     * @return
     */
    
    @PostMapping("/user/lost/user/check")
    public R checkLostUser() {
        lostUserBizService.checkLostUser();
        
        return R.ok();
    }
}

