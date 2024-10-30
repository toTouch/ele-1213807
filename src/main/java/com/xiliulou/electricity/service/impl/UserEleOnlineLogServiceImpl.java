package com.xiliulou.electricity.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserEleOnlineLog;
import com.xiliulou.electricity.mapper.UserEleOnlineLogMapper;
import com.xiliulou.electricity.service.UserEleOnlineLogService;
import com.xiliulou.electricity.tenant.TenantContextHolder;

@Service
public class UserEleOnlineLogServiceImpl implements UserEleOnlineLogService {

    @Autowired
    private UserEleOnlineLogMapper userEleOnlineLogMapper;

    @Override
    @Transactional
    public int insert(UserEleOnlineLog userEleOnlineLog) {
        return userEleOnlineLogMapper.insert(userEleOnlineLog);
    }

    @Override
    public String generateUserDeviceStatusValue(Integer onlineStatus, Long occurTime) {
        return onlineStatus + ":" + occurTime;
    }

    @Override
    public Pair<Integer, Long> parseUserDeviceStatusValue(String value) {
        String[] parts = value.split(":");
        return Pair.of(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
    }

    @Override
    public UserEleOnlineLog queryLastLog(Integer id) {
        return userEleOnlineLogMapper.queryLastLog(id);
    }

    @Override
    public R queryOnlineLogList(Integer size, Integer offset, String type, Integer eleId) {
        return R.ok(userEleOnlineLogMapper.queryOnlineLogList(size, offset, type, eleId,
                TenantContextHolder.getTenantId()));
    }

    @Override
    public R queryOnlineLogCount(String type, Integer eleId) {
        return R.ok(userEleOnlineLogMapper.queryOnlineLogCount(type, eleId));
    }
}
