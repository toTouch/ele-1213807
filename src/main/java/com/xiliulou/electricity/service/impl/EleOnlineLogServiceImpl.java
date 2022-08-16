package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.EleOnlineLog;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.mapper.EleOnlineLogMapper;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.EleOnlineLogService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ELeOnlineLogVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券规则表(TEleOnLineLog)表服务接口
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
@Service("eleOnlineLogService")
@Slf4j
public class EleOnlineLogServiceImpl implements EleOnlineLogService {

    @Resource
    EleOnlineLogMapper eleOnlineLogMapper;

    @Override
    public EleOnlineLog insert(EleOnlineLog eleOnlineLog) {
        this.eleOnlineLogMapper.insert(eleOnlineLog);
        return eleOnlineLog;
    }

    @Override
    public R queryOnlineLogList(Integer size, Integer offset, String type, Integer eleId) {
        List<ELeOnlineLogVO> list = eleOnlineLogMapper.queryOnlineLogList(size, offset, type, eleId);
        return R.ok(list);
    }
}
