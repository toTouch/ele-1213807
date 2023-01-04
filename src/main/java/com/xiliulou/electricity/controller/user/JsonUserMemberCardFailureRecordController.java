package com.xiliulou.electricity.controller.user;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAttr;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.MemberCardFailureRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.H
 * @create: 2022-12-19 17:20
 **/
@RestController
@Slf4j
public class JsonUserMemberCardFailureRecordController {

    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;

    @GetMapping("user/queryFailureMemberCard")
    public R queryFailureMemberCard(@RequestParam("size") Integer size,
                                    @RequestParam("offset") Integer offset) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }

        if (size <= 0 || size > 50) {
            size = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        return R.ok(memberCardFailureRecordService.queryFailureMemberCard(uid, offset, size));
    }


}
