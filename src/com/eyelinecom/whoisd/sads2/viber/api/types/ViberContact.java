package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by boris on 22/11/16.
 */
public class ViberContact {

  @JsonProperty(value = "name")
  String name;

  @JsonProperty(value = "phone_number")
  String phoneNumber;

  public String getName() {
    return name;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }
}
