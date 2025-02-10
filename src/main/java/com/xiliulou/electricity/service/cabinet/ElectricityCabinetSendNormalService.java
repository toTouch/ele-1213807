package com.xiliulou.electricity.service.cabinet;

import com.xiliulou.electricity.task.cabinet.ElectricityCabinetSendNormalTask;

public interface ElectricityCabinetSendNormalService {
    void sendNormalCommand(ElectricityCabinetSendNormalTask.SendNormalTaskParam taskParam);
}
