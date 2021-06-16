package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 15:25
 **/
@RestController
@Slf4j
public class JsonAdminElectricityMemberCardController {

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;

    /**
     * 新增
     *
     * @return
     */
    @PostMapping("admin/electricityMemberCard")
    public R saveElectricityMemberCard(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
        return electricityMemberCardService.saveElectricityMemberCard(electricityMemberCard);
    }

    /**
     * 修改
     *
     * @return
     */
    @PutMapping("admin/electricityMemberCard")
    public R updateElectricityMemberCard(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
        if (Objects.isNull(electricityMemberCard)) {
            return R.failMsg("请求参数不能为空!");
        }
        return electricityMemberCardService.updateElectricityMemberCard(electricityMemberCard);
    }

    /**
     * 删除
     *
     * @return
     */
    @DeleteMapping("admin/electricityMemberCard/{id}")
    public R deleteElectricityMemberCard(@PathVariable(value = "id") Integer id) {
        return electricityMemberCardService.deleteElectricityMemberCard(id);
    }

    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/page")
    public R getElectricityMemberCardPage(@RequestParam(value = "offset") Long offset,
                                          @RequestParam(value = "size") Long size,
                                          @RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false) Integer status) {


        return electricityMemberCardService.getElectricityMemberCardPage(offset, size, status, type);
    }

}
