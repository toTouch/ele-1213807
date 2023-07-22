package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.query.ElePowerListQuery;
import com.xiliulou.electricity.query.PowerMonthStatisticsQuery;
import com.xiliulou.electricity.service.ElePowerMonthRecordService;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author : eclair
 * @date : 2023/7/19 10:16
 */
@RestController
public class JsonAdminElePowerController extends BaseController {
    @Autowired
    ElePowerService elePowerService;

    @Autowired
    ElePowerMonthRecordService monthRecordService;

    @GetMapping("/admin/power/list")
    public R getList(@RequestParam("size") Integer size,
                     @RequestParam("offset") Integer offset,
                     @RequestParam(value = "eid", required = false) Long eid,
                     @RequestParam(value = "startTime") Long startTime,
                     @RequestParam(value = "endTime") Long endTime) {
        if (endTime - startTime > TimeUnit.DAYS.toMillis(31)) {
            throw new CustomBusinessException("时间跨度不可以超过31");
        }

        if (size > 50 || size < 0) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        ElePowerListQuery query = ElePowerListQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .size(size)
                .eid(eid)
                .offset(offset)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return returnPairResult(elePowerService.queryList(query));
    }

    @PostMapping("/admin/power/list/export")
    public void exportPowerListExcel(HttpServletResponse response, @RequestParam(value = "eid") Long eid,
                                     @RequestParam(value = "startTime") Long startTime,
                                     @RequestParam(value = "endTime") Long endTime) {
        if (endTime - startTime > TimeUnit.DAYS.toMillis(31)) {
            throw new CustomBusinessException("时间跨度不可以超过31");
        }

        ElePowerListQuery query = ElePowerListQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .eid(eid)
                .offset(NumberConstant.ZERO)
                .size(Integer.MAX_VALUE)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        elePowerService.exportList(query, response);
    }

    @GetMapping("/admin/power/day/list")
    public R getDayList(@RequestParam(value = "eid") Long eid,
                        @RequestParam(value = "startTime") Long startTime,
                        @RequestParam(value = "endTime") Long endTime) {

        if (endTime - startTime > TimeUnit.DAYS.toMillis(31)) {
            return R.fail("时间跨度不可以超过31");
        }

        return returnPairResult(elePowerService.queryDayList(eid, startTime, endTime, TenantContextHolder.getTenantId()));
    }

    @GetMapping("/admin/power/day/detail")
    public R getDayDetail(@RequestParam(value = "eid") Long eid,
                          @RequestParam(value = "startTime") Long startTime,
                          @RequestParam(value = "endTime") Long endTime) {

        if (endTime - startTime > TimeUnit.DAYS.toMillis(31)) {
            return R.fail("时间跨度不可以超过31");
        }

        return returnPairResult(elePowerService.queryDayDetail(eid, startTime, endTime, TenantContextHolder.getTenantId()));
    }


    @GetMapping("/admin/power/month/list")
    public R getMonthList(@RequestParam(value = "eid") Long eid,
                          @RequestParam(value = "startTime") Long startTime,
                          @RequestParam(value = "endTime") Long endTime) {

        if (endTime - startTime > TimeUnit.DAYS.toMillis(356 * 2)) {
            return R.fail("时间跨度不可以超过31");
        }

        return returnPairResult(elePowerService.queryMonthList(eid, startTime, endTime, TenantContextHolder.getTenantId()));
    }

    @GetMapping("/admin/power/month/detail")
    public R getMonthDetail(@RequestParam(value = "eid") Long eid,
                            @RequestParam(value = "startTime") Long startTime,
                            @RequestParam(value = "endTime") Long endTime) {

        if (endTime - startTime > TimeUnit.DAYS.toMillis(356 * 2)) {
            return R.fail("时间跨度不可以超过31");
        }

        return returnPairResult(elePowerService.queryMonthDetail(eid, startTime, endTime, TenantContextHolder.getTenantId()));
    }

    @GetMapping("/admin/power/month/statistics")
    public R monthStatistics(@RequestParam("date") String date,
                             @RequestParam(value = "eid", required = false) Long eid,
                             @RequestParam(value = "storeId", required = false) Long storeId,
                             @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                             @RequestParam(value = "offset") Integer offset,
                             @RequestParam(value = "size") Integer size) {
        if (size > 50 || size < 0) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        PowerMonthStatisticsQuery query = PowerMonthStatisticsQuery.builder()
                .storeId(storeId)
                .date(date)
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .eid(eid).build();
        return returnPairResult(monthRecordService.queryMonthStatistics(query));

    }

    @PostMapping("/admin/power/month/statistics")
    public void monthStatisticsExport(@RequestParam("date") String date,
                                      @RequestParam(value = "eid", required = false) Long eid,
                                      @RequestParam(value = "storeId", required = false) Long storeId,
                                      @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                      HttpServletResponse response) {

        PowerMonthStatisticsQuery query = PowerMonthStatisticsQuery.builder()
                .storeId(storeId)
                .date(date)
                .size(NumberConstant.ZERO)
                .offset(Integer.MAX_VALUE)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .eid(eid).build();
        monthRecordService.exportMonthStatistics(query,response);

    }


    @GetMapping("/admin/power/month/statistics/count")
    public R monthStatistics(@RequestParam("date") String date,
                             @RequestParam(value = "eid", required = false) Long eid,
                             @RequestParam(value = "storeId", required = false) Long storeId,
                             @RequestParam(value = "franchiseeId", required = false) Long franchiseeId
    ) {

        PowerMonthStatisticsQuery query = PowerMonthStatisticsQuery.builder()
                .storeId(storeId)
                .date(date)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .eid(eid).build();
        return returnPairResult(monthRecordService.queryMonthStatisticsCount(query));

    }
}
