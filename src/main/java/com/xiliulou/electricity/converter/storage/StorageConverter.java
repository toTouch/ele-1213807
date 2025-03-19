/**
 * Create date: 2024/9/23
 */

package com.xiliulou.electricity.converter.storage;

import com.xiliulou.storage.config.CdnConfig;
import com.xiliulou.storage.config.StorageProperties;
import com.xiliulou.storage.service.StorageService;
import com.xiliulou.storage.service.impl.AliyunOssService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/23 09:40
 */
@Component
public class StorageConverter {
    
    @Autowired
    StorageProperties storageProperties;
    
    @Autowired
    private AliyunOssService aliyunOssService;
    
    /**
     * 返回true 为华为的存储文件
     *
     * @param input
     * @return
     */
    public String filePrefixFoldPathHuaweiOrAliWithCdn(String input) {
        // 正则表达式：匹配以 ^(auth|fault|cabinet|site|store|vehicleModel|sysSet).* 开头的字符串 走华为obs
        String regex = "^(auth|fault|cabinet|site|store|vehicleModel|sysSet).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        //        判定华为云还是阿里云
        if (matcher.matches()) {
            //              判定华为云是否走cdn
            StorageProperties.HuaweiProperties huawei = storageProperties.getHuawei();
            if (storageProperties.IS_USE_CDN.equals(huawei.getCdn())) {
                return "https://" + huawei.getUrlPrefix() + "/" + input;
            } else {
                return "https://" + huawei.getBucketName() + "." + huawei.getEndpoint() + "/" + input;
            }
        } else {
            StorageProperties.AliyunProperties ali = storageProperties.getAliyun();
            if (storageProperties.IS_USE_CDN.equals(ali.getCdn())) {
                return "https://" + ali.getUrlPrefix() + "/" + input;
            } else {
                return aliyunOssService.getOssFileUrl(ali.getBucketName(), input,
                        System.currentTimeMillis() + 10 * 60 * 1000L);
            }
        }
        
    }
    
    public String getPicRemovePrefix(String picUrl) {
        String regex = "https?://[^/]+\\.com/(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(picUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return picUrl;
    }
    
    
}
