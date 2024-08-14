package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantInviterModifyRecordRequest;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 邀请人修改记录
 * @date 2024/3/28 09:27:12
 */
@Slf4j
@RestController
public class JsonMerchantInviterModifyRecordController {
    
    @Resource
    private MerchantInviterModifyRecordService merchantInviterModifyRecordService;
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/merchant/inviterModifyRecord/page")
    public R page(@RequestParam("uid") Long uid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        MerchantInviterModifyRecordRequest request = MerchantInviterModifyRecordRequest.builder().uid(uid).build();
        
        return R.ok(merchantInviterModifyRecordService.listByPage(request));
    }
    
    @GetMapping("/admin/merchant/inviterModifyRecord/pageCount")
    public R pageCount(@RequestParam("uid") Long uid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        MerchantInviterModifyRecordRequest request = MerchantInviterModifyRecordRequest.builder().uid(uid).build();
        
        return R.ok(merchantInviterModifyRecordService.countTotal(request));
    }
}
