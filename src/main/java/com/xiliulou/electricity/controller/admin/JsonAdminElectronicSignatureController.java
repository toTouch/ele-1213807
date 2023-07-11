package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleEsignConfigQuery;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.service.EleEsignConfigService;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Kenneth
 * @Date: 2023/7/10 11:20
 * @Description:
 */

@RestController
@Slf4j
public class JsonAdminElectronicSignatureController {

    @Autowired
    EleEsignConfigService eleEsignConfigService;

    @Autowired
    EleUserEsignRecordService eleUserEsignRecordService;

    @GetMapping(value = "/admin/esign/config")
    public R getEsignConfig() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(eleEsignConfigService.selectLatestByTenantId(tenantId));
    }

    @PostMapping(value = "/admin/esign/update")
    public R addOrUpdateEsignConfig(@RequestBody @Validated EleEsignConfigQuery eleEsignConfigQuery){
        return R.ok(eleEsignConfigService.insertOrUpdate(eleEsignConfigQuery));
    }

    @GetMapping(value = "/admin/esign/userRecords")
    public R getEsignUserRecords(@RequestParam("size") Long size,
                                 @RequestParam("offset") Long offset,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 @RequestParam(value = "signFinishStatus", required = false) Integer signFinishStatus){
        EleUserEsignRecordQuery eleUserEsignRecordQuery = EleUserEsignRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId().longValue())
                .size(size)
                .offset(offset)
                .name(name)
                .phone(phone)
                .signFinishStatus(signFinishStatus).build();
        return R.ok(eleUserEsignRecordService.queryUserEsignRecords(eleUserEsignRecordQuery));
    }

    @GetMapping(value = "/admin/esign/userRecordsCount")
    public R getEsignUserRecordsCount(@RequestParam("size") Long size,
                                      @RequestParam("offset") Long offset,
                                      @RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam(value = "signFinishStatus", required = false) Integer signFinishStatus){
        EleUserEsignRecordQuery eleUserEsignRecordQuery = EleUserEsignRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId().longValue())
                .size(size)
                .offset(offset)
                .name(name)
                .phone(phone)
                .signFinishStatus(signFinishStatus).build();
        return R.ok(eleUserEsignRecordService.queryCount(eleUserEsignRecordQuery));
    }

}
