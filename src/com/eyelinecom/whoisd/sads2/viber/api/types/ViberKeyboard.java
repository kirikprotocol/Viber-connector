package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by boris on 13/12/16.
 */
public class ViberKeyboard {

  @JsonProperty(value = "Type")
  String type = "keyboard";

  @JsonProperty(value = "Buttons")
  ViberButton[] buttons;

  public void setButtons(ViberButton[] buttons) {
    this.buttons = buttons;
  }

}

