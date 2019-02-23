package com.example.vmac.WatBot;

/**
 * Created by VMac on 17/11/16.
 */

import java.io.Serializable;

public class Message implements Serializable {
  private String id, message;
  private String sender;


  public Message() {
  }

  public Message(String id, String message, String createdAt) {
    this.id = id;
    this.message = message;


  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


}

