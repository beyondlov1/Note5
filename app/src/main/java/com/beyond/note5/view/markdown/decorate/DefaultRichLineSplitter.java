package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.bean.RichListLine;
import com.beyond.note5.view.markdown.decorate.resolver.OlRichLineResolver;

import java.util.ArrayList;
import java.util.List;

public class DefaultRichLineSplitter implements RichLineSplitter {

    @Override
    public List<RichLine> split(Editable s) {
        List<RichLine> lines = new ArrayList<>();
        char[] chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        int listIndex = 0;
        int lastIndex = 0;
        RichLine lastRichLine = null;
        int lineIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (aChar == '\n') {
                RichLine richLine = new RichLine(s, lastIndex, i + 1);
                if (RichListLine.isOlListLine(richLine.getContent()) || OlRichLineResolver.isListLine(richLine)) {
                    RichListLine listLine = new RichListLine(richLine);
                    listLine.setListIndex(listIndex);
                    richLine = listLine;
                    listIndex++;
                }

                richLine.setIndex(lineIndex);
                richLine.setPrev(lastRichLine);
                if (lastRichLine != null) {
                    lastRichLine.setNext(richLine);
                }
                lines.add(richLine);

                lineIndex++;
                lastIndex = i + 1;
                lastRichLine = richLine;
            }
        }
        RichLine richLine = new RichLine(s, lastIndex, chars.length);
        if (RichListLine.isOlListLine(richLine.getContent()) || OlRichLineResolver.isListLine(richLine)) {
            RichListLine listLine = new RichListLine(richLine);
            listLine.setListIndex(listIndex);
            richLine = listLine;
        }

        richLine.setIndex(lineIndex);
        richLine.setPrev(lastRichLine);
        if (lastRichLine != null) {
            lastRichLine.setNext(richLine);
        }
        lines.add(richLine);

        return lines;
    }
}
    