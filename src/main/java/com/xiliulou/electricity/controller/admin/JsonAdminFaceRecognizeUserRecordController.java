package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FaceRecognizeUserRecord;
import com.xiliulou.electricity.query.FaceRecognizeUserRecordQuery;
import com.xiliulou.electricity.service.FaceRecognizeUserRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (FaceRecognizeUserRecord)表控制层
 *
 * @author zzlong
 * @since 2023-02-02 14:27:10
 */
@RestController
@Slf4j
public class JsonAdminFaceRecognizeUserRecordController {

    @Autowired
    private FaceRecognizeUserRecordService faceRecognizeUserRecordService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/faceRecognizeUserRecord/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "userName", required = false) String userName,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "startTime", required = false) Long startTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FaceRecognizeUserRecordQuery query = FaceRecognizeUserRecordQuery.builder()
                .userName(userName)
                .phone(phone)
                .size(size)
                .stareTime(startTime)
                .endTime(endTime)
                .offset(offset).build();

        return R.ok(this.faceRecognizeUserRecordService.selectByPage(query));
    }

    /**
     * 分页总记录数
     */
    @GetMapping("/admin/faceRecognizeUserRecord/queryCount")
    public R pageCount( @RequestParam(value = "userName", required = false) String userName,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "startTime", required = false) Long startTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        FaceRecognizeUserRecordQuery query = FaceRecognizeUserRecordQuery.builder()
                .userName(userName)
                .phone(phone)
                .stareTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.faceRecognizeUserRecordService.selectByPageCount(query));
    }

}
