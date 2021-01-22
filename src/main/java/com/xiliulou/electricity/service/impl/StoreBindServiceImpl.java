package com.xiliulou.electricity.service.impl;
import com.xiliulou.electricity.entity.StoreBind;
import com.xiliulou.electricity.mapper.StoreBindMapper;
import com.xiliulou.electricity.service.StoreBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (StoreBind)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("storeBindService")
@Slf4j
public class StoreBindServiceImpl implements StoreBindService {
    @Resource
    StoreBindMapper storeBindMapper;
    @Override
    public void deleteByUid(Long uid) {
        storeBindMapper.deleteByUid(uid);
    }

    @Override
    public void insert(StoreBind storeBind) {
        storeBindMapper.insert(storeBind);
    }
}