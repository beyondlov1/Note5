package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

import com.beyond.note5.view.markdown.decorate.resolver.OlRichLineResolver;

import java.util.ArrayList;
import java.util.List;

public class DefaultRichLineSplitter implements RichLineSplitter{

        @Override
        public List<RichLine> split(Editable s) {
            List<RichLine> lines = new ArrayList<>();
            char[] chars = new char[s.length()];
            s.getChars(0,s.length(),chars,0);
            int listIndex = 0;
            int lastIndex = 0;
            for (int i = 0; i < chars.length; i++) {
                char aChar = chars[i];
                if (aChar == '\n'){
                    RichLine richLine = new RichLine(s, lastIndex, i + 1);
                    if (OlRichLineResolver.isListLine(richLine)){
                        listIndex ++;
                    }
                    if (RichListLine.isListLine(richLine.getContent())){
                        RichListLine listLine = new RichListLine(richLine);
                        listLine.setListIndex(listIndex);
                        lines.add(listLine);
                        listIndex ++;
                    }else {
                        lines.add(richLine);
                    }
                    lastIndex = i+1;
                }
            }
            RichLine richLine = new RichLine(s, lastIndex, chars.length);
            if (RichListLine.isListLine(richLine.getContent())){
                RichListLine listLine = new RichListLine(richLine);
                listLine.setListIndex(listIndex);
                lines.add(listLine);
            }else {
                lines.add(richLine);
            }
            return lines;
        }
    }
    