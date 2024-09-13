package com.example.common.util;


import com.example.common.service.BranchHandle;
import com.example.common.service.PresentOrElseHandler;
import com.example.common.service.ThrowExceptionFunction;

public class VUtils {


    public static ThrowExceptionFunction isTure(boolean b) {

        return (errorMessage) -> {
            if (b) {
                throw new RuntimeException(errorMessage);
            }
        };

    }

    public static BranchHandle isTureOrFalse(boolean b){

        return (trueHandle, falseHandle) -> {
            if (b){
                trueHandle.run();
            } else {
                falseHandle.run();
            }
        };
    }


    public static PresentOrElseHandler<?> isBlankOrNoBlank(String str){

        return (consumer, runnable) -> {
            if (str == null || str.length() == 0){
                runnable.run();
            } else {
                consumer.accept(str);
            }
        };
    }




}
