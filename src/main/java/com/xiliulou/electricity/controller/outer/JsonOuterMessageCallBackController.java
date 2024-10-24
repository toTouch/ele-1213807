package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.warn.WarnNoteCallBack;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/6/21 9:37
 * @desc 消息中新回调类
 */

@RestController
@Slf4j
public class JsonOuterMessageCallBackController extends BaseController {
    @Resource
    private EleHardwareFailureWarnMsgService eleHardwareFailureWarnMsgService;
    /**
     * 告警短息回调通知
     *
     * @return
     */
    @PostMapping("/outer/message/warn/note/notice")
    public R warnNoteNotice(@RequestBody WarnNoteCallBack warnNoteCallBack) {
        return returnTripleResult(eleHardwareFailureWarnMsgService.warnNoteNotice(warnNoteCallBack));
    }
}
