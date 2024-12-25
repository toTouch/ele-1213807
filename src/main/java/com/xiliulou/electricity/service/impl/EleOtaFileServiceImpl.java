package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.mapper.EleOtaFileMapper;
import com.xiliulou.electricity.service.EleOtaFileService;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.electricity.vo.OtaFileCheckSumVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
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
            if (!Objects.equals(eleOtaFile.getFileType(), EleOtaFile.TYPE_SIX_FILE) && !Objects.equals(eleOtaFile.getFileType(), EleOtaFile.TYPE_NEW_SIX_FILE)) {
                otaFileCheckSumVo.setCoreSha256HexEle(eleOtaFile.getCoreSha256Value());
                otaFileCheckSumVo.setCoreNameEle(eleOtaFile.getCoreName());
            }
        
            otaFileCheckSumVo.setSubSha256HexEle(eleOtaFile.getSubSha256Value());
            otaFileCheckSumVo.setSubNameEle(eleOtaFile.getSubName());
            otaFileCheckSumVo.setFileType(eleOtaFile.getFileType());
        }
        
        List<OtaFileConfig> otaFileConfigList = otaFileConfigService.listByTypes(
                List.of(OtaFileConfig.TYPE_CORE_BOARD, OtaFileConfig.TYPE_SUB_BOARD, OtaFileConfig.TYPE_OLD_CORE_BOARD, OtaFileConfig.TYPE_OLD_SUB_BOARD,
                        OtaFileConfig.TYPE_SIX_SUB_BOARD, OtaFileConfig.TYPE_NEW_SIX_SUB_BOARD));
        if (CollectionUtils.isEmpty(otaFileConfigList)) {
            return R.ok(otaFileCheckSumVo);
        }
        
        Map<Integer, OtaFileConfig> typeMap = otaFileConfigList.stream().collect(Collectors.toMap(OtaFileConfig::getType, otaFileConfig -> otaFileConfig));
        if (MapUtils.isEmpty(typeMap)) {
            return R.ok(otaFileCheckSumVo);
        }
        
        for (Map.Entry<Integer, OtaFileConfig> entry : typeMap.entrySet()) {
            Integer type = entry.getKey();
            OtaFileConfig otaFileConfig = entry.getValue();
            
            switch (type) {
                case OtaFileConfig.TYPE_CORE_BOARD:
                    otaFileCheckSumVo.setCoreSha256HexCloud(otaFileConfig.getSha256Value());
                    otaFileCheckSumVo.setCoreVersionCloud(otaFileConfig.getVersion());
                    break;
                case OtaFileConfig.TYPE_SUB_BOARD:
                    otaFileCheckSumVo.setSubSha256HexCloud(otaFileConfig.getSha256Value());
                    otaFileCheckSumVo.setSubVersionCloud(otaFileConfig.getVersion());
                    break;
                case OtaFileConfig.TYPE_OLD_CORE_BOARD:
                    otaFileCheckSumVo.setOldCoreSha256HexCloud(otaFileConfig.getSha256Value());
                    otaFileCheckSumVo.setOldCoreVersionCloud(otaFileConfig.getVersion());
                    break;
                case OtaFileConfig.TYPE_OLD_SUB_BOARD:
                    otaFileCheckSumVo.setOldSubSha256HexCloud(otaFileConfig.getSha256Value());
                    otaFileCheckSumVo.setOldSubVersionCloud(otaFileConfig.getVersion());
                    break;
                case OtaFileConfig.TYPE_SIX_SUB_BOARD:
                    otaFileCheckSumVo.setSixSubSha256HexCloud(otaFileConfig.getSha256Value());
                    otaFileCheckSumVo.setSixSubVersionCloud(otaFileConfig.getVersion());
                    break;
                case OtaFileConfig.TYPE_NEW_SIX_SUB_BOARD:
                    otaFileCheckSumVo.setNewSixSubSha256HexCloud(otaFileConfig.getSha256Value());
                    otaFileCheckSumVo.setNewSixSubVersionCloud(otaFileConfig.getVersion());
                    break;
                default:
                    break;
            }
        }
        
        return R.ok(otaFileCheckSumVo);
    }
    
}
