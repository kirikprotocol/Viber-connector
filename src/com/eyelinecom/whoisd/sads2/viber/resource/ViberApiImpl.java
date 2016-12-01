package com.eyelinecom.whoisd.sads2.viber.resource;

import com.eyelinecom.whoisd.sads2.common.*;
import com.eyelinecom.whoisd.sads2.eventstat.DetailedStatLogger;
import com.eyelinecom.whoisd.sads2.executors.connector.Context;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.viber.api.types.*;
import com.eyelinecom.whoisd.sads2.viber.util.MarshalUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 7:11
 
 */
public class ViberApiImpl implements ViberApi {

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ViberApiImpl.class);

  private static final AtomicLong guidGenerator = new AtomicLong(System.currentTimeMillis());

  private final HttpDataLoader loader;
  private final DetailedStatLogger detailedStatLogger;
  private final Properties properties;
  private final String connectorUrl;
  private final int maxRateLimit;
  private final int maxRateInterval;
  private final ArrayBlockingQueue<Long> rateLimitQueue;

  public ViberApiImpl(HttpDataLoader loader,
                   DetailedStatLogger detailedStatLogger,
                   Properties properties) {

    this.loader = loader;
    this.detailedStatLogger = detailedStatLogger;
    this.properties = properties;
    this.connectorUrl = properties.getProperty("connector.url");
    this.maxRateLimit = Integer.parseInt(properties.getProperty("rate.limit", "2"));
    this.maxRateInterval = Integer.parseInt(properties.getProperty("rate.interval", "1000"));
    this.rateLimitQueue = new ArrayBlockingQueue<>(maxRateLimit);
    for (int i = 0; i < maxRateLimit; i++) rateLimitQueue.add(0L); //this queue will be alwats full
  }

  private String requestApi(String url, String json) throws Exception {
    //queue.
    checkRateLimit();
    log.debug("viber api request: " + url + "\nparams are\n" + json);

    if (Context.getSadsRequest() != null) {
      detailedStatLogger.onOuterResponse(Context.getSadsRequest(), url);
    }

    Loader.Entity data =
        loader.load(url, json, "application/json", "UTF-8", new HashMap(), HttpLoader.METHOD_POST);

    String response = new String(data.getBuffer());
    log.debug("viber api response: " + response);

    return response;
  }

  private synchronized void checkRateLimit() throws InterruptedException {
    // TODO: should make better version? consider serveral last request?
    long currentTime = System.currentTimeMillis();
    long farthestRequestTime = rateLimitQueue.peek();
    if (currentTime - farthestRequestTime < maxRateInterval) {
      long sleepTime = maxRateInterval - (currentTime - farthestRequestTime);
      if (sleepTime > 10) log.debug("rate limit exceeded, sleeping for " + sleepTime);
      Thread.sleep(sleepTime);
    }
    rateLimitQueue.remove();
    rateLimitQueue.put(System.currentTimeMillis());
  }

  @Override
  public String connectorUrl() {
        return connectorUrl;
  }



  @Override
  public ViberAccountInfoResponse getAccountInfo(String authToken) {
    try {
      ViberRequest request = new ViberRequest();
      request.setAuthToken(authToken);
      String json = MarshalUtils.marshal(request);
      String response = requestApi("https://chatapi.viber.com/pa/get_account_info", json);
      return MarshalUtils.unmarshal(response, ViberAccountInfoResponse.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ViberResponse subscribeWebhook(String authToken, String webhookUrl) {
    try {
      ViberRequest request = new ViberRequest();
      request.setAuthToken(authToken);
      request.setUrl(webhookUrl);
      request.setEventTypes(new String[] { "message", "delivered", "seen", "webhook" } );
      String json = MarshalUtils.marshal(request);
      String response = requestApi("https://chatapi.viber.com/pa/set_webhook", json);
      return MarshalUtils.unmarshal(response, ViberResponse.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ViberResponse sendMessage(ViberMessage message) {
    try {
      String json = MarshalUtils.marshal(message);
      String response = requestApi("https://chatapi.viber.com/pa/send_message", json);
      return MarshalUtils.unmarshal(response, ViberResponse.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendMessage(String msg, String receiver, String authToken) {
    try {
      ViberMessage viberMessage = new ViberMessage();
      viberMessage.setAuthToken(authToken);
      viberMessage.setReceiver(receiver);
      viberMessage.setType(ViberMessage.TYPE_TEXT);
      viberMessage.setText(msg);
      String json = MarshalUtils.marshal(viberMessage);
      String response = requestApi("https://chatapi.viber.com/pa/send_message", json);
      ViberResponse viberResponse = MarshalUtils.unmarshal(response, ViberResponse.class);
      if (viberResponse.getStatus() != 0) {
        log.warn("error sending message\n" + json);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public static class Factory implements ResourceFactory {

    @Override
    public Object build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
      final HttpDataLoader loader = SADSInitUtils.getResource("loader", properties);
      final DetailedStatLogger detailedStatLogger = SADSInitUtils.getResource("detailed-stat-logger", properties);
      return new ViberApiImpl(loader, detailedStatLogger, properties);
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }
}
