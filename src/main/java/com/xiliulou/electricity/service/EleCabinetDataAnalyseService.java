package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;

import java.util.List;

public interface EleCabinetDataAnalyseService {
    List<EleCabinetDataAnalyseVO> selectOfflineByPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectLockPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectFailurePage(ElectricityCabinetQuery cabinetQuery);

    Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery);

    Integer selectFailurePageCount(ElectricityCabinetQuery cabinetQuery);
}
