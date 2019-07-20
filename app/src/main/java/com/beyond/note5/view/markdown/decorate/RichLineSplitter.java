package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public interface RichLineSplitter {
    List<RichLine> split(Editable fullSource);
}
