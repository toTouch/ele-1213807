package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.ChannelActivityService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

/**
 * (ChannelActivity)表控制层
 *
 * @author zgw
 * @since 2023-03-22 10:42:57
 */
@RestController
public class JsonAdminChannelActivityController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private ChannelActivityService channelActivityService;
    
    @GetMapping("/admin/channelActivity/list")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size) {
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 50L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        return returnTripleResult(channelActivityService.queryList(offset, size));
    }
    
    @GetMapping("/admin/channelActivity/queryCount")
    public R queryCount() {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        return returnTripleResult(channelActivityService.queryCount());
    }
    
    @PutMapping("/admin/channelActivity/status")
    public R updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        return returnTripleResult(channelActivityService.updateStatus(id, status));
    }
}
