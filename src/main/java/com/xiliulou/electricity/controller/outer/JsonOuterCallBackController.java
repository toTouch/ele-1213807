package com.xiliulou.electricity.controller.outer;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.pay.weixinv3.query.WechatV3OrderCallBackQuery;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundOrderCallBackQuery;
import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import com.xiliulou.pay.weixinv3.service.WechatV3PostProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-04 10:53
 **/
@RestController
@Slf4j
public class JsonOuterCallBackController {
    @Autowired
    WechatV3PostProcessHandler wechatV3PostProcessHandler;
    @Qualifier("newRedisTemplate")
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 微信支付通知
     *
     * @return
     */
    @PostMapping("/outer/wechat/pay/notified/{tenantId}")
    public WechatV3CallBackResult payNotified(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3OrderCallBackQuery wechatV3OrderCallBackQuery) {
        wechatV3OrderCallBackQuery.setTenantId(tenantId);
        wechatV3PostProcessHandler.postProcessAfterWechatPay(wechatV3OrderCallBackQuery);
        return WechatV3CallBackResult.success();
    }

    /**
     * 微信退款通知
     *
     * @return
     */
    @PostMapping("/outer/wechat/refund/notified/{tenantId}")
    public WechatV3CallBackResult refundNotified(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        wechatV3RefundOrderCallBackQuery.setTenantId(tenantId);
        wechatV3PostProcessHandler.postProcessAfterWechatRefund(wechatV3RefundOrderCallBackQuery);
        return WechatV3CallBackResult.success();
    }

    //测试redis
    @GetMapping("/outer/redis/test")
    public R redisTest(){
        String key = "redis_test";
        RedisScript redisScript = RedisScript.of("local times = redis.call('incr',KEYS[1]) if times == 1 then redis.call('expire',KEYS[1],ARGV[1]) end if times > tonumber(ARGV[2]) then return 0 end return 1",
                String.class);

        List<String> list = new ArrayList<>(1);
        list.add(key);
        Object result = redisTemplate.execute(redisScript, list, 60, 10);

        if (ObjectUtil.equal(1L, result)) {
            return R.ok();
        }
        return R.fail("失败");
    }
}
