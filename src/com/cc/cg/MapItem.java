package com.cc.cg;

import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class MapItem extends OverlayItem {

    public enum Type { PROJECT, ME }
        
    private String name = "";
    private Type type;

    public MapItem(GeoPoint point, String name, Type type){
	super(point, "", "");
	this.name = name;
	this.type = type;
    }


    public String getName(){ return name; }
    public Type getType(){ return type; }
}
