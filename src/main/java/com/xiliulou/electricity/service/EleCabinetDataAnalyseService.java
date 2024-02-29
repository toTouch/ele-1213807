package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import com.xiliulou.electricity.vo.EleCabinetOrderAnalyseVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface EleCabinetDataAnalyseService {
    List<EleCabinetDataAnalyseVO> selectOfflineByPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectLockPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery);

    EleCabinetOrderAnalyseVO averageStatistics(Integer eid);

    EleCabinetOrderAnalyseVO todayStatistics(Integer eid);
    
    List<EleCabinetDataAnalyseVO> selectLowPowerPage(ElectricityCabinetQuery cabinetQuery);
    
    Integer selectLowPowerPageCount(ElectricityCabinetQuery cabinetQuery);
    
    List<EleCabinetDataAnalyseVO> selectFullPowerPage(ElectricityCabinetQuery cabinetQuery);
    
    Integer selectFullPowerPageCount(ElectricityCabinetQuery cabinetQuery);
}
