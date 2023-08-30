package com.xiliulou.electricity.service.recommend;

import com.xiliulou.electricity.query.recommend.CarModelRecommendQryReq;
import com.xiliulou.electricity.vo.recommend.CarModelRecommendVO;

import java.util.List;

/**
 * 推荐业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface RecommendBizService {

    /**
     * 根据经纬度，系统用车推荐
     * @param carModelRecommendQryReq 查询模型
     * @return 推荐车辆型号集
     */
    List<CarModelRecommendVO> carModelByDistance(CarModelRecommendQryReq carModelRecommendQryReq);

}
