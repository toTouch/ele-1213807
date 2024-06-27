package com.xiliulou.electricity.service.userinfo.overdue.impl;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.userinfo.overdue.UserInfoOverdueRemark;
import com.xiliulou.electricity.mapper.userinfo.overdue.UserInfoOverdueRemarkMapper;
import com.xiliulou.electricity.query.userinfo.overdue.OverdueRemarkReq;
import com.xiliulou.electricity.service.userinfo.overdue.UserInfoOverdueRemarkService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.tx.userinfo.overdue.UserInfoOverdueTxService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

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
@Slf4j
@Service
public class UserInfoOverdueRemarkServiceImpl implements UserInfoOverdueRemarkService {
    
    private final RedisService redisService;
    
    private final UserInfoOverdueTxService userInfoOverdueTxService;
    
    private final UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper;
    
    public UserInfoOverdueRemarkServiceImpl(RedisService redisService, UserInfoOverdueTxService userInfoOverdueTxService, UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper) {
        this.redisService = redisService;
        this.userInfoOverdueTxService = userInfoOverdueTxService;
        this.userInfoOverdueRemarkMapper = userInfoOverdueRemarkMapper;
    }
    
    @Override
    public Triple<Boolean, String, String> insertOrUpdate(OverdueRemarkReq request) {
        String lockKey = String.format(CacheConstant.CACHE_USERINFO_OVERDUE_REMARK_SAVE_LOCK, request.getType(), request.getUid());
        if (!redisService.setNx(lockKey, String.valueOf(request.getUid()), 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0033", "操作频繁");
        }
        
        try {
            Integer tenantId = TenantContextHolder.getTenantId();
            UserInfoOverdueRemark remark = new UserInfoOverdueRemark();
            remark.setUid(request.getUid());
            remark.setRemark(StringUtils.defaultIfBlank(request.getRemark(), ""));
            remark.setType(request.getType());
            remark.setTenantId(tenantId.longValue());
            Long id = userInfoOverdueRemarkMapper.queryIdByUidAndType(remark.getUid(), remark.getType(), remark.getTenantId());
            userInfoOverdueTxService.insertOrUpdate(remark, id);
        } finally {
            redisService.delete(lockKey);
        }
        
        return Triple.of(true, null, null);
    }
    
}
