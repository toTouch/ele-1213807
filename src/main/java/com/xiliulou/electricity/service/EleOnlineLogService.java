package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.EleOnlineLog;
import com.xiliulou.electricity.query.CouponQuery;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 优惠券规则表(TEleOnLineLog)表服务接口
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
public interface EleOnlineLogService {


    EleOnlineLog insert(EleOnlineLog eleOnlineLog);

    R queryOnlineLogList(Integer size, Integer offset, String type, Integer eleId);

    R queryOnlineLogCount(String type, Integer eleId);

}
