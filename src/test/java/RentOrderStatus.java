public enum RentOrderStatus {
    /**
     * 租电池前置检测
     */
    RENT_INIT_CHECK(1.1),
    /**
     * 租电池格挡是空仓
     */
    RENT_BATTERY_NOT_EXISTS(1.2),

    /**
     * 租电池开门成功
     */
    RENT_OPEN_SUCCESS(2.0),
    /**
     * 租电池开门失败
     */
    RENT_OPEN_FAIL(2.1),
    /**
     * 租电池成功取走
     */
    RENT_BATTERY_TAKE_SUCCESS(3.0),
    /**
     * 租电池超时
     */
    RENT_BATTERY_TAKE_TIMEOUT(3.1);


    RentOrderStatus(Double statusSeq) {
        this.statusSeq = statusSeq;
    }

    private final Double statusSeq;

    public Double getStatusSeq() {
        return statusSeq;
    }
}
