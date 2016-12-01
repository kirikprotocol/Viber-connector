package com.eyelinecom.whoisd.sads2.viber.connector;

import com.eyelinecom.whoisd.sads2.common.StoredHttpRequest;
import com.eyelinecom.whoisd.sads2.events.Event;
import com.eyelinecom.whoisd.sads2.eventstat.LoggableExternalRequest;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberCallback;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 4:01
 
 */
public class ViberCallbackRequest extends StoredHttpRequest implements LoggableExternalRequest {

  private final static Log log = new Log4JLogger(org.apache.log4j.Logger.getLogger(ViberCallbackRequest.class));

  private final String serviceId;
  private final ViberCallback callback;

  private transient Profile profile;
  private transient Event event;

  public ViberCallbackRequest(HttpServletRequest request) throws IOException {
    super(request);
    final String[] parts = getRequestURI().split("/");
    serviceId = parts[parts.length - 1];
    String content = getContent();
    log.debug("viber callback: " + content);
    callback = readCallback(content);
  }

  public ViberCallback getCallback() {
    return callback;
  }

  public String getServiceId() {
    return serviceId;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static ViberCallback readCallback(String json) throws IOException {
    return mapper.readerFor(ViberCallback.class).readValue(mapper.readTree(json));
  }

  @Override
  public Object getLoggableData() {
    return getCallback();
  }
}
