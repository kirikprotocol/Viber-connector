package com.eyelinecom.whoisd.sads2.viber.registry;

import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.exception.ConfigurationException;
import com.eyelinecom.whoisd.sads2.registry.Config;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfigListener;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberResponse;
import com.eyelinecom.whoisd.sads2.viber.resource.ViberApi;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 
 
 
 
 */
public class ViberServiceRegistry extends ServiceConfigListener {

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ViberServiceRegistry.class);

  public static final String CONF_TOKEN = "viber.token";

  private final Map<String, ServiceEntry> serviceMap = new ConcurrentHashMap<>();
  private final ViberApi client;

  private final static String CHAT_URL_BASE = "http://www.viber.com/";

  public ViberServiceRegistry(ViberApi client) {
    this.client = client;
  }

  @Override
  protected void process(Config config) throws ConfigurationException {
    final String serviceId = config.getId();
    if (config.isEmpty()) {
      unregister(serviceId);
    } else if (config instanceof ServiceConfig) {
      final ServiceConfig serviceConfig = (ServiceConfig) config;
      String token = getAccessToken(serviceConfig.getAttributes());
      token = StringUtils.trimToNull(token);
      if (token != null) {
        register(serviceId, token);
      }
    }
  }

  private void register(String serviceId, String token) {
    ServiceEntry serviceEntry = serviceMap.get(serviceId);
    if (serviceEntry != null && token.equals(serviceEntry.token)) {
      log.debug("Service " + serviceId + " already registered in viber client, token: " + token.substring(0, 6) + "...");
      return;
    }
    String url = null;
    try {
      url = UrlUtils.merge(client.connectorUrl(), serviceId);
    } catch (Exception e) {
      String msg = "error preparing webhook url";
      log.error(msg, e);
      throw new RuntimeException(msg);
    }
    log.debug("registering serviceId: " + serviceId + ", token: " + token.substring(0, 6) + "..." + " url: " + url);

    ViberResponse response = client.subscribeWebhook(token, url);
    if (response.getStatus() == 0) {
      serviceMap.put(serviceId, new ServiceEntry(serviceId, token));
      log.debug("registered");
    } else {
      log.error(
          "Failed to register in viber client for token: \"" + token + "\"" + ", reason: " + response.getStatusMessage()
      );
    }
  }

  private void unregister(String serviceId) {
    final ServiceEntry prevEntry = serviceMap.remove(serviceId);
    if (prevEntry != null) {
      client.subscribeWebhook(prevEntry.token, "");
      log.debug("Unregistered " + serviceId);
    } else {
      log.debug("Nothing to unregister for " + serviceId);
    }
  }

  public static String getAccessToken(Properties properties) {
    ViberBotSettings botSettings = getViberBotSettings(properties);
    if (botSettings == null) return null;
    return botSettings.accessToken;
  }

  private static ViberBotSettings getViberBotSettings(Properties properties) {
    return ViberBotSettings.get(properties.getProperty(ViberServiceRegistry.CONF_TOKEN));
  }

  public String getChatUrl(Properties properties) {
    String token = getAccessToken(properties);
    String uri = client.getAccountInfo(token).getUri();
    if (uri == null) {
      return null;
    }
    return getChatUrl(uri);
  }

  public static String getChatUrl(String uri) {
    return CHAT_URL_BASE + uri;
  }

  public static class Factory implements ResourceFactory {

    @Override
    public Object build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
      ViberApi api = SADSInitUtils.getResource("viber-api", properties);
      return new ViberServiceRegistry(api);
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }

  private static class ServiceEntry {

    private final String serviceId;
    private final String token;

    public ServiceEntry(String serviceId, String token) {
      this.serviceId = serviceId;
      this.token = token;
    }
  }

}

