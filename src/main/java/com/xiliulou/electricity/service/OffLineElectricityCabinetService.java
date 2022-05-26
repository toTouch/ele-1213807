package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;

/**
 * 离线换电Service
 *
 * @author HRP
 * @since 2022-03-02 14:03:36
 */
public interface OffLineElectricityCabinetService {

    R generateVerificationCode();

    R frontDetection();

}
