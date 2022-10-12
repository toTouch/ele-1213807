package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.mapper.OtaFileConfigMapper;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.impl.AliyunOssService;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * (OtaFileConfig)表服务实现类
 *
 * @author Hardy
 * @since 2022-10-12 09:24:48
 */
@Service("otaFileConfigService")
@Slf4j
public class OtaFileConfigServiceImpl implements OtaFileConfigService {
    
    /**
     * 核心板oss路径
     */
    private static String coreBoardPath = "a/b/core_board.bin";
    
    /**
     * 子板oss路径
     */
    private static String daughterBoardPath = "a/b/daughter_board.bin";
    
    /**
     * AliYunOss路径
     */
    private static String aliYunOssUrl = "https://xiliulou-electricity.oss-cn-beijing.aliyuncs.com/";
    
    @Resource
    private OtaFileConfigMapper otaFileConfigMapper;
    
    @Autowired
    private AliyunOssService aliyunOssService;
    
    @Autowired
    private StorageConfig storageConfig;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public OtaFileConfig queryByIdFromDB(Long id) {
        return this.otaFileConfigMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public OtaFileConfig queryByIdFromCache(Long id) {
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
    public List<OtaFileConfig> queryAllByLimit(int offset, int limit) {
        return this.otaFileConfigMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param otaFileConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OtaFileConfig insert(OtaFileConfig otaFileConfig) {
        this.otaFileConfigMapper.insertOne(otaFileConfig);
        return otaFileConfig;
    }
    
    /**
     * 修改数据
     *
     * @param otaFileConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(OtaFileConfig otaFileConfig) {
        return this.otaFileConfigMapper.update(otaFileConfig);
        
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
        return this.otaFileConfigMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R otaFileConfigUpload(MultipartFile file, String name, String version, Integer type) {
        try (InputStream inputStream = file.getInputStream()) {
            String ossPath = OtaFileConfig.TYPE_DAUGHTER_BOARD.equals(type) ? daughterBoardPath : coreBoardPath;
            String sha256Hex = DigestUtils.sha256Hex(inputStream);
            String downloadLink = aliYunOssUrl + ossPath;
            
            aliyunOssService.uploadFile(storageConfig.getBucketName(), ossPath, inputStream);
            
            OtaFileConfig otaFileConfig = new OtaFileConfig();
            otaFileConfig.setName(name);
            otaFileConfig.setDownloadLink(downloadLink);
            otaFileConfig.setSha256Value(sha256Hex);
            otaFileConfig.setVersion(version);
            otaFileConfig.setType(type);
            otaFileConfig.setCreateTime(System.currentTimeMillis());
            otaFileConfig.setUpdateTime(System.currentTimeMillis());
            
            
        } catch (Exception e) {
        
        }
        return null;
    }
}
