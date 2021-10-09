package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.MaintenanceRecord;
import com.xiliulou.electricity.mapper.MaintenanceRecordMapper;
import com.xiliulou.electricity.query.MaintenanceRecordHandleQuery;
import com.xiliulou.electricity.query.MaintenanceRecordListQuery;
import com.xiliulou.electricity.query.UserMaintenanceQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.MaintenanceRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.MaintenanceRecordVo;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (MaintenanceRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-09-26 14:07:39
 */
@Service("maintenanceRecordService")
@Slf4j
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {
    @Resource
    private MaintenanceRecordMapper maintenanceRecordMapper;

    @Autowired
    UserService userService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    StorageConfig storageConfig;

    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public MaintenanceRecord queryByIdFromDB(Long id) {
        return this.maintenanceRecordMapper.selectById(id);
    }


    /**
     * 新增数据
     *
     * @param maintenanceRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceRecord insert(MaintenanceRecord maintenanceRecord) {
        this.maintenanceRecordMapper.insert(maintenanceRecord);
        return maintenanceRecord;
    }

    /**
     * 修改数据
     *
     * @param maintenanceRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(MaintenanceRecord maintenanceRecord) {
        return this.maintenanceRecordMapper.updateById(maintenanceRecord);

    }



    @Override
    public Triple<Boolean, String, Object> saveSubmitRecord(UserMaintenanceQuery userMaintenanceQuery) {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "SYSTEM.0006", "用户不存在");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(userMaintenanceQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("Maintenance Record error! not found electricityCabinet! uid={},cid={}", userInfo.getUid(), userMaintenanceQuery.getElectricityCabinetId());
            return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
        }


        MaintenanceRecord build = MaintenanceRecord.builder()
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .electricityCabinetId(userMaintenanceQuery.getElectricityCabinetId())
                .pic(userMaintenanceQuery.getFilepath())
                .remark(userMaintenanceQuery.getRemark())
                .type(userMaintenanceQuery.getType())
                .status(MaintenanceRecord.STATUS_CREATED)
                .tenantId(tenantId)
                .build();
        insert(build);
        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> queryListForUser(MaintenanceRecordListQuery query) {
        List<MaintenanceRecord> returnList = maintenanceRecordMapper.queryList(query);
        if (!DataUtil.collectionIsUsable(returnList)) {
            return Triple.of(true, null, Collections.EMPTY_LIST);
        }

        for (MaintenanceRecord maintenanceRecord:returnList) {
            if(StringUtil.isNotEmpty(maintenanceRecord.getPic())){
                maintenanceRecord.setPic(storageService.getOssFileUrl(storageConfig.getBucketName(), maintenanceRecord.getPic(), System.currentTimeMillis() + 10 * 60 * 1000L));
            }
        }

        return generateVoReturn(returnList);
    }

    @Override
    public Triple<Boolean, String, Object> queryListForAdmin(MaintenanceRecordListQuery query) {
        List<MaintenanceRecord> returnList = maintenanceRecordMapper.queryList(query);
        if (!DataUtil.collectionIsUsable(returnList)) {
            return Triple.of(true, null, Collections.EMPTY_LIST);
        }

        for (MaintenanceRecord maintenanceRecord:returnList) {
            if(StringUtil.isNotEmpty(maintenanceRecord.getPic())){
                maintenanceRecord.setPic(storageService.getOssFileUrl(storageConfig.getBucketName(), maintenanceRecord.getPic(), System.currentTimeMillis() + 10 * 60 * 1000L));
            }
        }


        return generateVoReturn(returnList);
    }

    @Override
    public Triple<Boolean, String, Object> handleMaintenanceRecord(MaintenanceRecordHandleQuery maintenanceRecordHandleQuery) {
        MaintenanceRecord maintenanceRecord = queryByIdFromDB(maintenanceRecordHandleQuery.getId());
        if (Objects.isNull(maintenanceRecord)) {
            return Triple.of(false, "ELECTRICITY.0097", "没有此故障记录");
        }

        maintenanceRecord.setUpdateTime(System.currentTimeMillis());
        maintenanceRecord.setOperateUid(SecurityUtils.getUid());
        maintenanceRecord.setOperateRemark(maintenanceRecordHandleQuery.getRemark());
        maintenanceRecord.setStatus(maintenanceRecordHandleQuery.getStatus());
        update(maintenanceRecord);
        return Triple.of(true, null, null);
    }

    @Override
    public R queryCountForUser(MaintenanceRecordListQuery query) {
        return R.ok(maintenanceRecordMapper.queryCount(query));
    }

    @Override
    public R  queryCountForAdmin(MaintenanceRecordListQuery query) {
        return R.ok(maintenanceRecordMapper.queryCount(query));
    }

    private Triple<Boolean, String, Object> generateVoReturn(List<MaintenanceRecord> returnList) {
        List<MaintenanceRecordVo> collect = returnList.parallelStream().map(e -> {
            MaintenanceRecordVo maintenanceRecordVo = new MaintenanceRecordVo();
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
            BeanUtils.copyProperties(e, maintenanceRecordVo);
            maintenanceRecordVo.setElectricityCabinetName(electricityCabinet.getName());
            maintenanceRecordVo.setUrl(e.getPic());
            return maintenanceRecordVo;
        }).collect(Collectors.toList());

        return Triple.of(true, null, collect);
    }
}
