package com.cc.cg;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ThreadPool {

    private ExecutorService _pool = Executors.newCachedThreadPool( );

    // Singleton
    private ThreadPool(){}
    private static ThreadPool _instance = new ThreadPool();
    public static ThreadPool instance() { return _instance; }

    public void execute(Runnable r){
	_pool.execute(r);
    }
}