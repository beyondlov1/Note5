package com.beyond.note5.view.fragment;

import com.beyond.note5.constant.LoadType;

public interface DetailStage {
    void load(LoadType loadType);
    void refresh(LoadType loadType);
    void clear();
}
