package com.xiliulou.electricity.controller.outer;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
public class ElectricityCabinetOuterController {

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Value("${ele.apk.version:1.1.1}")
    String apkVersion;
    @Value("${ele.apk.url:https://ele.xiliulou.com/apk}")
    String apkUrl;

    /**
     * app检查版本
     *
     * @return
     */
    @GetMapping(value = "/outer/defaultVersion")
    public R getLastedAppVersion() {
        Map<String, Object> result = Maps.newHashMap();
        result.put("version", apkVersion);
        result.put("dir", apkUrl);
        return R.ok(result);
    }

    //发送命令
    @PostMapping(value = "/outer/electricityCabinet/command")
    public R sendCommandToEleForOuterV2(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        return  electricityCabinetService.sendCommandToEleForOuter(eleOuterCommandQuery);

    }

    //检查命令
    @GetMapping("/outer/electricityCabinet/open/check")
    public R checkOpenSession(@RequestParam("sessionId") String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        return electricityCabinetService.checkOpenSessionId(sessionId);
    }

}