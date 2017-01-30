package com.eyelinecom.whoisd.sads2.viber.registry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 2:12
 
 */
public class ViberBotSettings {
  private static final Pattern PATTERN = Pattern.compile("[0-9a-fA-F]{16}+\\-[0-9a-fA-F]{16}+\\-[0-9a-fA-F]{16}+");

  public final String accessToken; //451bbb6ccfd4eeaa-b12230c23889273f-d4a5333444d97dca

  private ViberBotSettings(String accessToken) {
    this.accessToken = accessToken;
  }

  public static ViberBotSettings get(String value) {
    if (value == null) return null;
    Matcher m = PATTERN.matcher(value);
    if (!m.matches()) return null;
    return new ViberBotSettings(value);
  }
}
