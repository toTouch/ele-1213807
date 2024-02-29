package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 17:28:03
 */

@RestController
@Slf4j
public class JsonUserMerchantJoinRecordController extends BaseController {
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    /**
     * 扫码二维码参与，生成记录
     * @param code
     * code规则：merchantId:inviterUid:inviterType
     * inviterType：1-商户本人 2-场地员工
     */
    @PostMapping("/user/merchant/joinRecord/joinScanCode")
    public R joinScanCode(@RequestParam String code) {
        return merchantJoinRecordService.joinScanCode(code);
    }

    /**
     * 商户端查询用户管理列表
     * @param size
     * @param offset
     * @param uid
     * @param name
     * @param type
     * @return
     */
    @GetMapping("/merchant/joinUserList")
    public R queryJoinUserList(@RequestParam(value = "size", required = false) Long size,
                               @RequestParam(value = "offset", required = false) Long offset,
                               @RequestParam(value = "uid", required = false) Long uid,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "type", required = true) Integer type) {

        if (size == null || size <= 0) {
            size = 10L;
        }

        if (offset == null || offset <= 0) {
            offset = 0L;
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        Long merchantUid = SecurityUtils.getUid();
        if (Objects.isNull(merchantUid)) {
            log.error("query join user list error for merchant, not found merchant");
            return R.fail("", "没有查询到相关商户");
        }

        MerchantJoinUserQueryMode merchantJoinUserQueryMode = MerchantJoinUserQueryMode.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .uid(uid)
                .type(type)
                .merchantUid(merchantUid)
                .tenantId(tenantId)
                .build();

        return R.ok(merchantJoinRecordService.selectJoinUserList(merchantJoinUserQueryMode));

    }

}
