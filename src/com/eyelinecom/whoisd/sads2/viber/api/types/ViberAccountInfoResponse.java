package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Created by boris on 23/11/16.
 */
public class ViberAccountInfoResponse {

  @JsonProperty(value = "uri")
  String uri;

  @JsonProperty(value = "webhook")
  String webhook;


  public String getUri() {
    return uri;
  }

  public String getWebhook() {
    return webhook;
  }
}
