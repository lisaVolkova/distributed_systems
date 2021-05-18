package com.example1.loggingservice.loggingservice;

import java.util.UUID;

public class Message {
    private UUID mUUID;
    private String mText;

    public Message(UUID uuid, String text) {
        mText = text;
        mUUID = uuid;
    }

    @Override
    public String toString() {
        return "Message{" +
                "mUUID=" + mUUID +
                ", mText='" + mText + '\'' +
                '}';
    }

    public UUID getUUID() {
        return mUUID;
    }

    public void setUUID(UUID uuid) {
        mUUID = uuid;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
