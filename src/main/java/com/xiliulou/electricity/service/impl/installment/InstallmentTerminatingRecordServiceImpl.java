package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentTerminatingRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:52
 */
@Service
@Slf4j
public class InstallmentTerminatingRecordServiceImpl implements InstallmentTerminatingRecordService {
    
    @Autowired
    private InstallmentTerminatingRecordMapper installmentTerminatingRecordMapper;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Override
    public Integer insert(InstallmentTerminatingRecord installmentTerminatingRecord) {
        return installmentTerminatingRecordMapper.insert(installmentTerminatingRecord);
    }
    
    @Override
    public Integer update(InstallmentTerminatingRecord installmentTerminatingRecord) {
        return installmentTerminatingRecordMapper.update(installmentTerminatingRecord);
    }
    
    @Slave
    @Override
    public R<List<InstallmentTerminatingRecordVO>> listForPage(InstallmentTerminatingRecordQuery query) {
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordMapper.selectPage(query);
        
        List<InstallmentTerminatingRecordVO> collect = records.parallelStream().map(installmentTerminatingRecord -> {
            InstallmentTerminatingRecordVO vo = new InstallmentTerminatingRecordVO();
            BeanUtils.copyProperties(installmentTerminatingRecord, vo);
            
            vo.setFranchiseeName(franchiseeService.queryByIdFromCache(installmentTerminatingRecord.getFranchiseeId()).getName());
            
            // 设置电或者车的套餐名称
            String packageName;
            if (Objects.equals(installmentTerminatingRecord.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                packageName = batteryMemberCardService.queryByIdFromCache(installmentTerminatingRecord.getPackageId()).getName();
            } else {
                packageName = carRentalPackageService.selectById(installmentTerminatingRecord.getPackageId()).getName();
            }
            vo.setPackageName(packageName);
            return vo;
        }).collect(Collectors.toList());
        
        return R.ok(collect);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentTerminatingRecordQuery query) {
        return R.ok(installmentTerminatingRecordMapper.count(query));
    }
    
    @Override
    public String agreementPayNotify(String bizContent, Long uid) {
        
        return "SUCCESS";
    }
}
