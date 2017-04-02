package model;

import java.util.HashSet;
import java.util.Set;

public class LinkQueue {
	//已访问的url集合
	private  Set<Object> visitedUrl = new HashSet<Object>();
	
	//待访问的url集合
	private  Queue unVisitedUrl = new Queue();
	
	//获得url队列
	public  Queue getUnVisitedUrl(){
		return unVisitedUrl;
	}
	
	//添加到访问过的url队列中
	public  void addVisitedUrl(String url){
		visitedUrl.add(url);
	}
	
	//移出访问过的URL
	public  void removeVisitedUrl(String url){
		visitedUrl.remove(url);
	}
	
	//未访问过的URL出队列
	public  Object unVisitedUrlDeQueue(){
		return unVisitedUrl.deQueue();
	}
	
	//保证每个URL只被访问过一次
	public  void addUnvisitedUrl(String url){
		if(url != null && !url.trim().equals("") && !visitedUrl.contains(url) && !unVisitedUrl.contains(url)){
			unVisitedUrl.enQueue(url);
		}
	}
	
	//获得已经访问的url数目
	public  int getVisitedUrlNum(){
		return visitedUrl.size();
	}
	
	//判断未访问的url队列是否为空
	public  boolean unVisitedUrlEmpty(){
		return unVisitedUrl.empty();
	}
}
