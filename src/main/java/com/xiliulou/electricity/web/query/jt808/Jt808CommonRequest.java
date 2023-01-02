package com.xiliulou.electricity.web.query.jt808;

import com.xiliulou.electricity.constant.Jt808Constant;
import com.xiliulou.electricity.utils.SignUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : eclair
 * @date : 2022/12/29 09:38
 */
@Slf4j
@Data
public class Jt808CommonRequest {
    private String version;
    private String requestId;
    private Long requestTime;
    private String appId;
    private String sign;
    
    public Jt808CommonRequest( String requestId) {
        this.version = "1.0.0";
        this.requestId = requestId;
        this.requestTime = System.currentTimeMillis();
        this.appId = Jt808Constant.APP_ID;
        try {
            this.sign = SignUtils.getSignature(appId,requestTime,requestId,version,Jt808Constant.APP_SECRET);
        } catch (Exception e) {
            log.error("sign error",e);
        }
    }
}
