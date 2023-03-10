package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.EleIotOtaPathConfig;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.OtaFileConfigMapper;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.impl.AliyunOssService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

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
     * AliYunOss路径
     */
    //private static String aliYunOssUrl = "https://xiliulou-electricity.oss-cn-beijing.aliyuncs.com/";
    
    @Resource
    private OtaFileConfigMapper otaFileConfigMapper;
    
    @Autowired
    private AliyunOssService aliyunOssService;
    
    @Autowired
    private StorageConfig storageConfig;
    
    @Autowired
    private EleIotOtaPathConfig eleIotOtaPathConfig;
    
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
    
    @Override
    public List<OtaFileConfig> queryAll() {
        return this.otaFileConfigMapper.queryAll();
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
    public OtaFileConfig queryByType(Integer type) {
        return this.otaFileConfigMapper.queryByType(type);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R otaFileConfigUpload(MultipartFile file, String name, String version, Integer type) {
        if (!User.TYPE_USER_SUPER.equals(SecurityUtils.getUserInfo().getType())) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        if (type < 1 || type > 4) {
            log.error("OTA UPLOAD ERROR! ota file type error! name={}, version={}, type={}", name, version, type);
            return R.fail("100300", "ota文件类型不合法,请联系管理员或重新上传！");
        }
    
        if (StringUtils.isBlank(version)) {
            log.error("OTA UPLOAD ERROR! ota file version error! name={}, version={}, type={}", name, version, type);
            return R.fail("100313", "ota文件版本号不合法");
        }
    
        Pair<Boolean, Integer> result = resolutionVersion(version);
        if (!result.getLeft()) {
            log.error("OTA UPLOAD ERROR! ota file version error! name={}, version={}, type={}", name, version, type);
            return R.fail("100313", "ota文件版本号不合法");
        }
    
        Integer versionNum = result.getRight();
        if (Objects.equals(type, OtaFileConfig.TYPE_OLD_CORE_BOARD) || Objects
                .equals(type, OtaFileConfig.TYPE_OLD_SUB_BOARD)) {
            if (Objects.isNull(versionNum) || versionNum < 50) {
                log.error("OTA UPLOAD ERROR! ota file version error! name={}, version={}, type={}", name, version,
                        type);
                return R.fail("100313", "ota文件版本号不合法");
            }
        }
    
        if (Objects.equals(type, OtaFileConfig.TYPE_CORE_BOARD) || Objects.equals(type, OtaFileConfig.TYPE_SUB_BOARD)) {
            if (Objects.isNull(versionNum) || versionNum > 50) {
                log.error("OTA UPLOAD ERROR! ota file version error! name={}, version={}, type={}", name, version,
                        type);
                return R.fail("100313", "ota文件版本号不合法");
            }
        }
    
        InputStream ossInputStream = null;
        InputStream sha256HexInputStream = null;
        try {
            String ossPath = eleIotOtaPathConfig.getOtaPath() + name;
            String downloadLink =
                    "https://" + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" + ossPath;
        
            byte[] fileByte = file.getBytes();
            ossInputStream = new ByteArrayInputStream(fileByte);
            sha256HexInputStream = new ByteArrayInputStream(fileByte);
        
            aliyunOssService.uploadFile(storageConfig.getBucketName(), ossPath, ossInputStream);
        
            String sha256Hex = DigestUtils.sha256Hex(sha256HexInputStream);
            OtaFileConfig otaFileConfig = queryByType(type);
            if (Objects.isNull(otaFileConfig)) {
                otaFileConfig = new OtaFileConfig();
                otaFileConfig.setName(name);
                otaFileConfig.setDownloadLink(downloadLink);
                otaFileConfig.setSha256Value(sha256Hex);
                otaFileConfig.setVersion(version);
                otaFileConfig.setType(type);
                otaFileConfig.setCreateTime(System.currentTimeMillis());
                otaFileConfig.setUpdateTime(System.currentTimeMillis());
                this.otaFileConfigMapper.insertOne(otaFileConfig);
            } else {
                otaFileConfig.setName(name);
                otaFileConfig.setDownloadLink(downloadLink);
                otaFileConfig.setSha256Value(sha256Hex);
                otaFileConfig.setVersion(version);
                otaFileConfig.setUpdateTime(System.currentTimeMillis());
                this.otaFileConfigMapper.update(otaFileConfig);
            }
        } catch (Exception e) {
            log.error("OTA_FILE_CONFIG_UPLOAD ERROR!", e);
            return R.fail("ota文件上传失败！");
        } finally {
            if (Objects.nonNull(ossInputStream)) {
                try {
                    ossInputStream.close();
                } catch (IOException e) {
                    log.error("OTA_FILE_CONFIG_UPLOAD ERROR! ossInputStream close fail!", e);
                }
            }
        
            if (Objects.nonNull(sha256HexInputStream)) {
                try {
                    sha256HexInputStream.close();
                } catch (IOException e) {
                    log.error("OTA_FILE_CONFIG_UPLOAD ERROR! sha256HexInputStream close fail!", e);
                }
            }
        }
        return R.ok();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R otaFileConfigDelete(Long id) {
        if (!User.TYPE_USER_SUPER.equals(SecurityUtils.getUserInfo().getType())) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        OtaFileConfig otaFileConfig = this.queryByIdFromDB(id);
        if (Objects.isNull(otaFileConfig)) {
            log.error("OTA_FILE_CONFIG_DELETE ERROR! otaFileConfig is null! id={}", id);
            return R.fail("oat文件不存在");
        }
        try {
            String ossPath = eleIotOtaPathConfig + otaFileConfig.getName();
            //aliyunOssService.removeOssFile(storageConfig.getBucketName(), ossPath);
            this.deleteById(id);
        } catch (Exception e) {
            log.error("OTA_FILE_CONFIG_DELETE ERROR!", e);
            return R.fail("ota文件删除失败！");
        }
        return R.ok();
    }
    
    @Override
    public R otaFileConfigQueryList() {
        if (!User.TYPE_USER_SUPER.equals(SecurityUtils.getUserInfo().getType())) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        return R.ok(queryAll());
    }
    
    
    /**
     * 解析版本号开头
     *
     * @param version
     * @return
     */
    private Pair<Boolean, Integer> resolutionVersion(String version) {
        int index = version.indexOf(".");
        if (Objects.equals(index, -1)) {
            return Pair.of(false, null);
        }
    
        String versionPrefix = version.substring(0, index);
        try {
            int i = Integer.parseInt(versionPrefix);
            return Pair.of(true, i);
        } catch (Exception e) {
            log.error("RESOLUTION VERSION ERROR!", e);
        }
        
        return Pair.of(false, null);
    }
    
}
