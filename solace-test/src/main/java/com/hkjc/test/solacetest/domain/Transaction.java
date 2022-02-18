package com.hkjc.test.solacetest.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Transaction {
	@Id
	private String Id;
	private String IO;
	private String Topic;
	private String Content;
	private LocalDateTime CreatedDateTime;
	
	public void setId(String _id) {
		this.Id = _id;
	}
	
	public String getId() {
		return this.Id;
	}
	
	public void setIO(String _IO) {
		this.IO = _IO;
	}
	
	public String getIO() {
		return this.IO;
	}
	
	public void setContent(String _Content) {
		this.Content = _Content;
	}
	
	public String getContent() {
		return this.Content;
	}
	
	public void setTopic(String _Topic) {
		this.Topic = _Topic;
	}
	
	public String getTopic() {
		return this.Topic;
	}
	
	public void setCreatedDateTime(LocalDateTime _dt) {
		this.CreatedDateTime = _dt;
	}
	
	public LocalDateTime getCreatedDateTime() {
		return this.CreatedDateTime;
	}
}
