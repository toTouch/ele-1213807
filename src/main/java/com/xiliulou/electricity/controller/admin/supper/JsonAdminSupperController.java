package com.xiliulou.electricity.controller.admin.supper;

import cn.hutool.core.lang.Pair;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.supper.DelBatteryReq;
import com.xiliulou.electricity.query.supper.UserGrantSourceReq;
import com.xiliulou.electricity.service.supper.AdminSupperService;
import com.xiliulou.electricity.vo.supper.DelBatteryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Slf4j
@RestController
@RequestMapping("/admin/inner/supper")
public class JsonAdminSupperController {
    
    @Resource
    private AdminSupperService adminSupperService;
    
    /**
     * 根据电池SN删除电池
     * @param delBatteryReq 删除电池请求体
     * @return R<DelBatteryVo>
     */
    @PostMapping("/delBatterys")
    public R<DelBatteryVo> delBatterys(@RequestBody DelBatteryReq delBatteryReq) {
        Pair<List<String>, List<String>> pair = adminSupperService.delBatteryBySnList(delBatteryReq.getTenantId(), delBatteryReq.getBatterySnList(), delBatteryReq.getViolentDel());
        DelBatteryVo delBatteryVo = new DelBatteryVo();
        delBatteryVo.setSuccessSnList(pair.getKey());
        delBatteryVo.setFailedSnList(pair.getValue());
        return R.ok(delBatteryVo);
    }
    
    @PostMapping("/grantPermission")
    public R<?> grantPermission(@RequestBody UserGrantSourceReq userGrantSourceReq) {
        adminSupperService.grantPermission(userGrantSourceReq);
        return R.ok();
    }
}
