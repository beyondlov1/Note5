package com.beyond.note5.event;

public class SpeechRecognizedEvent extends AbstractEvent<String>{
    public SpeechRecognizedEvent(String s) {
        super(s);
    }
}
