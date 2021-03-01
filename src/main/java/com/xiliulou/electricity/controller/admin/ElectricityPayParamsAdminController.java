package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:35
 **/
@Slf4j
@RestController
public class ElectricityPayParamsAdminController {
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    public static final Long API_DEFAULT_SIZE = 51200L;
    public static final String API_FILE_TYPE = "p12";

    /**
     * 新增/修改支付参数
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/electricityPayParams")
    public R save(@RequestBody @Validated ElectricityPayParams electricityPayParams) {

        return electricityPayParamsService.saveOrUpdateElectricityPayParams(electricityPayParams, System.currentTimeMillis());
    }

    /**
     * huoqu
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/electricityPayParams")
    public R get() {

        return R.ok(electricityPayParamsService.getElectricityPayParams());
    }

    /**
     * 退款证书文件上传
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/wxUploadCert", consumes = "multipart/form-data")
    @Transactional
    public R singleFileUpload(@RequestParam("file") MultipartFile file) {
        //文件上传大小的限制
        if (file.getSize() > API_DEFAULT_SIZE) {
            return R.fail("API证书不能大于50KB");
        }
        String fileType = FileUtil.extName(file.getOriginalFilename());
        if (!Objects.equals(fileType, API_FILE_TYPE)) {
            return R.fail("api文件不正确");
        }
        ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
        if (Objects.isNull(electricityPayParams)) {
            return R.fail("请先配置支付信息");
        }
        String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());


        String path = "/opt/cert/" + fileName;
        File newFile = new File(path);
        //MultipartFile（注意这个时候）
        try {
            file.transferTo(newFile);
        } catch (IOException e) {
            log.error("上传失败", e);
            return R.fail(e.getLocalizedMessage());
        }
        electricityPayParams.setApiName(fileName);
        electricityPayParamsService.saveOrUpdateElectricityPayParams(electricityPayParams,System.currentTimeMillis());
        return R.ok();
    }


}
