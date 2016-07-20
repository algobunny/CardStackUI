package com.mutualmobile.cardstack.sample;

/**
 * Created by alice on 7/19/16.
 */
public class ModelA {

    public enum Section {
        MERCURY, MARS, JUPITER, VENUS
    }

    public final int bgColor;
    public final Section section;
    //public final float spacing;

    public ModelA(int bgColor, Section seciton){
        this.bgColor = bgColor;
        this.section = seciton;
    }
}
