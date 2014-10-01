package com.prowritingaid.client;

public enum WritingStyle {
        NotSet(0),
        General(1),
        Academic(2),
        Business(3),
        Technical(4),
        Creative(5),
        Casual(6),
        Web(7);
        
        private final int value;

        private WritingStyle(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
}
