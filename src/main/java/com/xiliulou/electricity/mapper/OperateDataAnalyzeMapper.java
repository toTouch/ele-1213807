package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.OperateDataAnalyze;

import java.util.List;

/**
 * @Description: OperateDataAnalyzeMapper
 * @Author: RenHang
 * @Date: 2025/02/19
 */

public interface OperateDataAnalyzeMapper {

    String selectLatestBatch();

    List<OperateDataAnalyze> selectListByBatch(String batch);
}
