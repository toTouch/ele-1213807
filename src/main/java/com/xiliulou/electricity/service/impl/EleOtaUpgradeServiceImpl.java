package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.EleOtaUpgrade;
import com.xiliulou.electricity.entity.EleOtaUpgradeHistory;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.mapper.EleOtaUpgradeMapper;
import com.xiliulou.electricity.query.OtaUpgradeQuery;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import com.xiliulou.electricity.service.EleOtaFileService;
import com.xiliulou.electricity.service.EleOtaUpgradeHistoryService;
import com.xiliulou.electricity.service.EleOtaUpgradeService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.electricity.vo.OtaUpgradeInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleOtaUpgrade)表服务实现类
 *
 * @author Hardy
 * @since 2022-10-14 09:02:01
 */
@Service("eleOtaUpgradeService")
@Slf4j
public class EleOtaUpgradeServiceImpl implements EleOtaUpgradeService {
    
    @Resource
    private EleOtaUpgradeMapper eleOtaUpgradeMapper;
    
    @Autowired
    EleCabinetCoreDataService eleCabinetCoreDataService;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    EleOtaUpgradeHistoryService eleOtaUpgradeHistoryService;
    
    @Autowired
    OtaFileConfigService otaFileConfigService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaUpgrade queryByIdFromDB(Long id) {
        return this.eleOtaUpgradeMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaUpgrade queryByIdFromCache(Long id) {
        return null;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<EleOtaUpgrade> queryAllByLimit(int offset, int limit) {
        return this.eleOtaUpgradeMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleOtaUpgrade insert(EleOtaUpgrade eleOtaUpgrade) {
        this.eleOtaUpgradeMapper.insertOne(eleOtaUpgrade);
        return eleOtaUpgrade;
    }
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleOtaUpgrade eleOtaUpgrade) {
        return this.eleOtaUpgradeMapper.update(eleOtaUpgrade);
        
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
        return this.eleOtaUpgradeMapper.deleteById(id) > 0;
    }
    
    @Override
    public R queryVoList(Long eid) {
        List<OtaUpgradeInfoVo> otaCellNoUpgradeInfoVos = Optional
                .ofNullable(eleOtaUpgradeMapper.queryCellNoUpgradeInfoVoList(eid)).orElse(Lists.newArrayList());

        OtaUpgradeInfoVo otaCoreUpgradeInfoVo = Optional.ofNullable(eleOtaUpgradeMapper.queryCoreUpgradeInfoVo(eid))
                .orElse(new OtaUpgradeInfoVo());
        otaCellNoUpgradeInfoVos.add(0, otaCoreUpgradeInfoVo);
        return R.ok(otaCellNoUpgradeInfoVos);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEleOtaUpgradeAndSaveHistory(List<Integer> cellNos, Integer eid, String sessionId) {
        Optional.ofNullable(cellNos).orElse(Lists.newArrayList()).parallelStream().forEach(cellNo -> {
            Integer type = Objects.equals(cellNo, 0) ? EleOtaUpgrade.TYPE_CORE : EleOtaUpgrade.TYPE_SUB;
            EleOtaUpgrade eleOtaUpgradeFromDb = queryByEidAndCellNo(eid, cellNo, type);
            if (Objects.isNull(eleOtaUpgradeFromDb)) {
                EleOtaUpgrade eleOtaUpgrade = new EleOtaUpgrade();
                eleOtaUpgrade.setElectricityCabinetId(Long.valueOf(eid));
                eleOtaUpgrade.setCellNo(String.valueOf(cellNo));
                eleOtaUpgrade.setStatus(EleOtaUpgrade.STATUS_INIT);
                eleOtaUpgrade.setType(type);
                eleOtaUpgrade.setCreateTime(System.currentTimeMillis());
                eleOtaUpgrade.setUpdateTime(System.currentTimeMillis());
                eleOtaUpgradeMapper.insertOne(eleOtaUpgrade);
            } else {
                EleOtaUpgrade eleOtaUpgrade = new EleOtaUpgrade();
                eleOtaUpgrade.setId(eleOtaUpgradeFromDb.getId());
                eleOtaUpgrade.setStatus(EleOtaUpgrade.STATUS_INIT);
                eleOtaUpgrade.setUpdateTime(System.currentTimeMillis());
                eleOtaUpgradeMapper.update(eleOtaUpgrade);
            }
            
            EleOtaUpgradeHistory eleOtaUpgradeHistory = new EleOtaUpgradeHistory();
            eleOtaUpgradeHistory.setCellNo(String.valueOf(cellNo));
            eleOtaUpgradeHistory.setElectricityCabinetId(Long.valueOf(eid));
            eleOtaUpgradeHistory.setType(type);
            eleOtaUpgradeHistory.setUpgradeVersion(queryOtaVersionByEidAndCellNo(type));
            eleOtaUpgradeHistory.setHistoryVersion(queryEleVersionByEidAndCellNo(eid, cellNo, type));
            eleOtaUpgradeHistory.setStatus(EleOtaUpgrade.STATUS_INIT);
            eleOtaUpgradeHistory.setSessionId(sessionId);
            eleOtaUpgradeHistory.setCreateTime(System.currentTimeMillis());
            eleOtaUpgradeHistory.setUpdateTime(System.currentTimeMillis());
            eleOtaUpgradeHistoryService.insert(eleOtaUpgradeHistory);
        });
    }
    
    private String queryOtaVersionByEidAndCellNo(Integer type) {
        OtaFileConfig otaFileConfig = otaFileConfigService.queryByType(type);
        if (Objects.nonNull(otaFileConfig)) {
            return otaFileConfig.getVersion();
        }
        return null;
    }
    
    private String queryEleVersionByEidAndCellNo(Integer eid, Integer cellNo, Integer type) {
        if (EleOtaUpgrade.TYPE_CORE.equals(type)) {
            EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.selectByEleCabinetId(eid);
            if (Objects.nonNull(eleCabinetCoreData)) {
                return eleCabinetCoreData.getCoreVersion();
            }
        }
        
        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService
                .queryByCellNo(eid, String.valueOf(cellNo));
        if (Objects.nonNull(electricityCabinetBox)) {
            return electricityCabinetBox.getVersion();
        }
        
        return null;
    }
    
    @Override
    public EleOtaUpgrade queryByEidAndCellNo(Integer eid, Integer cellNo, Integer type) {
        return eleOtaUpgradeMapper.queryByEidAndCellNo(eid, cellNo, type);
    }
}
