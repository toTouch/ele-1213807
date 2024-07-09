package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.mapper.AlipayAppConfigMapper;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 支付宝小程序配置(AlipayAppConfig)表服务实现类
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
@Service("alipayAppConfigService")
@Slf4j
public class AlipayAppConfigServiceImpl implements AlipayAppConfigService {
    
    @Resource
    private AlipayAppConfigMapper alipayAppConfigMapper;
    
    @Slave
    @Override
    public AlipayAppConfig queryByAppId(String appId) {
        return this.alipayAppConfigMapper.selectByAppId(appId);
    }
    
    @Slave
    @Override
    public AlipayAppConfig queryByTenantId(Integer tenantId) {
        return this.alipayAppConfigMapper.selectByTenantId(tenantId);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AlipayAppConfig queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param alipayAppConfig 实例对象
     * @return 实例对象
     */
    @Override
    public Integer update(AlipayAppConfig alipayAppConfig) {
        return this.alipayAppConfigMapper.update(alipayAppConfig);
        
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Integer deleteById(Long id) {
        return this.alipayAppConfigMapper.deleteById(id);
    }
}
