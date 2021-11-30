package com.xiliulou.electricity.query;

import com.xiliulou.electricity.entity.Store;
import lombok.Data;

import java.util.List;

/**
 * @author zgw
 * @date 2021/11/29 16:09
 * @mood
 */
@Data
public class StoreElectricityCabinetQuery {
    private Store store;
    private List<Integer> electricityCabinetId;
}
