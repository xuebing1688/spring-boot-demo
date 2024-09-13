package com.example.common.service;

@FunctionalInterface
public interface BranchHandle {

    void trueOrFalseHandle(Runnable trueHandle, Runnable falseHandle);




}
