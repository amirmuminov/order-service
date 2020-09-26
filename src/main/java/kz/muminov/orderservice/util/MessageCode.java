package kz.muminov.orderservice.util;

public enum MessageCode {

    ORDER_DOES_NOT_EXIST(1, "Order does not exist"),
    ORDER_STATUS_NOT_NOT_PAYED(2, "Order has not status NOT_PAYED"),
    ORDER_STATUS_IS_PAYED(3, "Order has status PAYED");

    int errorCode;
    private String defaultMessage;

    MessageCode(int code, String defaultMessage){
        this.errorCode = code;
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
