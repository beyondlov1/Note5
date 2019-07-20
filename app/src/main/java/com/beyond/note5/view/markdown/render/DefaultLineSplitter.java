package com.beyond.note5.view.markdown.render;

import com.beyond.note5.view.markdown.render.bean.Line;
import com.beyond.note5.view.markdown.render.bean.ListLine;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class DefaultLineSplitter implements LineSplitter {

    private String separator = "\n";
    private boolean isStartWithSeparator = false;
    private boolean isEndWithSeparator = false;
    private int count;


    @Override
    public void init() {

    }

    @Override
    public List<Line> split(String source) {
        String[] lineSources = StringUtils.split(source,separator);
        List<Line> lines = new ArrayList<>(lineSources.length);
        int index = 0;
        int listIndex = 0;
        for (String lineSource : lineSources) {
            Line line;
            if (ListLine.isListLine(lineSource)){
                ListLine listLine = new ListLine(lineSource);
                listLine.setListIndex(listIndex);
                line = listLine;
                listIndex++;
            }else {
                line = new Line(lineSource);
            }
            line.setIndex(index);
            lines.add(line);
            index++;
        }
        isStartWithSeparator = StringUtils.startsWith(source,separator);
        isEndWithSeparator = StringUtils.endsWith(source,separator);
        count = lines.size();
        return lines;
    }

    @Override
    public boolean shouldInsertSeparator(Line line) {
        return line.getIndex() == 0 && isStartWithSeparator;
    }


    @Override
    public boolean shouldAppendSeparator(Line line) {
        return !(line.getIndex() == count-1 && !isEndWithSeparator);
    }

    @Override
    public String getSeparator() {
        return separator;
    }
}
