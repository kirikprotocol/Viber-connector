package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by boris on 13/12/16.
 */
public class ViberButton {

  @JsonProperty(value = "ActionBody")
  String actionBody;

  @JsonProperty(value = "Text")
  String text;

  public void setActionBody(String actionBody) {
    this.actionBody = actionBody;
  }

  public void setText(String text) {
    this.text = text;
  }

}
