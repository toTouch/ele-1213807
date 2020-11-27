package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.storage.config.AliyunOssConfig;
import com.xiliulou.storage.config.MinioConfig;
import com.xiliulou.storage.service.impl.AliyunOssService;
import com.xiliulou.storage.service.impl.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 换电柜文件表(TElectricityCabinetFile)表控制层
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
@RestController
@Slf4j
public class ElectricityCabinetFileAdminController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    @Autowired
    AliyunOssConfig aliyunOssConfig;
    @Autowired
    AliyunOssService aliyunOssService;
    @Autowired
    MinioConfig minioConfig;
    @Autowired
    MinioService minioService;

    //minio上传
    @PostMapping("/admin/electricityCabinetFileService/minio/upload")
    public R minioUpload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                         @RequestParam("fileType") Integer fileType) {
        if (Objects.isNull(electricityCabinetId)) {
            electricityCabinetId = -1;
        }
        int index=1;
        List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(electricityCabinetId, fileType);
        if(ObjectUtil.isNotEmpty(electricityCabinetFileList)){
            electricityCabinetFileList= electricityCabinetFileList.stream().sorted(Comparator.comparing(ElectricityCabinetFile::getIndex).reversed()).collect(Collectors.toList());
            index=electricityCabinetFileList.get(0).getIndex()+1;
        }
        String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
        String bucketName = minioConfig.getBucketName();
        try {
            minioService.uploadMinioFile(bucketName, fileName, file.getInputStream());
            ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .delFlag(ElectricityCabinetFile.DEL_NORMAL)
                    .electricityCabinetId(electricityCabinetId)
                    .type(fileType)
                    .bucketName(bucketName)
                    .name(fileName)
                    .index(index).build();
            electricityCabinetFileService.insert(electricityCabinetFile);
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.fail(e.getLocalizedMessage());
        }
        return R.ok();
    }

   //oss上传
    @PostMapping("/admin/electricityCabinetFileService/oss/call/back")
    public R ossCallBack(@RequestParam("fileName") String fileName,
                              @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                              @RequestParam("fileType") Integer fileType) {

        if (StrUtil.isEmpty(fileName)) {
            log.error("UPLOAD ERROR! no filename");
            return R.fail("SYSTEM.0008","文件名不能为空");
        }
        if (Objects.isNull(electricityCabinetId)) {
            electricityCabinetId = -1;
        }
        int index=1;
        List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(electricityCabinetId, fileType);
        if(ObjectUtil.isNotEmpty(electricityCabinetFileList)){
            electricityCabinetFileList= electricityCabinetFileList.stream().sorted(Comparator.comparing(ElectricityCabinetFile::getIndex).reversed()).collect(Collectors.toList());
            index=electricityCabinetFileList.get(0).getIndex()+1;
        }
        ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .delFlag(ElectricityCabinetFile.DEL_NORMAL)
                .electricityCabinetId(electricityCabinetId)
                .type(fileType)
                .url(AliyunOssConfig.https + aliyunOssConfig.getBucketName() + "." + aliyunOssConfig.getEndpoint() + "/" + fileName)
                .name(fileName)
                .index(index).build();
        electricityCabinetFileService.insert(electricityCabinetFile);
        return R.ok();
    }

    /**
     * 获取文件信息
     *
     */
    @GetMapping("/admin/electricityCabinetFileService/getFile/{electricityCabinetId}/{fileType}")
    public R getFile(@PathVariable("electricityCabinetId") Integer electricityCabinetId, @PathVariable("fileType") Integer fileType) {
        List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(electricityCabinetId, fileType);
        if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
            return R.ok();
        }
        List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
        for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList) {
            electricityCabinetFile.setUrl(aliyunOssService.getOssFileUrl(aliyunOssConfig.getBucketName(), electricityCabinetFile.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
            electricityCabinetFiles.add(electricityCabinetFile);
        }
        return R.ok(electricityCabinetFiles);
    }

    /**
     * minio获取文件
     *
     */
    @GetMapping("/admin/electricityCabinetFileService/getMinioFile/{fileName}")
    public void getMinioFile(@PathVariable String fileName, HttpServletResponse response) {
        electricityCabinetFileService.getMinioFile(fileName, response);
    }

    @DeleteMapping("/admin/electricityCabinetFileService/deleteFile/{id}")
    public R deleteFile(@PathVariable("id") Long id) {
        return electricityCabinetFileService.deleteById(id);
    }



}