package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * (ChannelActivityHistory)表控制层
 *
 * @author Hardy
 * @since 2023-03-23 09:24:25
 */
@RestController
public class JsonAdminChannelActivityHistoryController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private ChannelActivityHistoryService channelActivityHistoryService;
    
    @GetMapping("/admin/channelActivityHistory/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            offset = 50L;
        }
        return this
                .returnTripleResult(channelActivityHistoryService.queryList(size, offset, phone, beginTime, endTime));
    }
    
    @GetMapping("/admin/channelActivityHistory/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        return this.returnTripleResult(channelActivityHistoryService.queryCount(phone, beginTime, endTime));
    }
    
    @GetMapping("/admin/channelActivityHistory/exportExcel")
    public void queryExportExcel(@RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {
        channelActivityHistoryService.queryExportExcel(phone, beginTime, endTime, response);
    }
}
