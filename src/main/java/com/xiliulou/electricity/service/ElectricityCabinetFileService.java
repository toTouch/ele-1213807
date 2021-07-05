
package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 换电柜文件表(TElectricityCabinetFile)表服务接口
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */

public interface ElectricityCabinetFileService {

    /**
     * 新增数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 实例对象
     */

    ElectricityCabinetFile insert(ElectricityCabinetFile electricityCabinetFile);



    /**
     * 通过主键删除数据
     */

    R deleteById(Long id);

    List<ElectricityCabinetFile> queryByDeviceInfo(Integer electricityCabinetId, Integer fileType,Integer  isUseOSS);

    void getMinioFile(String fileName, HttpServletResponse response);

    void deleteByDeviceInfo(Integer otherId, Integer fileType,Integer  isUseOSS);
}
