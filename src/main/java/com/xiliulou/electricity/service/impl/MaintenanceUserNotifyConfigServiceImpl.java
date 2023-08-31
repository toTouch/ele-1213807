package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.MaintenanceUserNotifyConfigMapper;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.query.MaintenanceUserNotifyConfigQuery;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.MaintenanceUserNotifyConfigVo;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * (MaintenanceUserNotifyConfig)表服务实现类
 *
 * @author makejava
 * @since 2022-04-12 09:07:48
 */
@Service("maintenanceUserNotifyConfigService")
@Slf4j
public class MaintenanceUserNotifyConfigServiceImpl implements MaintenanceUserNotifyConfigService {
    @Resource
    private MaintenanceUserNotifyConfigMapper maintenanceUserNotifyConfigMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    RocketMqService rocketMqService;
    @Autowired
    WechatConfig wechatConfig;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public MaintenanceUserNotifyConfig queryByTenantIdFromDB(Integer id) {
        return this.maintenanceUserNotifyConfigMapper.queryByTenantId(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public MaintenanceUserNotifyConfig queryByTenantIdFromCache(Integer id) {
        MaintenanceUserNotifyConfig cacheConfig = redisService.getWithHash(CacheConstant.CACHE_TENANT_MAINTENANCE_USER_CONFIG + id, MaintenanceUserNotifyConfig.class);
        if (Objects.nonNull(cacheConfig)) {
            return cacheConfig;
        }

        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromDB(id);
        if (Objects.isNull(maintenanceUserNotifyConfig)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_TENANT_MAINTENANCE_USER_CONFIG + id, maintenanceUserNotifyConfig);
        return maintenanceUserNotifyConfig;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<MaintenanceUserNotifyConfig> queryAllByLimit(int offset, int limit) {
        return this.maintenanceUserNotifyConfigMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param maintenanceUserNotifyConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceUserNotifyConfig insert(MaintenanceUserNotifyConfig maintenanceUserNotifyConfig) {
        this.maintenanceUserNotifyConfigMapper.insertOne(maintenanceUserNotifyConfig);
        return maintenanceUserNotifyConfig;
    }

    /**
     * 修改数据
     *
     * @param maintenanceUserNotifyConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(MaintenanceUserNotifyConfig maintenanceUserNotifyConfig) {
        return DbUtils.dbOperateSuccessThen(this.maintenanceUserNotifyConfigMapper.update(maintenanceUserNotifyConfig), () -> {
            redisService.delete(CacheConstant.CACHE_TENANT_MAINTENANCE_USER_CONFIG + maintenanceUserNotifyConfig.getTenantId());
            return 1;
        });

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        return this.maintenanceUserNotifyConfigMapper.deleteById(id) > 0;
    }

    @Override
    public Pair<Boolean, Object> queryConfigInfo() {
        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(TenantContextHolder.getTenantId());

        if (Objects.isNull(maintenanceUserNotifyConfig)) {
            return Pair.of(true, null);
        }

        MaintenanceUserNotifyConfigVo resultVo = MaintenanceUserNotifyConfigVo.builder()
                .permissions(maintenanceUserNotifyConfig.getPermissions())
                .phones(JsonUtil.fromJsonArray(maintenanceUserNotifyConfig.getPhones(), String.class))
                .qrUrl(wechatConfig.getMaintenanceQr())
                .build();
        return Pair.of(true, resultVo);
    }

    @Override
    public Pair<Boolean, Object> saveConfig(MaintenanceUserNotifyConfigQuery query) {
        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.nonNull(maintenanceUserNotifyConfig)) {
            return Pair.of(false, "已存在配置，无法重复创建");
        }

        MaintenanceUserNotifyConfig build = MaintenanceUserNotifyConfig.builder()
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .permissions(query.getPermission())
                .phones(query.getPhones())
                .build();
        insert(build);
        return Pair.of(true, null);
    }

    @Override
    public Pair<Boolean, Object> updateConfig(MaintenanceUserNotifyConfigQuery query) {
        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(maintenanceUserNotifyConfig)) {
            return Pair.of(false, "操作失败");
        }

        MaintenanceUserNotifyConfig updateConfig = MaintenanceUserNotifyConfig.builder()
                .updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .permissions(query.getPermission())
                .phones(query.getPhones())
                .build();
        update(updateConfig);
        return Pair.of(true, null);
    }

    @Override
    public void sendDeviceNotifyMq(ElectricityCabinet electricityCabinet, String status, String time) {
        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(maintenanceUserNotifyConfig) || StrUtil.isEmpty(maintenanceUserNotifyConfig.getPhones())) {
            return;
        }

        if ((maintenanceUserNotifyConfig.getPermissions() & MaintenanceUserNotifyConfig.P_DEVICE) != MaintenanceUserNotifyConfig.P_DEVICE) {
            return;
        }

        List<String> phones = JsonUtil.fromJsonArray(maintenanceUserNotifyConfig.getPhones(), String.class);

        phones.forEach(p -> {
            MqNotifyCommon<MqDeviceNotify> query = new MqNotifyCommon<>();
            query.setPhone(p);
            query.setTime(System.currentTimeMillis());
            query.setType(MaintenanceUserNotifyConfig.P_DEVICE);

            MqDeviceNotify mqDeviceNotify = new MqDeviceNotify();
            mqDeviceNotify.setProductKey(electricityCabinet.getProductKey());
            mqDeviceNotify.setDeviceSn(electricityCabinet.getDeviceName());
            mqDeviceNotify.setOccurTime(time);
            mqDeviceNotify.setStatus(status);
            mqDeviceNotify.setProjectName(MqNotifyCommon.PROJECT_NAME);
            mqDeviceNotify.setDeviceName(electricityCabinet.getName());
            query.setData(mqDeviceNotify);

            Pair<Boolean, String> result = rocketMqService.sendSyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(query), "", "", status.equals(ElectricityCabinet.IOT_STATUS_OFFLINE) ? 4 : 0);
            if (!result.getLeft()) {
                log.error("SEND MQ ERROR! d={} reason={}", electricityCabinet.getDeviceName(), result.getRight());
            }


        });


    }

    @Override
    public void sendCellLockMsg(String sessionId, ElectricityCabinet electricityCabinet, Integer cellNo, String occurTime) {
        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(maintenanceUserNotifyConfig) || StrUtil.isEmpty(maintenanceUserNotifyConfig.getPhones())) {
            return;
        }

        if ((maintenanceUserNotifyConfig.getPermissions() & MaintenanceUserNotifyConfig.P_HARDWARE_INFO) != MaintenanceUserNotifyConfig.P_HARDWARE_INFO) {
            return;
        }

        List<String> phones = JsonUtil.fromJsonArray(maintenanceUserNotifyConfig.getPhones(), String.class);
        phones.forEach(p -> {
            MqNotifyCommon<MqHardwareNotify> query = new MqNotifyCommon<>();
            query.setPhone(p);
            query.setTime(System.currentTimeMillis());
            query.setType(MaintenanceUserNotifyConfig.P_HARDWARE_INFO);

            MqHardwareNotify mqHardwareNotify = new MqHardwareNotify();
            mqHardwareNotify.setDeviceName(electricityCabinet.getName());
            mqHardwareNotify.setOccurTime(occurTime);
            mqHardwareNotify.setErrMsg(String.format("%s号仓门发生异常，已被锁定！", cellNo));
            mqHardwareNotify.setProjectTitle(MqHardwareNotify.LOCK_CELL_PROJECT_TITLE);
            query.setData(mqHardwareNotify);

            rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(query), String.valueOf(MqNotifyCommon.TYPE_HARDWARE_INFO), sessionId, 0);

        });
    }

    @Override
    public void sendUserUploadExceptionMsg(MaintenanceRecord maintenanceRecord, ElectricityCabinet electricityCabinet) {

        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(maintenanceUserNotifyConfig) || StrUtil.isEmpty(maintenanceUserNotifyConfig.getPhones())) {
            return;
        }

        if ((maintenanceUserNotifyConfig.getPermissions() & MaintenanceUserNotifyConfig.TYPE_USER_UPLOAD_EXCEPTION) != MaintenanceUserNotifyConfig.TYPE_USER_UPLOAD_EXCEPTION) {
            return;
        }

        List<String> phones = JsonUtil.fromJsonArray(maintenanceUserNotifyConfig.getPhones(), String.class);

        phones.forEach(p -> {

            MqNotifyCommon<MqHardwareNotify> query = new MqNotifyCommon<>();
            query.setPhone(p);
            query.setTime(System.currentTimeMillis());
            query.setType(MaintenanceUserNotifyConfig.TYPE_USER_UPLOAD);

            MqHardwareNotify mqHardwareNotify = new MqHardwareNotify();
            mqHardwareNotify.setDeviceName(electricityCabinet.getName());
            mqHardwareNotify.setType(maintenanceRecord.getType());
            mqHardwareNotify.setOccurTime(DateUtils.parseTimeToStringDate(maintenanceRecord.getCreateTime()));
            mqHardwareNotify.setErrMsg(maintenanceRecord.getRemark());
            mqHardwareNotify.setProjectTitle(MqHardwareNotify.USER_UPLOAD_EXCEPTION);
            query.setData(mqHardwareNotify);

            Pair<Boolean, String> result = rocketMqService.sendSyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(query), "", "", 3);
            if (!result.getLeft()) {
                log.error("SEND MQ ERROR! d={} reason={}", electricityCabinet.getDeviceName(), result.getRight());
            }
        });

    }

    @Override
    public Pair<Boolean, Object> testSendMsg() {
        Integer tenantId = TenantContextHolder.getTenantId();
        MaintenanceUserNotifyConfig maintenanceUserNotifyConfig = queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(maintenanceUserNotifyConfig) || StrUtil.isEmpty(maintenanceUserNotifyConfig.getPhones())) {
            return Pair.of(false, "请先配置手机号");
        }

        if (!redisService.setNx(CacheConstant.CACHE_TENANT_MAINTENANCE_USER_CONFIG_TEST + tenantId, "ok", TimeUnit.MINUTES.toMillis(5), false)) {
            return Pair.of(false, "5分钟之内只能测试一次");
        }

        List<String> phones = JsonUtil.fromJsonArray(maintenanceUserNotifyConfig.getPhones(), String.class);

        phones.forEach(p -> {
            MqNotifyCommon<MqDeviceNotify> query = new MqNotifyCommon<>();
            query.setPhone(p);
            query.setTime(System.currentTimeMillis());
            query.setType(MaintenanceUserNotifyConfig.P_DEVICE);

            MqDeviceNotify mqDeviceNotify = new MqDeviceNotify();
            mqDeviceNotify.setProductKey("test");
            mqDeviceNotify.setDeviceSn("test");
            mqDeviceNotify.setOccurTime("test");
            mqDeviceNotify.setStatus("test");
            mqDeviceNotify.setProjectName(MqNotifyCommon.PROJECT_NAME);
            mqDeviceNotify.setDeviceName("test");
            query.setData(mqDeviceNotify);

            Pair<Boolean, String> result = rocketMqService.sendSyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(query), "", "", 0);
            if (!result.getLeft()) {
                log.error("SEND MQ ERROR! d={} reason={}", "test", result.getRight());
            }
        });
        return Pair.of(true, null);
    }
}
