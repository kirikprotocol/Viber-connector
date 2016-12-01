package com.eyelinecom.whoisd.sads2.viber.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 6:38
 
 */
public class ViberMessage {

  public static final String TYPE_STICKER = "sticker";
  public static final String TYPE_TEXT = "text";
  public static final String TYPE_PICTURE = "picture";
  public static final String TYPE_VIDEO = "video";
  public static final String TYPE_FILE = "file";
  public static final String TYPE_LOCATION = "location";
  public static final String TYPE_CONTACT = "contact";

  public static final Set<String> FILE_TYPES;
  static {
    FILE_TYPES = new HashSet<String>();
    FILE_TYPES.add(TYPE_STICKER);
    FILE_TYPES.add(TYPE_PICTURE);
    FILE_TYPES.add(TYPE_VIDEO);
    FILE_TYPES.add(TYPE_FILE);
  }

  @JsonProperty(value = "auth_token")
  String authToken;
  @JsonProperty(value = "receiver")
  String receiver;
  @JsonProperty(value = "type")
  String type; // sticker|text|picture|video|file|location|contact
  @JsonProperty(value = "text")
  String text;
  @JsonProperty(value = "contact")
  ViberContact contact;
  @JsonProperty(value = "media")
  String media;
  @JsonProperty(value = "location")
  ViberLocation location;

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setContact(ViberContact contact) {
    this.contact = contact;
  }

  public void setMedia(String media) {
    this.media = media;
  }

  public void setLocation(ViberLocation location) {
    this.location = location;
  }

  public String getText() {
    return text;
  }

  public String getType() {
    return type;
  }

  public ViberLocation getLocation() {
    return location;
  }

  public String getMedia() {
    return media;
  }

  public ViberContact getContact() {
    return contact;
  }
}
