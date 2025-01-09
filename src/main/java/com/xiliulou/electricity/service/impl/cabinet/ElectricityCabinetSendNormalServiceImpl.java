package com.xiliulou.electricity.service.impl.cabinet;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CabinetBoxConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.dto.cabinet.ElectricityCabinetSendNormalDTO;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.cabinet.ElectricityCabinetSendNormalService;
import com.xiliulou.electricity.task.cabinet.ElectricityCabinetSendNormalTask;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElectricityCabinetSendNormalServiceImpl implements ElectricityCabinetSendNormalService {
    @Resource
    private TenantService tenantService;

    @Resource
    private ElectricityCabinetService electricityCabinetService;

    @Resource
    private RocketMqService rocketMqService;

    @Resource
    private RedisService redisService;

    @Override
    public void sendNormalCommand(ElectricityCabinetSendNormalTask.SendNormalTaskParam taskParam) {
        Integer startId = 0;
        Integer size = 200;
        String operatorId = UUID.randomUUID().toString().replaceAll("-", "");

        // 指定租户下发
        if (ObjectUtils.isNotEmpty(taskParam.getTenantIds())) {
            doSendNormalCommand(taskParam.getTenantIds(), operatorId, taskParam);
            return;
        }

        // 指定柜机下发
        if (ObjectUtils.isNotEmpty(taskParam.getCabinetIds())) {
            List<List<Integer>> partition = ListUtils.partition(taskParam.getCabinetIds(), size);

            partition.stream().forEach(cabinetIdList -> {
                ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().offset(0L).size(200L).idList(taskParam.getCabinetIds()).build();
                R r = electricityCabinetService.queryAllElectricityCabinet(electricityCabinetQuery);
                if (ObjectUtils.isEmpty(r) || ObjectUtils.isEmpty(r.getData())) {
                    return;
                }

                doSendNormalCommandByCabinetList((List<ElectricityCabinetVO>) r.getData(), operatorId);
            });

            return;
        }

        // 全量处理
        while (true) {
            List<Integer> tenantIdList = tenantService.queryIdListByStartId(startId, size);
            if (ObjectUtils.isEmpty(tenantIdList)) {
                return;
            }

            startId = tenantIdList.get(tenantIdList.size() - 1);

            // 过滤出
            if (ObjectUtils.isNotEmpty(taskParam.getTenantIds())) {
                tenantIdList = tenantIdList.stream().filter(id -> taskParam.getTenantIds().contains(id)).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(tenantIdList)) {
                    continue;
                }
            }

            doSendNormalCommand(tenantIdList, operatorId, taskParam);
        }

    }

    private void doSendNormalCommand(List<Integer> tenantIdList, String operatorId, ElectricityCabinetSendNormalTask.SendNormalTaskParam taskParam) {
        if (ObjectUtils.isEmpty(tenantIdList)) {
            return;
        }

        tenantIdList.stream().forEach(tenantId -> {
            Long offset = 0L;
            Integer size = 200;

            ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().tenantId(tenantId).size(Long.valueOf(size)).build();

            // 查询租户下的柜机
            while (true) {
                electricityCabinetQuery.setOffset(offset);

                R r = electricityCabinetService.queryAllElectricityCabinet(electricityCabinetQuery);
                if (ObjectUtils.isEmpty(r) || ObjectUtils.isEmpty(r.getData())) {
                    break;
                }

                offset += size;

                doSendNormalCommandByCabinetList((List<ElectricityCabinetVO>) r.getData(), operatorId);
            }
        });
    }

    private void doSendNormalCommandByCabinetList(List<ElectricityCabinetVO> electricityCabinetVOList, String operatorId) {
        if (ObjectUtils.isEmpty(electricityCabinetVOList)) {
            return;
        }

        electricityCabinetVOList.stream().forEach(electricityCabinetVO -> {
            // 检测柜机是否在线
            boolean b = electricityCabinetService.deviceIsOnline(electricityCabinetVO.getProductKey(), electricityCabinetVO.getDeviceName(), electricityCabinetVO.getPattern());
            if (!b) {
                return;
            }

            // 检测缓存是否存在
            ElectricityCabinetOtherSetting otherSetting = redisService.getWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinetVO.getId(),
                    ElectricityCabinetOtherSetting.class);
            if (ObjectUtils.isEmpty(otherSetting) || ObjectUtils.isEmpty(otherSetting.getApplicationMode())) {
                return;
            }

            // 检测柜机是否为normal/normal_v
            if (!(Objects.equals(otherSetting.getApplicationMode(), CabinetBoxConstant.APPLICATION_MODE_NORMAL_V)
                    || Objects.equals(otherSetting.getApplicationMode(), CabinetBoxConstant.APPLICATION_MODE_NORMAL))) {
                return;
            }

            // 发送指令
            EleOuterCommandQuery eleOuterCommandQuery = new EleOuterCommandQuery();
            eleOuterCommandQuery.setCommand(ElectricityIotConstant.ELE_OTHER_SETTING);
            eleOuterCommandQuery.setProductKey(electricityCabinetVO.getProductKey());
            eleOuterCommandQuery.setDeviceName(electricityCabinetVO.getDeviceName());

            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("applicationMode", otherSetting.getApplicationMode());
            eleOuterCommandQuery.setData(dataMap);

            R sendCommandResult = electricityCabinetService.sendCommand(eleOuterCommandQuery);
            if (!sendCommandResult.isSuccess()) {
                log.info("electricity cabinet send normal warn! cabinet send command fail operatorId:{}, cabinetId:{}, msg:{}", operatorId, electricityCabinetVO.getId(),
                        sendCommandResult.getErrMsg());
                return;
            }

            String sessionId = (String) sendCommandResult.getData();

            // 发送异步消息
            ElectricityCabinetSendNormalDTO electricityCabinetSendNormalDTO = ElectricityCabinetSendNormalDTO.builder().cabinetId(electricityCabinetVO.getId())
                    .operatorId(operatorId).sessionId(sessionId).build();

            rocketMqService.sendSyncMsg(MqProducerConstant.CABINET_NORMAL_RESULT_TOPIC_NAME, JsonUtil.toJson(electricityCabinetSendNormalDTO), "", "", 4);
        });
    }
}
