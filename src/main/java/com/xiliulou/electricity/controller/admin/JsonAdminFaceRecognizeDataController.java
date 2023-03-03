package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FaceRecognizeData;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FaceRecognizeDataQuery;
import com.xiliulou.electricity.service.FaceRecognizeDataService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (FaceRecognizeData)表控制层
 *
 * @author zzlong
 * @since 2023-01-31 15:38:29
 */
@RestController
@Slf4j
public class JsonAdminFaceRecognizeDataController extends BaseController {

    @Autowired
    private FaceRecognizeDataService faceRecognizeDataService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/faceRecognizeData/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "tenantName", required = false) String tenantName) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FaceRecognizeDataQuery query = FaceRecognizeDataQuery.builder()
                .size(size)
                .offset(offset)
                .tenantName(tenantName).build();

        return R.ok(this.faceRecognizeDataService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/faceRecognizeData/queryCount")
    public R pageCount(@RequestParam(value = "tenantName", required = false) String tenantName) {

        FaceRecognizeDataQuery query = FaceRecognizeDataQuery.builder()
                .tenantName(tenantName).build();
        return R.ok(this.faceRecognizeDataService.selectByPageCount(query));
    }

    /**
     * 新增
     */
    @PostMapping("/admin/faceRecognizeData")
    public R save(@RequestBody @Validated(CreateGroup.class) FaceRecognizeDataQuery faceRecognizeDataQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! add faceRecognizeData no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        this.faceRecognizeDataService.insert(faceRecognizeDataQuery);
        return R.ok();
    }


    /**
     * 修改
     */
    @PutMapping("/admin/faceRecognizeData")
    public R update(@RequestBody @Validated(UpdateGroup.class) FaceRecognizeDataQuery faceRecognizeDataQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! update faceRecognizeData no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        this.faceRecognizeDataService.update(faceRecognizeDataQuery);
        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/faceRecognizeData/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! delete faceRecognizeData no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        this.faceRecognizeDataService.deleteById(id);
        return R.ok();
    }

    /**
     * 人脸核身次数充值
     */
    @PutMapping("/admin/faceRecognizeData/recharge")
    public R recharge(@RequestBody @Validated(UpdateGroup.class) FaceRecognizeDataQuery faceRecognizeDataQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! update faceRecognizeData no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(this.faceRecognizeDataService.recharge(faceRecognizeDataQuery));
    }

}
