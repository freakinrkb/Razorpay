package com.example.razorpay.common.exception;

public class InvalidStateTransitionException extends RuntimeException{

    public InvalidStateTransitionException(String fromState, String event) {
        super("Invalid transition from " + fromState + " with event " + event);
    }
}
