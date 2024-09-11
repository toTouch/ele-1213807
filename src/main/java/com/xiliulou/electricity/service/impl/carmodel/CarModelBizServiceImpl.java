package com.xiliulou.electricity.service.impl.carmodel;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.PictureQuery;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.PictureService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.carmodel.CarModelBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.PictureVO;
import com.xiliulou.electricity.vo.StoreVO;
import com.xiliulou.electricity.vo.car.CarModelDetailVo;
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
    private CarRenalPackageDepositBizService carRenalPackageDepositBizService;

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
    
    @Resource
    private UserInfoService userInfoService;

    /**
     * 根据车辆型号ID获取车辆型号信息<br />
     * 包含：基本信息、图片信息、门店信息
     *
     * @param carModelId 车辆型号ID
     * @return 车辆型号详细信息
     */
    @Override
    public CarModelDetailVo queryDetailByCarModelId(Integer carModelId) {
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
        PictureQuery query = PictureQuery.builder().tenantId(TenantContextHolder.getTenantId()).businessId(Long.valueOf(carModel.getId())).imgType(Picture.TYPE_CAR_IMG).build();
        List<Picture> pictures = pictureService.queryListByQuery(query);
        if (!CollectionUtils.isEmpty(pictures)) {
            List<PictureVO> pictureVOList = pictureService.pictureParseVO(pictures);
            if (!CollectionUtils.isEmpty(pictureVOList)) {
                pictureUrls = pictureVOList.stream().map(PictureVO::getPictureOSSUrl).collect(Collectors.toList());
            }
        }

        // 查询门店信息
        Store store = storeService.queryByIdFromCache(carModel.getStoreId());

        // 拼装返回数据
        CarModelDetailVo carModelDetailVo = new CarModelDetailVo();
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
            log.warn("CarModelBizService.checkBuyByCarModelId, not found t_electricity_car_model or tenantId mismatch. ");
            throw new BizException("300000", "数据有误");
        }

        // 2. 查询是否存在可租的车辆
        boolean unleasedCarFlag = carService.checkUnleasedByCarModelId(carModelId);
        if (!unleasedCarFlag) {
            ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
            if (ObjectUtils.isEmpty(electricityCar)) {
                log.warn("CarModelBizService.checkBuyByCarModelId, There are no rental vehicles available. carModelId is {}", carModelId);
                throw new BizException("300043", "无可租车辆");
            }
            if (ObjectUtils.isNotEmpty(electricityCar) && !electricityCar.getModelId().equals(carModelId)) {
                log.warn("CarModelBizService.checkBuyByCarModelId, User vehicle model mismatch. carModelId is {}, user car_model_id is {}", carModelId, electricityCar.getModelId());
                throw new BizException("300043", "无可租车辆");
            }
        }


        // 3. 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(memberTermEntity) &&
                !(MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus()) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus()))) {
            log.info("CarModelBizService.checkBuyByCarModelId, The t_car_rental_package_member_term abnormal status. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        // 4. 获取车辆型号、押金、套餐类型
        Integer carModelIdExit = carModelId;
        BigDecimal depositExit = null;
        Integer rentalPackageTypeExit = null;
        Integer confineExit = null;
        Integer franchiseeIdExit = null;
        Integer storeIdExit = null;
        Integer freeDepositExit = null;
        
        //获取用户信息，查看用户押金缴纳状况。 如果已缴纳过单车押金或者车电一体押金，则判断所选车辆型号和套餐对应的车辆型号是否一致
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        boolean depositPaid = UserInfo.CAR_DEPOSIT_STATUS_YES.equals(userInfo.getCarDepositStatus()) || YesNoEnum.YES.getCode().equals(userInfo.getCarBatteryDepositStatus());
        
        if (ObjectUtils.isNotEmpty(memberTermEntity) && depositPaid) {
            Long rentalPackageId = memberTermEntity.getRentalPackageId();
            if (ObjectUtils.isEmpty(rentalPackageId) || rentalPackageId == 0) {
                String depositPayOrderNo = memberTermEntity.getDepositPayOrderNo();
                rentalPackageId = carRenalPackageDepositBizService.queryRentalPackageIdByDepositPayOrderNo(depositPayOrderNo);
            }
            CarRentalPackagePo rentalPackage = carRentalPackageService.selectById(rentalPackageId);
            if (ObjectUtils.isEmpty(rentalPackage)) {
                log.warn("CarModelBizService.checkBuyByCarModelId, not found t_car_rental_package. rentalPackageId is {}", rentalPackageId);
                throw new BizException("300000", "数据有误");
            }
            if (!carModelId.equals(rentalPackage.getCarModelId())) {
                log.warn("CarModelBizService.checkBuyByCarModelId, Vehicle model mismatch. rentalPackage carModelId is {}, request carModelId is {}", rentalPackage.getCarModelId(), carModelId);
                throw new BizException("300056", "车辆型号不匹配");
            }
            confineExit = rentalPackage.getConfine();
            freeDepositExit = rentalPackage.getFreeDeposit();
            depositExit = memberTermEntity.getRentalPackageDeposit();
            rentalPackageTypeExit = memberTermEntity.getRentalPackageType();
            franchiseeIdExit = memberTermEntity.getFranchiseeId();
            storeIdExit = memberTermEntity.getStoreId();
        }

        // 5. 查询是否存在此型号、押金、套餐类型一致的上架套餐
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setFranchiseeId(franchiseeIdExit);
        qryModel.setStoreId(storeIdExit);
        qryModel.setCarModelId(carModelIdExit);
        qryModel.setStatus(UpDownEnum.UP.getCode());
        qryModel.setConfine(confineExit);
        //qryModel.setFreeDeposit(freeDepositExit);
        if (ObjectUtils.isNotEmpty(depositExit)) {
            qryModel.setDeposit(depositExit);
        }
        if (ObjectUtils.isNotEmpty(rentalPackageTypeExit)) {
            qryModel.setType(rentalPackageTypeExit);
        }
        Integer count = carRentalPackageService.count(qryModel);

        if (count == 0) {
            throw new BizException("300044", "无可用套餐");
        }


        return count > 0;
    }
}
