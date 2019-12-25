package com.example.demo.model;

public class Message {
	private Object payload;
	private Integer messageId;
	
	public Message() {}
	
	public Message(Object payload, Integer messageId) {
		super();
		this.payload = payload;
		this.messageId = messageId;
	}

	public Object getPayload() {
		return payload;
	}
	
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
	public Integer getMessageId() {
		return messageId;
	}
	
	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}
	
	@Override
	public String toString() {
		return "Message [payload=" + payload + ", messageId=" + messageId + "]";
	}
}
