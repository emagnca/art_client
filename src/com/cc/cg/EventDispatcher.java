package com.cc.cg;

import java.util.*;
import android.util.Log;

public class EventDispatcher {

    private final static String TAG = "=== EventDispatcher ===";

    // Singleton
    private EventDispatcher(){}
    private static EventDispatcher _instance = new EventDispatcher();
    public static EventDispatcher instance() { return _instance; }

    // 
    private Set<PositionListener> _positionListeners = new HashSet<PositionListener>();
    private Set<ProjectListener> _projectListeners = new HashSet<ProjectListener>();

    private Object _positionLock = new Object();
    private Object _projectLock = new Object();

    /**
       Handling of position updates
     **/
    public interface PositionListener {
	void movedIntoProjectArea(long projectId);
	void movedOutOfProjectArea();
	void newPosition();
    }

    public void registerPositionListener(PositionListener listener){
	synchronized(_positionLock){
	    _positionListeners.add(listener); 
	}
	Log.d(TAG, "Register: Number of listeners=" + _positionListeners.size());
    }

    public void unregisterPositionListener(PositionListener listener){
	synchronized(_positionLock){
	    _positionListeners.remove(listener); 
	}
	Log.d(TAG, "UnRegister: Number of listeners=" + _positionListeners.size());
    }

    public void signalMovedIntoProjectArea(long projectId){
	synchronized(_positionLock){
	    for(PositionListener listener : _positionListeners)
		listener.movedIntoProjectArea(projectId);
	}
    } 

    public void signalMovedOutOfProjectArea(){
	synchronized(_positionLock){
	    for(PositionListener listener : _positionListeners)
		listener.movedOutOfProjectArea();
	}
    }

    public void signalNewPosition(){
	synchronized(_positionLock){
	    for(PositionListener listener : _positionListeners)
		listener.newPosition();
	}
    } 



    /**
       Handling of project updates
    **/
    public interface ProjectListener {
	void projectsModified();
    }

    public void registerProjectListener(ProjectListener listener){
	synchronized(_projectLock){
	    _projectListeners.add(listener); 
	}
	Log.d(TAG, "Register: Number of listeners=" + _projectListeners.size());
    }

    public void unregisterProjectListener(ProjectListener listener){
	synchronized(_projectLock){
	    _projectListeners.remove(listener); 
	}
	Log.d(TAG, "UnRegister: Number of listeners=" + _projectListeners.size());
    }

    public void signalProjectsModified(){
	synchronized(_projectLock){
	    for(ProjectListener listener : _projectListeners)
		listener.projectsModified();
	}
    } 
    
}