package com.xiliulou.electricity.controller.user.recommend;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.recommend.CarModelRecommendQryReq;
import com.xiliulou.electricity.service.recommend.RecommendBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.recommend.CarModelRecommendVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户推荐 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/recommend")
public class JsonUserRecommendController extends BasicController {

    @Resource
    private RecommendBizService recommendBizService;

    /**
     * 根据经纬度，系统用车推荐
     * @param carModelRecommendQryReq 查询模型
     * @return 推荐车辆型号集
     */
    @PostMapping("/carModelByDistance")
    public R<List<CarModelRecommendVO>> carModelByDistance(@RequestBody @Valid CarModelRecommendQryReq carModelRecommendQryReq) {
        if (!ObjectUtils.allNotNull(carModelRecommendQryReq, carModelRecommendQryReq.getFranchiseeId(),
                carModelRecommendQryReq.getLatitude(), carModelRecommendQryReq.getLongitude(), carModelRecommendQryReq.getDistance())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        carModelRecommendQryReq.setTenantId(tenantId);

        List<CarModelRecommendVO> recommendVoList = recommendBizService.carModelByDistance(carModelRecommendQryReq);

        return R.ok(recommendVoList);
    }

}
