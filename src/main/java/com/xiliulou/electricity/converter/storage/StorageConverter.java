/**
 * Create date: 2024/9/23
 */

package com.xiliulou.electricity.converter.storage;

import com.xiliulou.storage.config.CdnConfig;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/23 09:40
 */
@Component
public class StorageConverter {
    
    @Resource
    private CdnConfig cdnConfig;
    
    @Resource
    private StorageConfig storageConfig;
    
    @Qualifier("aliyunOssService")
    @Autowired
    private StorageService storageService;
    
    
    /**
     * url组装
     *
     * @param urlSuffix
     * @author caobotao.cbt
     * @date 2024/9/23 14:25
     */
    public String assembleUrl(String urlSuffix) {
        
        String cdnUrl = this.getCdnUrl(urlSuffix);
        
        if (StringUtils.isNotBlank(cdnUrl)) {
            return cdnUrl;
        }
        return StorageConfig.HTTPS + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" + urlSuffix;
    }
    
    
    /**
     * 生成url
     *
     * @param urlSuffix
     * @param expireTime
     * @author caobotao.cbt
     * @date 2024/9/23 14:40
     */
    public String generateUrl(String urlSuffix, long expireTime) {
        
        String cdnUrl = this.getCdnUrl(urlSuffix);
        
        if (StringUtils.isNotBlank(cdnUrl)) {
            return cdnUrl;
        }
        
        return storageService.getOssFileUrl(storageConfig.getBucketName(), urlSuffix, expireTime);
    }
    
    
    /**
     * name: 获取url前缀
     * description:
     * @author caobotao.cbt
     * @date 2024/9/23 15:20
     */
    public String getUrlPrefix() {
        if (CdnConfig.ENABLE.equals(cdnConfig.getEnable())) {
            return cdnConfig.getUrl();
        }
        
        return storageConfig.getUrlPrefix();
    }
    
    
    /**
     * 获取cdn图片地址
     *
     * @param urlSuffix
     * @author caobotao.cbt
     * @date 2024/9/23 14:38
     */
    private String getCdnUrl(String urlSuffix) {
        if (CdnConfig.ENABLE.equals(cdnConfig.getEnable())) {
            return StorageConfig.HTTPS + cdnConfig.getUrl() + "/" + urlSuffix;
        }
        return null;
    }
    
    
}
