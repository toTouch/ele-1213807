package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.bo.OperateDataAnalyzeBO;

import java.util.List;

/**
 * @Description: OperateDataAnalyzeMapper
 * @Author: RenHang
 * @Date: 2025/02/19
 */

public interface OperateDataAnalyzeMapper {

    String selectLatestBatch();

    List<OperateDataAnalyzeBO> selectListByBatch(String batch);
}
