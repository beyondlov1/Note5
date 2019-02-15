package com.beyond.note5.constant;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;

import java.util.Objects;

public enum ReadFlagEnum {
    STICK(-1, R.string.note_read_flag_stick),NORMAL(0, R.string.note_read_flag_normal), DONE(1,R.string.note_read_flag_done);

    Integer code;
    Integer nameResId;

    ReadFlagEnum(Integer code, Integer nameResId) {
        this.code = code;
        this.nameResId = nameResId;
    }

    public static String getName(Integer code) {
        ReadFlagEnum[] values = values();
        for (ReadFlagEnum value : values) {
            if (Objects.equals(value.code, code)){
                return MyApplication.getInstance().getResources().getString(value.nameResId);
            }
        }
        return null;
    }
}
