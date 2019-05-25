package com.beyond.note5.event;

import com.beyond.note5.bean.Document;

public class AfterFloatEditorSavedEvent extends AbstractEvent<Document> {
    public AfterFloatEditorSavedEvent(Document document) {
        super(document);
    }
}
