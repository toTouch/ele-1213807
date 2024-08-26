package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
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
 * @Date: 2024/8/26 10:51
 */
@Service
@Slf4j
public class InstallmentRecordServiceImpl implements InstallmentRecordService {
    
    @Autowired
    private InstallmentRecordMapper installmentRecordMapper;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Override
    public Integer insert(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.insert(installmentRecord);
    }
    
    @Override
    public Integer update(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.update(installmentRecord);
    }
    
    @Slave
    @Override
    public R<List<InstallmentRecordVO>> listForPage(InstallmentRecordQuery installmentRecordQuery) {
        List<InstallmentRecord> installmentRecords = installmentRecordMapper.selectPage(installmentRecordQuery);
        
        List<InstallmentRecordVO> installmentRecordVos = installmentRecords.parallelStream().map(installmentRecord -> {
            InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
            BeanUtils.copyProperties(installmentRecord, installmentRecordVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(installmentRecord.getFranchiseeId());
            installmentRecordVO.setFranchiseeName(franchisee.getName());
            
            // 设置电或者车的套餐名称
            String packageName;
            if (Objects.equals(installmentRecord.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                packageName = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId()).getName();
            } else {
                packageName = carRentalPackageService.selectById(installmentRecord.getPackageId()).getName();
            }
            installmentRecordVO.setPackageName(packageName);
            
            return installmentRecordVO;
        }).collect(Collectors.toList());
        
        return R.ok(installmentRecordVos);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentRecordQuery installmentRecordQuery) {
        return R.ok(installmentRecordMapper.count(installmentRecordQuery));
    }
}
