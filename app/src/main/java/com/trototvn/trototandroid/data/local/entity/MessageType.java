package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({ MessageType.TEXT, MessageType.IMAGE, MessageType.FILE })
public @interface MessageType {
    String TEXT = "TEXT";
    String IMAGE = "IMAGE";
    String FILE = "FILE";
}
