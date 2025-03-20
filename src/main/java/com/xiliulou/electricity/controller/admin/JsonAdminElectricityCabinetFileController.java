package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.converter.storage.StorageConverter;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.request.asset.ElectricityCabinetPictureBatchSaveRequest;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.tenant.TenantContextHolder;

import com.xiliulou.storage.config.StorageProperties;
import com.xiliulou.storage.service.StorageService;
import com.xiliulou.storage.service.impl.AliyunOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 换电柜文件表(TElectricityCabinetFile)表控制层
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */

@RestController
@Slf4j
public class JsonAdminElectricityCabinetFileController {
    
    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    
    @Autowired
    StorageService storageService;
    @Autowired
    AliyunOssService aliyunOssService;
    @Autowired
    StorageProperties storageProperties;
    @Autowired
    StorageConverter storageConverter;
    
    //通知前端是aili还是oss
    @GetMapping("/admin/electricityCabinetFileService/noticeIsOss")
    public R noticeIsOss() {
        if (Objects.equals(storageService.IS_USE_OSS, storageService.getIsUseOSS())) {
            return R.ok(storageService.getOssUploadSign());
        } else {
            return R.ok(storageService.IS_USE_MINIO);
        }
    }
    
    //minio上传
    @PostMapping("/admin/electricityCabinetFileService/minio/upload")
    public R minioUpload(@RequestParam("file") MultipartFile file) {
        String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
        Map<String, String> resultMap = new HashMap<>(4);
        //        resultMap.put("bucketName", bucketName);
        resultMap.put("fileName", fileName);
        try {
            storageService.uploadFile(fileName, file.getInputStream());
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.fail(e.getLocalizedMessage());
        }
        return R.ok(resultMap);
    }
    
    //统一上传
    @PostMapping("/admin/electricityCabinetFileService/call/back")
    public R callBack(@RequestBody CallBackQuery callBackQuery) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (ObjectUtil.isEmpty(callBackQuery.getFileNameList())) {
            return R.ok();
        }
        
        //换电柜
        if (ObjectUtil.equal(callBackQuery.getFileType(), ElectricityCabinetFile.TYPE_ELECTRICITY_CABINET)) {
            if (Objects.isNull(callBackQuery.getOtherId())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }
        
        //先删除
        electricityCabinetFileService.deleteByDeviceInfo(callBackQuery.getOtherId(), callBackQuery.getFileType(),
                storageService.getIsUseOSS());
        
        //再新增
        if (Objects.equals(StorageService.IS_USE_OSS, storageService.getIsUseOSS())) {
            int index = 1;
            for (String fileName : callBackQuery.getFileNameList()) {
                ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
                        .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                        .otherId(callBackQuery.getOtherId()).type(callBackQuery.getFileType())
                        .url(storageService.HTTPS+storageService.getBucketName()+"."+storageService.getEndpoint()+"/"+fileName).name(fileName).sequence(index)
                        .isOss(storageService.IS_USE_OSS).tenantId(tenantId).build();
                electricityCabinetFileService.insert(electricityCabinetFile);
                index = index + 1;
            }
            
        } else {
            int index = 1;
            for (String fileName : callBackQuery.getFileNameList()) {
                ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
                        .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                        .otherId(callBackQuery.getOtherId()).type(callBackQuery.getFileType())
                        .bucketName(storageService.getBucketName()).name(fileName).sequence(index)
                        .isOss(storageService.IS_USE_MINIO).tenantId(tenantId).build();
                electricityCabinetFileService.insert(electricityCabinetFile);
                index = index + 1;
            }
        }
        return R.ok();
    }
    
    /**
     * 电柜图片批量保存
     *
     * @param batchSaveRequest
     * @return
     */
    @PostMapping("/admin/electricityCabinetFileService/electricityCabinetPictureBatchSave")
    public R electricityCabinetPictureBatchSave(
            @RequestBody @Valid ElectricityCabinetPictureBatchSaveRequest batchSaveRequest) {
        if (ObjectUtil.isEmpty(batchSaveRequest.getFileNameList())) {
            return R.ok();
        }
        
        //换电柜
        if (ObjectUtil.equal(batchSaveRequest.getFileType(), ElectricityCabinetFile.TYPE_ELECTRICITY_CABINET)
                && CollectionUtils.isEmpty(batchSaveRequest.getOtherIdList())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        return electricityCabinetFileService.batchSaveCabinetPicture(batchSaveRequest);
    }
    /**
     * 获取文件信息
     */
    
    @GetMapping("/admin/electricityCabinetFileService/getFile")
    public R getFile(@RequestParam(value = "otherId", required = false) Long otherId,
            @RequestParam("fileType") Integer fileType) {
        List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(
                otherId, fileType, storageService.getIsUseOSS());
        if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
            return R.ok();
        }
        List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
        for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList) {
            if (Objects.equals(storageService.IS_USE_OSS, storageService.getIsUseOSS())) {
                String pic = electricityCabinetFile.getName();
                electricityCabinetFile.setUrl(storageConverter.filePrefixFoldPathHuaweiOrAliWithCdn(pic));
                
            }
            electricityCabinetFiles.add(electricityCabinetFile);
        }
        return R.ok(electricityCabinetFiles);
    }
    
    /**
     * minio获取文件
     */
    
    @GetMapping("/admin/electricityCabinetFileService/getMinioFile/{fileName}")
    public void getMinioFile(@PathVariable String fileName, HttpServletResponse response) {
        electricityCabinetFileService.getMinioFile(fileName, response);
    }
    
    /**
     * 删除文件
     */
    
    @DeleteMapping("/admin/electricityCabinetFileService/deleteFile/{id}")
    public R deleteFile(@PathVariable("id") Long id) {
        return electricityCabinetFileService.deleteById(id);
    }
    
}

