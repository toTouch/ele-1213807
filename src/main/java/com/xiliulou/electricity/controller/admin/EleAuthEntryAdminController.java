package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.service.EleAuthEntryService;
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
public class EleAuthEntryAdminController {
    /**
     * 服务对象
     */
    @Autowired
    EleAuthEntryService eleAuthEntryService;

    @Autowired
    RedisService redisService;

   /* *//**
     * 新增资料项
     *
     *//*
    @PostMapping(value = "/admin/authEntry")
    public R batchInsertAuthEntry(@RequestBody List<EleAuthEntry> eleAuthEntryList) {
        if (ObjectUtil.isEmpty(eleAuthEntryList)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        //限频
        Boolean getLockSuccess = redisService.setNx(
                ElectricityCabinetConstant.ELE_CACHE_AUTH_ENTRY_LOCK_KEY
                , IdUtil.fastSimpleUUID(), 5*1000L,false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        return eleAuthEntryService.batchInsertAuthEntry(eleAuthEntryList);
    }*/

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
        return R.ok(eleAuthEntryService.getEleAuthEntriesList());
    }

}