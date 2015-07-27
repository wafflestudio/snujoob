package com.wafflestudio.snujoop;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class User {
	private Integer id;
	private ArrayList<Integer> mySubjectIdList = null;
	private String token;
	
	public static User user = null;
	
	User(){
		id = null;
		mySubjectIdList = new ArrayList<Integer>();
		token = null;
	}
	User(Integer id, ArrayList<Integer> subjectIdList, String token){
		this.id = id;
		mySubjectIdList = subjectIdList;
		this.token = token;
	}
	Integer getId(){
		return this.id;
	}
	ArrayList<Integer> getSubjectIdList(){
		return this.mySubjectIdList;
	}
	String getToken(){
		return this.token;
	}
	void setId(Integer id){
		this.id = id;
	}
	void appendMySubjectIdList(Integer id){
		mySubjectIdList.add(id);
	}
	void deleteMySubjectIdList(Integer id){
		mySubjectIdList.remove(id);
	}
	Boolean isMySubjectIdList(Integer id){
		return mySubjectIdList.contains(id);
	}
	static Boolean isStudentNumber(String studentNumber){
		return Pattern.matches("20[0-9]{2}-[0-9]{5}", studentNumber);
	}
	void setToken(String token){
		this.token = token;
	}
}