package com.cc.cg.database;

import com.j256.ormlite.field.DatabaseField;

public class WorkPlan {

    public static final String ID_FIELD_NAME = "id";
    public static final String PROJECT_FIELD_NAME = "project";
    public static final String ACTIVITY_FIELD_NAME = "activity";
    public static final String START_FIELD_NAME = "start";
    public static final String STOP_FIELD_NAME = "stop";
    public static final String COMMENT_FIELD_NAME = "comment";
    public static final String TIMESTAMP_FIELD_NAME = "timestamp";

    @DatabaseField(id = true, columnName = ID_FIELD_NAME)
	int id;
    @DatabaseField(columnName = PROJECT_FIELD_NAME)
	String project;
    @DatabaseField(columnName = ACTIVITY_FIELD_NAME)
	String activity;
    @DatabaseField(columnName = START_FIELD_NAME)
	long start;
    @DatabaseField(columnName = STOP_FIELD_NAME)
	long stop;
    @DatabaseField(columnName = COMMENT_FIELD_NAME)
	String comment;    
    @DatabaseField(defaultValue = "0", columnName = TIMESTAMP_FIELD_NAME)
	long timestamp;    
    
    public WorkPlan() {}
    
    public WorkPlan(int id, String project, String activity, long start, long stop, 
		    String comment, long timestamp) {
	this.id = id;
	this.project = project;
	this.activity = activity;
	this.start = start;
	this.stop = stop;
	this.comment = comment;
	this.timestamp = timestamp;
    }
    
    public int getId(){ return id; }
    public String getActivity(){ return activity; }
    public void setActivity(String s){ this.activity = s; }
    public String getProject(){ return project; }
    public void setProject(String s){ this.project = s; }
    public String getComment(){ return comment; }
    public void setComment(String s){ this.comment = s; }
    public long getStart(){ return start; }
    public void setStart(long l){ this.start = l; }
    public long getStop(){ return stop; }
    public void setStop(long l){ this.stop = l; }
    public long getTimestamp(){ return timestamp; }
    public void setTimestamp(long timestamp){ this.timestamp = timestamp; }

    public String toFullString() {
	StringBuilder sb = new StringBuilder();
	sb.append("id=").append(id);
	sb.append(", ").append("project=").append(project);
	sb.append(", ").append("start=").append(start);
	sb.append(", ").append("stop=").append(stop);
	sb.append(", ").append("timestamp=").append(timestamp);
	return sb.toString();
    }


}
