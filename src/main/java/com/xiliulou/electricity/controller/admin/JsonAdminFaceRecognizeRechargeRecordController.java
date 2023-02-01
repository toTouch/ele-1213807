package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FaceRecognizeRechargeRecord;
import com.xiliulou.electricity.query.FaceRecognizeRechargeRecordQuery;
import com.xiliulou.electricity.service.FaceRecognizeRechargeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (FaceRecognizeRechargeRecord)表控制层
 *
 * @author zzlong
 * @since 2023-01-31 17:18:00
 */
@RestController
@Slf4j
public class JsonAdminFaceRecognizeRechargeRecordController {

    @Autowired
    private FaceRecognizeRechargeRecordService faceRecognizeRechargeRecordService;

    /**
     * 分页
     */
    @GetMapping("/admin/faceRecognizeRechargeRecord/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam("tenantId") Integer tenantId,
                  @RequestParam(value = "startTime", required = false) Long startTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FaceRecognizeRechargeRecordQuery query = FaceRecognizeRechargeRecordQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(tenantId)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.faceRecognizeRechargeRecordService.selectByPage(query));
    }

    /**
     * 分页总记录数
     */
    @GetMapping("/admin/faceRecognizeRechargeRecord/count")
    public R pageCount(@RequestParam("tenantId") Integer tenantId,
                       @RequestParam(value = "startTime", required = false) Long startTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        FaceRecognizeRechargeRecordQuery query = FaceRecognizeRechargeRecordQuery.builder()
                .tenantId(tenantId)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.faceRecognizeRechargeRecordService.selectByPageCount(query));
    }

}
