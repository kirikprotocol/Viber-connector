package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 6:37
 
 */
public class ViberCallback {


  @JsonProperty(value = "event")
  String event; // message|seen|delivered|webhook
  @JsonProperty(value = "message")
  ViberMessage message;
  @JsonProperty(value = "message_token")
  String messageToken;
  @JsonProperty(value = "sender")
  ViberSender sender;
  @JsonProperty(value = "timestamp")
  long timestamp;

  public String getEvent() {
    return event;
  }

  public ViberMessage getMessage() {
    return message;
  }

  public String getMessageToken() {
    return messageToken;
  }

  public ViberSender getSender() {
    return sender;
  }

  public long getTimestamp() {
    return timestamp;
  }

}
