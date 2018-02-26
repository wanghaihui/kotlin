package com.imagine.story.common.websocket;

class CounterPayloadGenerator implements PayloadGenerator {
    private long mCount;

    @Override
    public byte[] generate() {
        return Misc.getBytesUTF8(String.valueOf(increment()));
    }

    private long increment() {
        // Increment the counter.
        mCount = Math.max(mCount + 1, 1);
        return mCount;
    }
}
