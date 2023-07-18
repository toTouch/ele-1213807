package com.xiliulou.electricity.service.impl.car;

import com.alibaba.fastjson.JSON;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.ElectricityCabinetApplication;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;

@SpringBootTest(classes = ElectricityCabinetApplication.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class CarRentalPackageServiceImplTest {

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Test
    public void updateStatusById() {
    }

    @Test
    public void delById() {
    }

    @Test
    public void list() {
    }

    @Test
    public void count() {
        CarRentalPackageQryModel queryModel = new CarRentalPackageQryModel();
//        R<Integer> count = carRentalPackageService.count(queryModel);
//        System.err.println(JSON.toJSONString(count));
    }

    @Test
    public void selectById() {
    }

    @Test
    public void updateById() {
    }

    @Test
    public void insert() {
        CarRentalPackageOptModel optModel = new CarRentalPackageOptModel();
        optModel.setTenantId(1);
        optModel.setFranchiseeId(2);
        optModel.setStoreId(3);
        optModel.setCreateUid(4L);
        optModel.setUpdateUid(4L);
        long now = System.currentTimeMillis();
        optModel.setCreateTime(now);
        optModel.setUpdateTime(now);
        optModel.setName("测试新增租车套餐");
        optModel.setType(CarRentalPackageTypeEnum.CAR.getCode());
        optModel.setTenancy(30);
        optModel.setTenancyUnit(RentalUnitEnum.DAY.getCode());
        optModel.setRent(new BigDecimal("90"));
        optModel.setDeposit(new BigDecimal("99"));
        optModel.setCarModelId(1);
//        optModel.setBatteryModelIds("1,2,3");
        optModel.setApplicableType(ApplicableTypeEnum.ALL.getCode());
        optModel.setRentRebate(YesNoEnum.YES.getCode());
        optModel.setRentRebateTerm(7);
        optModel.setDepositExemption(DepositExemptionEnum.NO.getCode());
//        optModel.setDepositRebateApprove(YesNoEnum.YES.getCode());
        optModel.setRentUnitPrice(new BigDecimal("3"));
        optModel.setLateFee(new BigDecimal("5"));
        optModel.setConfine(RenalPackageConfineEnum.NO.getCode());
        optModel.setGiveCoupon(YesNoEnum.NO.getCode());
        optModel.setStatus(UpDownEnum.DOWN.getCode());
        optModel.setRemark("这是备注展示字段");

//        R<Long> insert = carRentalPackageService.insert(optModel);
//        System.err.println(JSON.toJSONString(insert));
    }
}