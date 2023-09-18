package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FaceRecognizeUserRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FaceRecognizeUserRecordQuery;
import com.xiliulou.electricity.service.FaceRecognizeUserRecordService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/faceRecognizeUserRecord/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "name", required = false) String name,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "startTime", required = false) Long startTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        FaceRecognizeUserRecordQuery query = FaceRecognizeUserRecordQuery.builder()
                .userName(name)
                .phone(phone)
                .uid(uid)
                .size(size)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .offset(offset).build();

        return R.ok(this.faceRecognizeUserRecordService.selectByPage(query));
    }

    /**
     * 分页总记录数
     */
    @GetMapping("/admin/faceRecognizeUserRecord/queryCount")
    public R pageCount( @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "startTime", required = false) Long startTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        FaceRecognizeUserRecordQuery query = FaceRecognizeUserRecordQuery.builder()
                .userName(name)
                .phone(phone)
                .uid(uid)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(this.faceRecognizeUserRecordService.selectByPageCount(query));
    }

}
