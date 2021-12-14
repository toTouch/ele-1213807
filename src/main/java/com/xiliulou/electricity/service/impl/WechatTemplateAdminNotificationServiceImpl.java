package com.xiliulou.electricity.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.WechatTemplateAdminNotification;
import com.xiliulou.electricity.mapper.WechatTemplateAdminNotificationMapper;
import com.xiliulou.electricity.query.WechatTemplateAdminNotificationQuery;
import com.xiliulou.electricity.service.WechatTemplateAdminNotificationService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.WechatTemplateAdminNotificationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
/**
 * (WechatTemplateAdminNotification)表服务实现类
 *
 * @author Hardy
 * @since 2021-11-25 16:50:59
 */
@Service("wechatTemplateAdminNotificationService")
@Slf4j
public class WechatTemplateAdminNotificationServiceImpl implements WechatTemplateAdminNotificationService {
    @Resource
    private WechatTemplateAdminNotificationMapper wechatTemplateAdminNotificationMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public WechatTemplateAdminNotification queryByIdFromDB(Long id) {
        return this.wechatTemplateAdminNotificationMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  WechatTemplateAdminNotification queryByIdFromCache(Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();

        WechatTemplateAdminNotification wechatTemplateAdminNotification = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ADMIN_NOTIFICATION+tenantId, WechatTemplateAdminNotification.class );
        if(Objects.nonNull(wechatTemplateAdminNotification)){
            return wechatTemplateAdminNotification;
        }

        wechatTemplateAdminNotification = this.queryByIdFromDB(id);
        if(Objects.nonNull(wechatTemplateAdminNotification)){
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ADMIN_NOTIFICATION + wechatTemplateAdminNotification.getTenantId(), wechatTemplateAdminNotification);
            return wechatTemplateAdminNotification;
        }

        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<WechatTemplateAdminNotification> queryAllByLimit(int offset, int limit) {
        return this.wechatTemplateAdminNotificationMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatTemplateAdminNotification insert(WechatTemplateAdminNotification wechatTemplateAdminNotification) {
        this.wechatTemplateAdminNotificationMapper.insertOne(wechatTemplateAdminNotification);
        return wechatTemplateAdminNotification;
    }

    /**
     * 修改数据
     *
     * @param wechatTemplateAdminNotification 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(WechatTemplateAdminNotification wechatTemplateAdminNotification) {
        return this.wechatTemplateAdminNotificationMapper.update(wechatTemplateAdminNotification);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.wechatTemplateAdminNotificationMapper.deleteById(id) > 0;
    }

    @Override
    public R saveOne(WechatTemplateAdminNotificationQuery wechatTemplateAdminNotificationQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();

        WechatTemplateAdminNotification queryByTenantId = wechatTemplateAdminNotificationMapper.queryByTenant(tenantId);
        if(Objects.nonNull(queryByTenantId)){
            return R.fail("不可重复添加");
        }
        List<String> list = wechatTemplateAdminNotificationQuery.getOpenIds();
        if(list == null){
            return R.fail("请配置openid");
        }
        if(list.size() > 4){
            return R.fail("最多只能添加四位管理员");
        }

        WechatTemplateAdminNotification wechatTemplateAdminNotification = new WechatTemplateAdminNotification();
        wechatTemplateAdminNotification.setOpenIds(JSON.toJSONString(list));
        wechatTemplateAdminNotification.setTenantId(tenantId);
        wechatTemplateAdminNotification.setCreateTime(System.currentTimeMillis());
        wechatTemplateAdminNotification.setUpdateTime(System.currentTimeMillis());
        wechatTemplateAdminNotification.setDelFlag(WechatTemplateAdminNotification.DEL_NORMAL);
        this.insert(wechatTemplateAdminNotification);

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ADMIN_NOTIFICATION + tenantId, wechatTemplateAdminNotification);
        return R.ok();
    }

    @Override
    public R updateOne(WechatTemplateAdminNotificationQuery wechatTemplateAdminNotificationQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();
        WechatTemplateAdminNotification queryByIdFromCache = queryByIdFromCache(wechatTemplateAdminNotificationQuery.getId());
        if(Objects.isNull(queryByIdFromCache)){
            return R.fail("没有查询到相关信息，请先添加");
        }

        List<String> list = wechatTemplateAdminNotificationQuery.getOpenIds();
        if(list.size() > 4){
            return R.fail("最多只能添加四位管理员");
        }

        WechatTemplateAdminNotification wechatTemplateAdminNotification = new WechatTemplateAdminNotification();
        wechatTemplateAdminNotification.setId(wechatTemplateAdminNotificationQuery.getId());
        wechatTemplateAdminNotification.setOpenIds(JSON.toJSONString(list));
        wechatTemplateAdminNotification.setUpdateTime(System.currentTimeMillis());
        this.update(wechatTemplateAdminNotification);

        redisService.delete(ElectricityCabinetConstant.CACHE_ADMIN_NOTIFICATION + tenantId);
        return R.ok();
    }

    @Override
    public WechatTemplateAdminNotification queryByTenant(Integer tenantId) {
        WechatTemplateAdminNotification wechatTemplateAdminNotification = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ADMIN_NOTIFICATION+tenantId, WechatTemplateAdminNotification.class );
        if(Objects.nonNull(wechatTemplateAdminNotification)){
            return wechatTemplateAdminNotification;
        }

        wechatTemplateAdminNotification = wechatTemplateAdminNotificationMapper.queryByTenant(tenantId);
        if(Objects.nonNull(wechatTemplateAdminNotification)){
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ADMIN_NOTIFICATION + tenantId, wechatTemplateAdminNotification);
            return wechatTemplateAdminNotification;
        }

        return null;
    }

    @Override
    public R queryList(){
        Integer tenantId = TenantContextHolder.getTenantId();
        WechatTemplateAdminNotification wechatTemplateAdminNotification = this.queryByTenant(tenantId);
        WechatTemplateAdminNotificationVo vo = new WechatTemplateAdminNotificationVo();
        if(Objects.nonNull(wechatTemplateAdminNotification)){
            vo.setId(wechatTemplateAdminNotification.getId());
            vo.setOpenIds(JSON.parseArray(wechatTemplateAdminNotification.getOpenIds(), String.class));
        }
        return R.ok(vo);
    }
}
