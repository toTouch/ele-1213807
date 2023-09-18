package com.xiliulou.electricity.service.impl.recommend;

import com.google.common.collect.Lists;
import com.xiliulou.electricity.entity.CarModelTag;
import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.PictureQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.query.recommend.CarModelRecommendQryReq;
import com.xiliulou.electricity.service.CarModelTagService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.PictureService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.recommend.RecommendBizService;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;
import com.xiliulou.electricity.vo.PictureVO;
import com.xiliulou.electricity.vo.StoreVO;
import com.xiliulou.electricity.vo.recommend.CarModelRecommendVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 推荐业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class RecommendBizServiceImpl implements RecommendBizService {

    @Resource
    private CarModelTagService carModelTagService;

    @Resource
    private PictureService pictureService;

    @Resource
    private ElectricityCarModelService carModelService;

    @Resource
    private StoreService storeService;

    /**
     * 根据经纬度，系统用车推荐
     * @param carModelRecommendQryReq 查询模型
     * @return 推荐车辆型号集
     */
    @Override
    public List<CarModelRecommendVO> carModelByDistance(CarModelRecommendQryReq carModelRecommendQryReq) {
        if (!ObjectUtils.allNotNull(carModelRecommendQryReq, carModelRecommendQryReq.getTenantId(), carModelRecommendQryReq.getFranchiseeId(),
                carModelRecommendQryReq.getLatitude(), carModelRecommendQryReq.getLongitude(), carModelRecommendQryReq.getDistance())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 根据加盟商，寻找范围内的所有门店
        StoreQuery storeQuery = new StoreQuery();
        storeQuery.setFranchiseeId(Long.valueOf(carModelRecommendQryReq.getFranchiseeId()));
        storeQuery.setLon(carModelRecommendQryReq.getLongitude());
        storeQuery.setLat(carModelRecommendQryReq.getLatitude());
        storeQuery.setQueryPictureFlag(false);
        storeQuery.setDistance(carModelRecommendQryReq.getDistance());
        List<StoreVO> storeVoList = storeService.selectListByDistance(storeQuery);
        if (CollectionUtils.isEmpty(storeVoList)) {
            log.info("RecommendBizService.carModelByDistance, storeVoList is empty.");
            return Collections.emptyList();
        }

        // 门店 MAP
        Map<Long, StoreVO> storeMap = storeVoList.stream().collect(Collectors.toMap(StoreVO::getId, Function.identity(), (k1, k2) -> k1));

        // 提取门店ID集
        List<Long> storeIdList = storeVoList.stream().map(StoreVO::getId).collect(Collectors.toList());

        // 获取车辆型号
        List<ElectricityCarModelVO> carModelList = carModelService.selectByStoreIdListLimit(10, storeIdList);
        if (CollectionUtils.isEmpty(carModelList)) {
            log.info("RecommendBizService.carModelByDistance, carModelList is empty.");
            return Collections.emptyList();
        }

        // 提取及转换车辆型号ID集
        List<Integer> carModelIntegerIds = carModelList.stream().map(ElectricityCarModelVO::getId).collect(Collectors.toList());
        List<Long> carModelLongIds = carModelIntegerIds.stream().map(Long::valueOf).collect(Collectors.toList());

        // 获取车辆型号图片
        Map<Long, List<Picture>> pictureMap = null;
        PictureQuery pictureQuery = new PictureQuery();
        pictureQuery.setStatus(Picture.STATUS_ENABLE);
        pictureQuery.setImgType(Picture.TYPE_CAR_IMG);
        pictureQuery.setDelFlag(Picture.DEL_NORMAL);
        pictureQuery.setTenantId(carModelRecommendQryReq.getTenantId());
        pictureQuery.setBusinessIdList(carModelLongIds);
        List<Picture> pictures = pictureService.queryListByQuery(pictureQuery);
        if (!CollectionUtils.isEmpty(pictures)) {
            pictureMap = pictures.stream().collect(Collectors.groupingBy(Picture::getBusinessId));
        }

        // 获取车辆型号标签
        Map<Long, List<CarModelTag>> carModelTagMap = null;
        List<CarModelTag> carModelTags = carModelTagService.selectByCarModelIds(carModelIntegerIds);
        if(!CollectionUtils.isEmpty(carModelTags)){
            carModelTagMap = carModelTags.stream().collect(Collectors.groupingBy(CarModelTag::getCarModelId));
        }

        // 拼装返回数据
        List<CarModelRecommendVO> carModelRecommendVoList = new ArrayList<>();
        CarModelRecommendVO carModelRecommendVo = null;
        for (ElectricityCarModelVO carModelVo : carModelList) {
            carModelRecommendVo = new CarModelRecommendVO();
            carModelRecommendVo.setCarModelId(carModelVo.getId());
            carModelRecommendVo.setCarModelName(carModelVo.getName());
            carModelRecommendVo.setCarModelRentedQuantity(carModelVo.getRentedQuantity());
            carModelRecommendVo.setCarModelCreateTime(carModelVo.getCreateTime());
            carModelRecommendVo.setStoreId(carModelVo.getStoreId().intValue());
            carModelRecommendVo.setStoreName(storeMap.getOrDefault(carModelVo.getStoreId(), new StoreVO()).getName());
            carModelRecommendVo.setStoreDistance(storeMap.getOrDefault(carModelVo.getStoreId(), new StoreVO()).getDistance());

            // 赋值图片信息
            List<Picture> pictureList = pictureMap.getOrDefault(Long.valueOf(carModelVo.getId()), Collections.emptyList());
            List<PictureVO> pictureVoList = pictureService.pictureParseVO(pictureList);
            if (!CollectionUtils.isEmpty(pictureVoList)) {
                carModelRecommendVo.setCarModelPictureOSSUrl(pictureVoList.get(0).getPictureOSSUrl());
            }

            // 赋值标签信息
            List<CarModelTag> carModelTagList = carModelTagMap.getOrDefault(Long.valueOf(carModelVo.getId()), Collections.emptyList());
            if (!CollectionUtils.isEmpty(carModelTagList)) {
                // 兼容老数据，最多四个
                List<String> carModelTagNames = null;
                if (carModelTagList.size() > 4) {
                    List<List<CarModelTag>> partitionList = Lists.partition(carModelTagList, 4);
                    carModelTagNames = partitionList.get(0).stream().map(CarModelTag::getTitle).collect(Collectors.toList());
                } else {
                    carModelTagNames = carModelTagList.stream().map(CarModelTag::getTitle).collect(Collectors.toList());
                }
                carModelRecommendVo.setCarModelTagNames(carModelTagNames);
            }

            carModelRecommendVoList.add(carModelRecommendVo);
        }

        if (!CollectionUtils.isEmpty(carModelRecommendVoList)) {
            // 二次排序
            carModelRecommendVoList.sort(Comparator.comparing(CarModelRecommendVO::getCarModelRentedQuantity)
                    .thenComparing(CarModelRecommendVO::getStoreDistance)
                    .thenComparing(CarModelRecommendVO::getCarModelCreateTime, Comparator.reverseOrder()));
        }

        return carModelRecommendVoList;

    }
}
