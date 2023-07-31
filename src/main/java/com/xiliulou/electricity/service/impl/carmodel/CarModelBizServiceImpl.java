package com.xiliulou.electricity.service.impl.carmodel;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.PictureService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.carmodel.CarModelBizService;
import com.xiliulou.electricity.vo.PictureVO;
import com.xiliulou.electricity.vo.StoreVO;
import com.xiliulou.electricity.vo.car.CarModelDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆型号业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarModelBizServiceImpl implements CarModelBizService {

    @Resource
    private StoreService storeService;

    @Resource
    private PictureService pictureService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private ElectricityCarModelService carModelService;

    /**
     * 根据车辆型号ID获取车辆型号信息<br />
     * 包含：基本信息、图片信息、门店信息
     *
     * @param carModelId 车辆型号ID
     * @return 车辆型号详细信息
     */
    @Override
    public CarModelDetailVO queryDetailByCarModelId(Integer carModelId) {
        if (ObjectUtils.isEmpty(carModelId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询车辆型号基本信息
        ElectricityCarModel carModel = carModelService.queryByIdFromCache(carModelId);
        if (ObjectUtils.isEmpty(carModel)) {
            log.info("CarModelBizService.queryDetailByCarModelId, not found t_electricity_car_model. carModelId is {}", JsonUtil.toJson(carModel));
            return null;
        }

        // 查询车辆型号图片信息
        List<String> pictureUrls = null;
        List<Picture> pictures = pictureService.selectByByBusinessId(Long.valueOf(carModel.getId()));
        if (!CollectionUtils.isEmpty(pictures)) {
            List<PictureVO> pictureVOList = pictureService.pictureParseVO(pictures);
            if (!CollectionUtils.isEmpty(pictureVOList)) {
                pictureUrls = pictureVOList.stream().map(PictureVO::getPictureOSSUrl).collect(Collectors.toList());
            }
        }

        // 查询门店信息
        Store store = storeService.queryByIdFromCache(carModel.getStoreId());

        // 拼装返回数据
        CarModelDetailVO carModelDetailVo = new CarModelDetailVO();
        carModelDetailVo.setId(carModel.getId());
        carModelDetailVo.setName(carModel.getName());
        carModelDetailVo.setOtherProperties(carModel.getOtherProperties());

        // 赋值车辆型号图片信息
        carModelDetailVo.setPictureUrls(pictureUrls);

        // 赋值门店信息
        if (ObjectUtils.isNotEmpty(store)) {
            StoreVO storeVo = new StoreVO();
            storeVo.setId(store.getId());
            storeVo.setName(store.getName());
            storeVo.setAddress(store.getAddress());
            storeVo.setLongitude(store.getLongitude());
            storeVo.setLatitude(store.getLatitude());
            carModelDetailVo.setStore(storeVo);
        }

        return carModelDetailVo;
    }

    /**
     * 检测是否允许购买此车辆型号
     *
     * @param tenantId   租户ID
     * @param uid        用户ID
     * @param carModelId 车辆型号ID
     * @return true(允许)、false(不允许)
     */
    @Override
    public boolean checkBuyByCarModelId(Integer tenantId, Long uid, Integer carModelId) {
        if (!ObjectUtils.allNotNull(tenantId, uid, carModelId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 1. 查询车辆型号是否存在
        ElectricityCarModel carModel = carModelService.queryByIdFromCache(carModelId);
        if (ObjectUtils.isEmpty(carModel) || !tenantId.equals(carModel.getTenantId())) {
            log.error("CarModelBizService.checkBuyByCarModelId, not found t_electricity_car_model or tenantId mismatch. ");
            throw new BizException("300000", "数据有误");
        }

        // 用户名下没有绑定车辆的时候
        ElectricityCar electricityCar = carService.queryInfoByUid(uid);
        if (ObjectUtils.isEmpty(electricityCar)) {
            // 2. 查询是否存在可租的车辆
            boolean unleasedCarFlag = carService.checkUnleasedByCarModelId(carModelId);
            if (!unleasedCarFlag) {
                log.error("CarModelBizService.checkBuyByCarModelId, There are no rental vehicles available. carModelId is {}", carModelId);
                return false;
            }
        }

        // 3. 查询租车会员信息
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(memberTermEntity) &&
                !(MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus()) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus()))) {
            log.info("CarModelBizService.checkBuyByCarModelId, The t_car_rental_package_member_term abnormal status. uid is {}", uid);
            return false;
        }

        // 4. 获取车辆型号、押金、套餐类型
        Integer carModelIdExit = carModelId;
        BigDecimal depositExit = null;
        Integer rentalPackageTypeExit = null;
        if (ObjectUtils.isNotEmpty(memberTermEntity)) {
            CarRentalPackagePO rentalPackage = carRentalPackageService.selectById(memberTermEntity.getRentalPackageId());
            if (ObjectUtils.isEmpty(rentalPackage)) {
                log.error("CarModelBizService.checkBuyByCarModelId, not found t_car_rental_package. rentalPackageId is {}", memberTermEntity.getRentalPackageId());
                throw new BizException("300000", "数据有误");
            }
            carModelIdExit = rentalPackage.getCarModelId();
            depositExit = memberTermEntity.getDeposit();
            rentalPackageTypeExit = memberTermEntity.getRentalPackageType();
        }

        // 5. 查询是否存在此型号、押金、套餐类型一致的上架套餐
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setCarModelId(carModelIdExit);
        qryModel.setStatus(UpDownEnum.UP.getCode());
        if (ObjectUtils.isNotEmpty(depositExit)) {
            qryModel.setDeposit(depositExit);
        }
        if (ObjectUtils.isNotEmpty(rentalPackageTypeExit)) {
            qryModel.setType(rentalPackageTypeExit);
        }
        Integer count = carRentalPackageService.count(qryModel);

        return count > 0;
    }
}
