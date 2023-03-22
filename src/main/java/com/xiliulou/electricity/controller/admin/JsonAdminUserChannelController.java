package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserChannelQuery;
import com.xiliulou.electricity.service.UserChannelService;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (UserChannel)表控制层
 *
 * @author Hardy
 * @since 2023-03-22 15:34:58
 */
@RestController
public class JsonAdminUserChannelController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserChannelService userChannelService;
    
    @GetMapping("admin/userChannel/list")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone) {
        return this.returnTripleResult(userChannelService.queryList(offset, size, name, phone));
    }
    
    @GetMapping("admin/userChannel/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone) {
        return this.returnTripleResult(userChannelService.queryCount(name, phone));
    }
    
    @PostMapping("admin/userChannel/save")
    public R saveOne(@RequestBody @Validated(value = CreateGroup.class) UserChannelQuery userChannelQuery) {
        return this.returnTripleResult(userChannelService.saveOne(userChannelQuery));
    }
}
