/**
 * Create date: 2024/6/26
 */

package com.xiliulou.electricity.controller.outer.notify;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.notify.NotifyUserInfoOptRequest;
import com.xiliulou.electricity.request.notify.NotifyUserInfoQryRequest;
import com.xiliulou.electricity.service.notify.NotifyUserInfoService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * description: 运维设置微信公众号通知控制层
 *
 * @author caobotao.cbt
 * @date 2024/6/26 14:56
 */
@RestController
@Slf4j
public class JsonAdminNotifyUserInfoController {
    
    @Resource
    private NotifyUserInfoService notifyUserInfoService;
    
    
    /**
     * 新增
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/26 15:05
     */
    @PostMapping("/outer/notify/userInfo")
    public R create(@RequestBody @Validated(CreateGroup.class) NotifyUserInfoOptRequest request) {
        return notifyUserInfoService.insert(request);
    }
    
    
    /**
     * 更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/26 15:05
     */
    @PutMapping("/outer/notify/userInfo")
    public R update(@RequestBody @Validated(UpdateGroup.class) NotifyUserInfoOptRequest request) {
        return notifyUserInfoService.update(request);
    }
    
    
    /**
     * code 查询openid
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/26 15:05
     */
    @PostMapping("/outer/notify/user/getOpenid")
    public R getOpenid(@RequestBody @Validated NotifyUserInfoQryRequest.QueryByCode request) {
        return notifyUserInfoService.queryWechatOpenIdByCode(request.getCode());
    }
    
    
    /**
     * 根据openid查询
     *
     * @param openId
     * @author caobotao.cbt
     * @date 2024/6/26 15:05
     */
    @GetMapping("/outer/notify/userInfo/{id}")
    public R userInfo(@PathVariable(value = "id") String openId) {
        return notifyUserInfoService.queryByOpenIdFromCache(openId);
    }
    
    
    /**
     * 查询所有
     *
     * @param offset
     * @param size
     * @author caobotao.cbt
     * @date 2024/6/26 15:05
     */
    @GetMapping("/outer/notify/user/list")
    public R userInfo(@RequestParam("offset") Integer offset, @RequestParam("size") Integer size) {
        return notifyUserInfoService.queryAll(offset, size);
    }
    
}
