package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.eyelinecom.whoisd.sads2.exception.ExceptionMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by boris on 22/11/16.
 */
public class ViberLocation {

  @JsonProperty(value = "lat")
  String lat;

  @JsonProperty(value = "lon")
  String lon;

  public String getLat() {
    return lat;
  }

  public String getLon() {
    return lon;
  }

  public Double getParsedLat() throws NumberFormatException {
    return Double.parseDouble(lat);
  }

  public Double getParsedLon() throws NumberFormatException {
    return Double.parseDouble(lon);
  }


  public void setLat(String lat) {
    this.lat = lat;
  }

  public void setLon(String lon) {
    this.lon = lon;
  }
}
