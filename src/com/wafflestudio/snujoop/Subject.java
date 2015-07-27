package com.wafflestudio.snujoop;

class Subject {
	private Integer id;
	private String subjectName;
	private String subjectNumber;
	private String lectureNumber;
	private String lecturer;
	
	Subject(){
		id = 0;
		subjectName = null;
		subjectNumber = null;
		lectureNumber = null;
		lecturer = null;
	}
	Subject(Integer id, String subjectName, String subjectNumber, String lectureNumber, String lecturer){
		this.id = id;
		this.subjectName = subjectName;
		this.subjectNumber = subjectNumber;
		this.lectureNumber = lectureNumber;
		this.lecturer = lecturer;
	}
	void setId(int id){
		this.id = id;
	}
	void setName(String name){
		this.subjectName = name;
	}
	Integer getId(){
		return this.id;
	}
	String getSubjectName(){
		return this.subjectName;
	}
	String getSubjectNumber(){
		return this.subjectNumber;
	}
	String getLectureNumber(){
		return this.lectureNumber;
	}
	String getLecturer(){
		return this.lecturer;
	}
}