
package com.xiliulou.electricity.service.impl;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.mapper.ElectricityCabinetFileMapper;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;


/**
 * 换电柜文件表(TElectricityCabinetFile)表服务实现类
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */

@Service("electricityCabinetFileService")
@Slf4j
public class ElectricityCabinetFileServiceImpl implements ElectricityCabinetFileService {
    @Resource
    private ElectricityCabinetFileMapper electricityCabinetFileMapper;
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;


    /**
     * 新增数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 实例对象
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetFile insert(ElectricityCabinetFile electricityCabinetFile) {
        this.electricityCabinetFileMapper.insert(electricityCabinetFile);
        return electricityCabinetFile;
    }


    /**
     * 通过主键删除数据
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteById(Long id) {
        ElectricityCabinetFile electricityCabinetFile = electricityCabinetFileMapper.selectById(id);
        if (Objects.isNull(electricityCabinetFile)) {
            return R.fail("ELECTRICITY.0009", "未找到文件");
        }
        electricityCabinetFileMapper.deleteById(id);
        return R.ok();
    }

    @Override
    public List<ElectricityCabinetFile> queryByDeviceInfo(Integer otherId, Integer fileType,Integer  isUseOSS) {
        return electricityCabinetFileMapper.selectList(Wrappers.<ElectricityCabinetFile>lambdaQuery().eq(ElectricityCabinetFile::getOtherId, otherId)
                .eq(ElectricityCabinetFile::getType, fileType).eq(ElectricityCabinetFile::getIsOss,isUseOSS));
    }

    @Override
    public void getMinioFile(String fileName, HttpServletResponse response) {
        int separator = fileName.lastIndexOf(StrUtil.DASHED);
        try (InputStream inputStream = storageService.getFile(fileName.substring(0, separator),
                fileName.substring(separator + 1))) {
            response.setContentType("application/octet-stream; charset=UTF-8");
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件读取异常", e);
        }
    }

    @Override
    public void deleteByDeviceInfo(Integer otherId, Integer fileType,Integer isUseOSS) {
        electricityCabinetFileMapper.deleteByDeviceInfo(otherId,fileType,isUseOSS);
    }
}
