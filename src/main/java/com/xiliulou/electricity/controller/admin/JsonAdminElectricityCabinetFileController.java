
package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	StorageConfig storageConfig;

	@Qualifier("aliyunOssService")
	@Autowired
	StorageService storageService;

	//通知前端是aili还是oss
	@GetMapping("/admin/electricityCabinetFileService/noticeIsOss")
	public R noticeIsOss() {
		if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
			return R.ok(storageService.getOssUploadSign());
		} else {
			return R.ok(StorageConfig.IS_USE_MINIO);
		}
	}

	//minio上传
	@PostMapping("/admin/electricityCabinetFileService/minio/upload")
	public R minioUpload(@RequestParam("file") MultipartFile file) {
		String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
		String bucketName = storageConfig.getBucketName();
		Map<String, String> resultMap = new HashMap<>(4);
		resultMap.put("bucketName", bucketName);
		resultMap.put("fileName", fileName);
		try {
			storageService.uploadFile(bucketName, fileName, file.getInputStream());
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
		electricityCabinetFileService.deleteByDeviceInfo(callBackQuery.getOtherId(), callBackQuery.getFileType(), storageConfig.getIsUseOSS());


		//再新增
		if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
			int index = 1;
			for (String fileName : callBackQuery.getFileNameList()) {
				ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
						.createTime(System.currentTimeMillis())
						.updateTime(System.currentTimeMillis())
						.otherId(callBackQuery.getOtherId())
						.type(callBackQuery.getFileType())
						.url(StorageConfig.HTTPS + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" + fileName)
						.name(fileName)
						.sequence(index)
						.isOss(StorageConfig.IS_USE_OSS)
						.tenantId(tenantId).build();
				electricityCabinetFileService.insert(electricityCabinetFile);
				index = index + 1;
			}

		} else {
			int index = 1;
			for (String fileName : callBackQuery.getFileNameList()) {
				ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
						.createTime(System.currentTimeMillis())
						.updateTime(System.currentTimeMillis())
						.otherId(callBackQuery.getOtherId())
						.type(callBackQuery.getFileType())
						.bucketName(storageConfig.getBucketName())
						.name(fileName)
						.sequence(index)
						.isOss(StorageConfig.IS_USE_MINIO)
						.tenantId(tenantId).build();
				electricityCabinetFileService.insert(electricityCabinetFile);
				index = index + 1;
			}
		}
		return R.ok();
	}

	/**
	 * 获取文件信息
	 */

	@GetMapping("/admin/electricityCabinetFileService/getFile")
	public R getFile(@RequestParam(value = "otherId", required = false) Long otherId,
			@RequestParam("fileType") Integer fileType) {
		List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(otherId, fileType, storageConfig.getIsUseOSS());
		if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
			return R.ok();
		}
		List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
		for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList) {
			if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
				electricityCabinetFile.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), electricityCabinetFile.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
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

