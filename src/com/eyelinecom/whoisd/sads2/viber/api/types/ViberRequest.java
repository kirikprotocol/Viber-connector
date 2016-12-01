package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by boris on 23/11/16.
 */
public class ViberRequest {

  @JsonProperty(value = "auth_token")
  String authToken;

  @JsonProperty(value = "url")
  String url;

  @JsonProperty(value = "event_types")
  String[] eventTypes;


  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setEventTypes(String[] eventTypes) {
    this.eventTypes = eventTypes;
  }
}
