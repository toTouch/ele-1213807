package com.xiliulou.electricity.service.userinfo.overdue.impl;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.userinfo.overdue.UserInfoOverdueRemark;
import com.xiliulou.electricity.mapper.userinfo.overdue.UserInfoOverdueRemarkMapper;
import com.xiliulou.electricity.query.userinfo.overdue.OverdueRemarkReq;
import com.xiliulou.electricity.service.userinfo.overdue.UserInfoOverdueRemarkService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.tx.userinfo.overdue.UserInfoOverdueTxService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * <p>
 * Description: This class is UserInfoOverdueRemarkServiceImpl!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/6/25
 **/
@Service
public class UserInfoOverdueRemarkServiceImpl implements UserInfoOverdueRemarkService {
    
    private static final Logger log = LoggerFactory.getLogger(UserInfoOverdueRemarkServiceImpl.class);
    
    private final RedisService redisService;
    
    private final UserInfoOverdueTxService userInfoOverdueTxService;
    
    public UserInfoOverdueRemarkServiceImpl(RedisService redisService, UserInfoOverdueTxService userInfoOverdueTxService) {
        this.redisService = redisService;
        this.userInfoOverdueTxService = userInfoOverdueTxService;
    }
    
    @Override
    public Triple<Boolean,String, String> insertOrUpdate(OverdueRemarkReq request) {
        String lockKey = String.format(CacheConstant.CACHE_USERINFO_OVERDUE_REMARK_SAVE_LOCK,request.getType(),request.getUid());
        if (redisService.getExpireTime(lockKey) > 0){
            return Triple.of(false,"ELECTRICITY.0034","操作频繁");
        }
        redisService.expire(lockKey,2000L,false);
        
        try {
            
            Integer tenantId = TenantContextHolder.getTenantId();
            UserInfoOverdueRemark remark = new UserInfoOverdueRemark();
            remark.setUid(request.getUid());
            remark.setRemark(StringUtils.defaultIfBlank(request.getRemark(),""));
            remark.setType(request.getType());
            remark.setTenantId(tenantId.longValue());
            
            userInfoOverdueTxService.insertOrUpdate(remark);
            
        }finally {
            redisService.delete(lockKey);
        }
        
        return Triple.of(true,null,null);
    }
}
