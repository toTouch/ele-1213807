package com.xiliulou.electricity.controller.outer;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
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

}