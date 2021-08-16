public enum ReturnOrderStatus {
    /**
     * 还电池前置检测
     */
    RETURN_INIT_CHECK(1.1),
    /**
     * 还电池仓内有电池
     */
    RETURN_BATTERY_EXISTS(1.2),

    /**
     * 还电池开门成功
     */
    RETURN_OPEN_SUCCESS(2.0),
    /**
     * 还电池开门失败
     */
    RETURN_OPEN_FAIL(2.1),
    /**
     * 还电池成功
     */
    RETURN_BATTERY_CHECK_SUCCESS(3.0),
    /**
     * 还电池检测超时
     */
    RETURN_BATTERY_CHECK_TIMEOUT(3.2),
    /**
     * 还电池检测失败
     */
    RETURN_BATTERY_CHECK_FAIL(3.1);


    ReturnOrderStatus(Double statusSeq) {
        this.statusSeq = statusSeq;
    }

    private final Double statusSeq;

    public Double getStatusSeq() {
        return statusSeq;
    }
}
