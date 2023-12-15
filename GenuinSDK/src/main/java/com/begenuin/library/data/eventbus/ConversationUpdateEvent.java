package com.begenuin.library.data.eventbus;

public class ConversationUpdateEvent {

    public boolean isRT;

    public ConversationUpdateEvent(boolean isRT){
        this.isRT = isRT;
    }
}
