package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by boris on 22/11/16.
 */
public class ViberSender {


  @JsonProperty(value = "avatar")
  String avatar;

  @JsonProperty(value = "id")
  String viberId;

  @JsonProperty(value = "name")
  String name;

  public String getAvatar() {
    return avatar;
  }

  public String getViberId() {
    return viberId;
  }

  public String getName() {
    return name;
  }
}
