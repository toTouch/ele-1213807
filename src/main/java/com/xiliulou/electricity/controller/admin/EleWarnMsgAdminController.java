package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
                       @RequestParam(value = "type", required = false) Integer type,
                       @RequestParam(value = "status", required = false) Integer status) {
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
                .status(status)
                .build();

        return eleWarnMsgService.queryList(eleWarnMsgQuery);
    }

    //解锁电柜
    @PostMapping(value = "/admin/eleWarnMsg/haveRead")
    public R haveRead(@RequestParam("id") Long id) {
        EleWarnMsg eleWarnMsg = eleWarnMsgService.queryByIdFromCache(id);
        if (Objects.isNull(eleWarnMsg)||Objects.equals(eleWarnMsg.getStatus(),EleWarnMsg.STATUS_HAVE_READ)) {
            return R.fail("ELECTRICITY.0064", "未找到未读消息");
        }
        EleWarnMsg updateEleWarnMsg=new EleWarnMsg();
        updateEleWarnMsg.setId(eleWarnMsg.getId());
        updateEleWarnMsg.setStatus(EleWarnMsg.STATUS_HAVE_READ);
        updateEleWarnMsg.setUpdateTime(System.currentTimeMillis());
        eleWarnMsgService.update(updateEleWarnMsg);
        return R.ok();
    }

}