package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({MessageStatus.SENT, MessageStatus.DELIVERED, MessageStatus.READ, MessageStatus.ERROR})
public @interface MessageStatus {
    String SENT = "SENT";
    String DELIVERED = "DELIVERED";
    String READ = "READ";
    String ERROR = "ERROR";
}
