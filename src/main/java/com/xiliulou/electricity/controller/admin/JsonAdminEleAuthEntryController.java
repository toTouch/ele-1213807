package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 实名认证资料项(TEleAuthEntry)表控制层
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
@RestController
public class JsonAdminEleAuthEntryController {
    /**
     * 服务对象
     */
    @Autowired
    EleAuthEntryService eleAuthEntryService;

    @Autowired
    RedisService redisService;


    /**
     * 修改资料项
     *
     */
    @PutMapping(value = "/admin/authEntry")
    public R updateEleAuthEntries(@RequestBody List<EleAuthEntry> eleAuthEntryList) {
        if (ObjectUtil.isEmpty(eleAuthEntryList)) {
            return R.ok();
        }
        return eleAuthEntryService.updateEleAuthEntries(eleAuthEntryList);
    }

    /**
     * 获取资料项
     *
     */
    @GetMapping(value = "/admin/authEntry/list")
    public R getEleAuthEntriesList() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(eleAuthEntryService.getEleAuthEntriesList(tenantId));
    }



}
