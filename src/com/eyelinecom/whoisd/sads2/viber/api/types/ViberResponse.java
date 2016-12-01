package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Created by boris on 23/11/16.
 */
public class ViberResponse {

  @JsonProperty(value = "status")
  int status;


  @JsonProperty(value = "status_message")
  String statusMessage;

  public int getStatus() {
    return status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }
}
