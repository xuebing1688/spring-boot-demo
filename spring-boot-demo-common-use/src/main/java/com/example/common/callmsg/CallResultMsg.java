package com.example.common.callmsg;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallResultMsg<T> {
    private boolean result;
    private int code;
    private String message;
    private T data;

}
