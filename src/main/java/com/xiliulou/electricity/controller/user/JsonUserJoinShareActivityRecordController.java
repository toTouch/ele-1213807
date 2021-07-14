package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@RequestMapping("joinShareActivityRecord")
public class JsonUserJoinShareActivityRecordController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareActivityRecordService joinShareActivityRecordService;




}
