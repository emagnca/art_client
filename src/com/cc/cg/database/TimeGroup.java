package com.cc.cg.database;

import java.text.*;
import java.util.*;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;
import android.util.Log;


public class TimeGroup {

    public static final String ID_FIELD_NAME = "id";

    private Object[] cachedSlots = new Object[0];

    private static final DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
    private static final String TAG = "=== TimeGroup ===";


    @DatabaseField(id = true)
	private int id;
    @DatabaseField(defaultValue = "0")
	private int approvedCount;
    @DatabaseField(defaultValue = "0")
	private int total;
    @ForeignCollectionField(eager = true)
	private ForeignCollection<TimeSlot> slots;

    
    public TimeGroup() {}
  
    public TimeGroup(Date d){
	id = getDayCode(d);
    }

    public int getId(){
	return id;
    }

    public void addTotal(int n){
	total += n;
    }

    public void subTotal(int n){
	total -= n;
    }

    public int getTotal(){
	return total;
    }

    public int getSlotCount(){
	return slots.size();
    }

    public void approve(){
	approvedCount++;
    }

    public boolean isApproved(){
	return approvedCount >= slots.size();
    }

    public ForeignCollection<TimeSlot> getSlots(){
	return slots;
    }

    public Date getDate(){
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.YEAR, getYear(id));
	calendar.set(Calendar.DAY_OF_YEAR, getDayOfYear(id));
	return calendar.getTime();
    }


    public static int getDayCode(Date d){
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(d);
	return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static int getDayCodeForToday(){
	Calendar calendar = Calendar.getInstance();
	return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static int getYear(int dayCode) {
	return dayCode / 1000;
    }

    public static int getDayOfYear(int dayCode){
	return dayCode % 1000;
    }


    public String getAsDate(){
	return dfDay.format(getDate());
    }

    public TimeSlot getSlot(int i){
	if(i >= cachedSlots.length) cachedSlots = slots.toArray();
	return (TimeSlot)cachedSlots[i];
    }

    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("id=").append(id);
	sb.append(", ").append("approveCount=").append(approvedCount);
	sb.append(", ").append("slotCount=").append(slots.size());
	sb.append(", ").append("total=").append(total);
	return sb.toString();
    }
}
