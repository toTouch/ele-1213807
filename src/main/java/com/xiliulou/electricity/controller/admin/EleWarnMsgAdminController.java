package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;



/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class EleWarnMsgAdminController {
    /**
     * 服务对象
     */
    @Autowired
    EleWarnMsgService eleWarnMsgService;

    //列表查询
    @GetMapping(value = "/admin/eleWarnMsg/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "type", required = false) Integer type) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .offset(offset)
                .size(size)
                .electricityCabinetId(electricityCabinetId)
                .type(type)
                .build();

        return eleWarnMsgService.queryList(eleWarnMsgQuery);
    }

}