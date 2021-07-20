package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
public interface ElectricityCabinetOrderOperHistoryMapper extends BaseMapper<ElectricityCabinetOrderOperHistory>{

    List<ElectricityCabinetOrderOperHistory> queryListByOrderId(@Param("query")ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery);

	Integer queryCountByOrderId(@Param("query") ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery);
}
