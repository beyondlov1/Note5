/*#######################################################
 *
 *   Maintained by Gregor Santner, 2018-
 *   https://gsantner.net/
 *
 *   License of this file: Apache 2.0 (Commercial upon request)
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.beyond.note5.view.markdown;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;

public class MarkdownHighlighter extends Highlighter {
    public final String _fontType;
    public final Integer _fontSize;
    private final boolean _highlightHexcolorEnabled;
    private final boolean _highlightLineEnding;
    private final boolean _highlightCodeChangeFont;

    private static final int MD_COLOR_HEADER = 0xffef6D00;
    private static final int MD_COLOR_LINK = 0xff1ea3fe;
    private static final int MD_COLOR_LIST = 0xffdaa521;
    private static final int MD_COLOR_QUOTE = 0xff88b04c;
    private static final int MD_COLOR_CODEBLOCK = 0xff8c8c8c;

    public MarkdownHighlighter() {
        _fontType = "san";
        _fontSize = 21;
        _highlightHexcolorEnabled = true;
        _highlightLineEnding =  true;
        _highlightCodeChangeFont =true;
    }

    @Override
    protected Editable run(final HighlightingEditor editor, final Editable editable) {
        try {
            clearSpans(editable);

            if (editable.length() == 0) {
                return editable;
            }

            //_profiler.restart("Header");
            createHeaderSpanForMatches(editable, MarkdownHighlighterPattern.HEADER, MD_COLOR_HEADER);
            //_profiler.restart("Link");
            createColorSpanForMatches(editable, MarkdownHighlighterPattern.LINK.pattern, MD_COLOR_LINK);
            //_profiler.restart("List");
            createColorSpanForMatches(editable, MarkdownHighlighterPattern.LIST_UNORDERED.pattern, MD_COLOR_LIST);
            //_profiler.restart("OrderedList");
            createColorSpanForMatches(editable, MarkdownHighlighterPattern.LIST_ORDERED.pattern, MD_COLOR_LIST);
            if (_highlightLineEnding) {
                //_profiler.restart("Double space ending - bgcolor");
                createColorBackgroundSpan(editable, MarkdownHighlighterPattern.DOUBLESPACE_LINE_ENDING.pattern, MD_COLOR_CODEBLOCK);
            }
            //_profiler.restart("Bold");
            createStyleSpanForMatches(editable, MarkdownHighlighterPattern.BOLD.pattern, Typeface.BOLD);
            //_profiler.restart("Italics");
            createStyleSpanForMatches(editable, MarkdownHighlighterPattern.ITALICS.pattern, Typeface.ITALIC);
            //_profiler.restart("Quotation");
            createColorSpanForMatches(editable, MarkdownHighlighterPattern.QUOTATION.pattern, MD_COLOR_QUOTE);
            //_profiler.restart("Strikethrough");
            createSpanWithStrikeThroughForMatches(editable, MarkdownHighlighterPattern.STRIKETHROUGH.pattern);
            if (_highlightCodeChangeFont) {
                //_profiler.restart("Code - Font [MonoSpace]");
                createMonospaceSpanForMatches(editable, MarkdownHighlighterPattern.CODE.pattern);
            }
            //_profiler.restart("Code - bgcolor");
            createColorBackgroundSpan(editable, MarkdownHighlighterPattern.CODE.pattern, MD_COLOR_CODEBLOCK);
            if (_highlightHexcolorEnabled) {
                //_profiler.restart("RGB Color underline");
                createColoredUnderlineSpanForMatches(editable, HexColorCodeUnderlineSpan.PATTERN, new HexColorCodeUnderlineSpan(), 1);
            }

        } catch (Exception ex) {
            // Ignoring errors
        }

        return editable;
    }

    private void createHeaderSpanForMatches(Editable editable, MarkdownHighlighterPattern pattern, int headerColor) {
    }

    @Override
    public InputFilter getAutoFormatter() {
        return new MarkdownAutoFormat();
    }

    @Override
    public int getHighlightingDelay(Context context) {
        return 270;
    }
}

