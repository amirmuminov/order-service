package kz.muminov.orderservice.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class DefaultException extends RuntimeException {

    private final String message;
    private int errorCode;
    private final int status;

    public DefaultException(String message, int errorCode) {
        super(message);
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST.value();
        this.errorCode = errorCode;
    }
}
