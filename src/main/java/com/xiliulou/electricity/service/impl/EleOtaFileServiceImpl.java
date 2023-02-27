package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.mapper.EleOtaFileMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.EleOtaFileService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.electricity.vo.OtaFileCheckSumVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author zgw
 * @since 2022-10-12 17:31:10
 */
@Service("eleOtaFileServiceImpl")
@Slf4j
public class EleOtaFileServiceImpl implements EleOtaFileService {
    
    @Resource
    private EleOtaFileMapper eleOtaFileMapper;

    
    @Autowired
    private OtaFileConfigService otaFileConfigService;

    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaFile queryByIdFromDB(Long id) {
        return this.eleOtaFileMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaFile queryByIdFromCache(Long id) {
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
    public List<EleOtaFile> queryAllByLimit(int offset, int limit) {
        return this.eleOtaFileMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleOtaFile insert(EleOtaFile eleOtaFile) {
        this.eleOtaFileMapper.insertOne(eleOtaFile);
        return eleOtaFile;
    }
    
    /**
     * 修改数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleOtaFile eleOtaFile) {
        return this.eleOtaFileMapper.update(eleOtaFile);
        
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
        return this.eleOtaFileMapper.deleteById(id) > 0;
    }
    
    @Override
    public EleOtaFile queryByEid(Integer eid) {
        return this.eleOtaFileMapper.queryByEid(eid);
    }
    
    @Override
    public R queryInfo(Integer eid) {
        OtaFileCheckSumVo otaFileCheckSumVo = new OtaFileCheckSumVo();

        EleOtaFile eleOtaFile = this.queryByEid(eid);
        if (Objects.nonNull(eleOtaFile)) {
            otaFileCheckSumVo.setSubSha256HexEle(eleOtaFile.getSubSha256Value());
            otaFileCheckSumVo.setCoreSha256HexEle(eleOtaFile.getCoreSha256Value());
            otaFileCheckSumVo.setCoreNameEle(eleOtaFile.getCoreName());
            otaFileCheckSumVo.setSubNameEle(eleOtaFile.getSubName());
        }
        
        OtaFileConfig coreBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_CORE_BOARD);
        if (Objects.nonNull(coreBoardOtaFileConfig)) {
            otaFileCheckSumVo.setCoreSha256HexCloud(coreBoardOtaFileConfig.getSha256Value());
            otaFileCheckSumVo.setCoreVersionCloud(coreBoardOtaFileConfig.getVersion());
        }
        
        OtaFileConfig subBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_SUB_BOARD);
        if (Objects.nonNull(subBoardOtaFileConfig)) {
            otaFileCheckSumVo.setSubSha256HexCloud(subBoardOtaFileConfig.getSha256Value());
            otaFileCheckSumVo.setSubVersionCloud(subBoardOtaFileConfig.getVersion());
        }
    
        OtaFileConfig oldCoreBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_OLD_CORE_BOARD);
        if (Objects.nonNull(oldCoreBoardOtaFileConfig)) {
            otaFileCheckSumVo.setOldCoreSha256HexCloud(oldCoreBoardOtaFileConfig.getSha256Value());
            otaFileCheckSumVo.setOldCoreVersionCloud(oldCoreBoardOtaFileConfig.getVersion());
        }
    
        OtaFileConfig oldSubBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_OLD_SUB_BOARD);
        if (Objects.nonNull(oldSubBoardOtaFileConfig)) {
            otaFileCheckSumVo.setOldCoreSha256HexCloud(oldSubBoardOtaFileConfig.getSha256Value());
            otaFileCheckSumVo.setOldCoreVersionCloud(oldSubBoardOtaFileConfig.getVersion());
        }
    
        return R.ok(otaFileCheckSumVo);
    }
    
}
