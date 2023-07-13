package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleEsignConfigQuery;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.query.EsignCapacityDataQuery;
import com.xiliulou.electricity.query.EsignCapacityRechargeRecordQuery;
import com.xiliulou.electricity.service.EleCabinetSignatureService;
import com.xiliulou.electricity.service.EleEsignConfigService;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.service.EsignCapacityDataService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
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
public class JsonAdminElectronicSignatureController extends BaseController {

    @Autowired
    EleEsignConfigService eleEsignConfigService;

    @Autowired
    EleUserEsignRecordService eleUserEsignRecordService;

    @Autowired
    EsignCapacityDataService esignCapacityDataService;

    @Autowired
    private EleCabinetSignatureService eleCabinetSignatureService;

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
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

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
    public R getEsignUserRecordsCount(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam(value = "signFinishStatus", required = false) Integer signFinishStatus){

        EleUserEsignRecordQuery eleUserEsignRecordQuery = EleUserEsignRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId().longValue())
                .name(name)
                .phone(phone)
                .signFinishStatus(signFinishStatus).build();
        return R.ok(eleUserEsignRecordService.queryCount(eleUserEsignRecordQuery));
    }

    @PostMapping("/admin/addEsignCapacity")
    public R addEsignCapacity(@RequestBody @Validated(UpdateGroup.class) EsignCapacityDataQuery esignCapacityDataQuery){
        return R.ok(esignCapacityDataService.addEsignCapacityData(esignCapacityDataQuery));
    }

    @GetMapping("/admin/queryEsignCapacity")
    public R queryEsignCapacity(){
        return R.ok(esignCapacityDataService.queryCapacityDataByTenantId(TenantContextHolder.getTenantId().longValue()));
    }

    @GetMapping("/admin/queryEsinRechargeRecords")
    public R queryEsinRechargeRecords(@RequestParam("size") Long size,
                                     @RequestParam("offset") Long offset,
                                     @RequestParam(value = "tenantId", required = false) Long tenantId){
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER PERMISSION ERROR! No authority.");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        EsignCapacityRechargeRecordQuery esignCapacityRechargeRecordQuery = new EsignCapacityRechargeRecordQuery();
        esignCapacityRechargeRecordQuery.setSize(size);
        esignCapacityRechargeRecordQuery.setOffset(offset);
        esignCapacityRechargeRecordQuery.setTenantId(tenantId);

        return R.ok(esignCapacityDataService.queryEsignRechargeRecords(esignCapacityRechargeRecordQuery));
    }

    @GetMapping("/admin/queryEsinRechargeRecordsCount")
    public R queryEsinRechargeRecordsCount(@RequestParam(value = "tenantId", required = false) Long tenantId){
        EsignCapacityRechargeRecordQuery esignCapacityRechargeRecordQuery = new EsignCapacityRechargeRecordQuery();
        esignCapacityRechargeRecordQuery.setTenantId(tenantId);
        return R.ok(esignCapacityDataService.queryRecordsCount(esignCapacityRechargeRecordQuery));
    }

    @GetMapping(value = "/admin/querySignFile/{signFlowId}")
    public R querySignFile(@PathVariable("signFlowId") String signFlowId){
        return returnTripleResult(eleCabinetSignatureService.getSignatureFile(signFlowId));

    }

}
