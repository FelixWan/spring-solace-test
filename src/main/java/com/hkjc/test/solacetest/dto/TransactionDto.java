package com.hkjc.test.solacetest.dto;

import java.time.LocalDateTime;

public class TransactionDto {
	private String Id;
	private String IO;
	private String Content;
	private String Topic;
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
