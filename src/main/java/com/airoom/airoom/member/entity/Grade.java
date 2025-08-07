package com.airoom.airoom.member.entity;

public enum Grade {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6);

    private final int value;

    Grade(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }



}
