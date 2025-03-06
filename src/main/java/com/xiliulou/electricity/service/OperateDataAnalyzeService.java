package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.OperateDataAnalyzeBO;

import java.util.List;

/**
 * @Description: OperateDataAnalyzeService
 * @Author: RenHang
 * @Date: 2025/02/19
 */

public interface OperateDataAnalyzeService {

    String queryLatestBatch();

    List<OperateDataAnalyzeBO> queryList(String batch);
}
