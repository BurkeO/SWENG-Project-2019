package com.example.vmac.WatBot;

/**
 * Created by J.Cistiakovas on 22/03/2019
 * last modified: 24/03/2019 by C.Coady - added the type attribute
 */

import java.io.Serializable;

public class Message implements Serializable {
  private String id, message, sender, type;


  public Message() {
  }

  public String getType(){
    return type;
  }

  public void setType(String type){
    this.type = type;
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

