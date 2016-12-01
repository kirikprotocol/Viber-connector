package com.eyelinecom.whoisd.sads2.viber.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.*;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.attachments.Attachment;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.session.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberLocation;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberMessage;
import com.eyelinecom.whoisd.sads2.viber.registry.ViberServiceRegistry;
import com.eyelinecom.whoisd.sads2.viber.resource.ViberApi;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.logging.Log;
import org.dom4j.Document;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.eyelinecom.whoisd.sads2.content.attachments.Attachment.Type.*;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created with IntelliJ IDEA.
 
 
 
 
 */
public class ViberAttachmentPushInterceptor extends BlankInterceptor implements Initable {

  private static final org.apache.log4j.Logger globalLog = org.apache.log4j.Logger.getLogger(ViberAttachmentPushInterceptor.class);

  private static final Cache<String, String> uploadPhotoCache = CacheBuilder.newBuilder()
    .maximumSize(100000)
    .expireAfterAccess(7, TimeUnit.DAYS)
    .removalListener(new RemovalListener<String, String>() {
      @Override
      public void onRemoval(RemovalNotification<String, String> notification) {
        globalLog.info("Removing from cache entry for " + notification.getKey() + ":" + notification.getValue());
      }
    }).build();


  private ViberApi client;
  private ServiceSessionManager sessionManager;
  private HttpDataLoader loader;


  public void afterResponseRender(SADSRequest request,
                                  ContentResponse content,
                                  SADSResponse response,
                                  RequestDispatcher dispatcher) throws InterceptionException {
    try {
      if (isNotBlank(request.getParameters().get("sadsSmsMessage"))) {
        sendAttachment(request, content, response);
      } else {
        // ?
        sendAttachment(request, content, response);
      }
    } catch (Exception e) {
      throw new InterceptionException(e);
    }

  }

  private void sendAttachment(SADSRequest request, ContentResponse content, SADSResponse response) {
    final String serviceId = request.getServiceId();
    final Document doc = (Document) response.getAttributes().get(PageBuilder.VALUE_DOCUMENT);
    Log log = SADSLogger.getLogger(request, this.getClass());

    final Collection<Attachment> attachments = Attachment.extract(globalLog, doc);
    if (attachments.isEmpty()) return;

    String authToken = ViberServiceRegistry.getAccessToken(request.getServiceScenario().getAttributes());
    String receiver = request.getProfile().property("viber", "id").getValue();

    for (Attachment attachment : attachments) {

      ViberMessage viberMessage = new ViberMessage();
      viberMessage.setAuthToken(authToken);
      viberMessage.setReceiver(receiver);

      Attachment.Type type = fromString(attachment.getType());
      if (LOCATION == type) {
        viberMessage.setType(ViberMessage.TYPE_LOCATION);
        ViberLocation viberLocation = new ViberLocation();
        viberLocation.setLat(attachment.getLatitude());
        viberLocation.setLon(attachment.getLongitude());
        viberMessage.setLocation(viberLocation);
      } else  {
        String url = extractMediaLink(attachment, request, log);
        viberMessage.setMedia(url);
        if (PHOTO == type) {
          viberMessage.setType(ViberMessage.TYPE_PICTURE);
        } else if (VIDEO == type) {
          viberMessage.setType(ViberMessage.TYPE_VIDEO);
        } else {
          viberMessage.setType(ViberMessage.TYPE_FILE);
        }
      }
      client.sendMessage(viberMessage);
    }
  }

  private String extractMediaLink(Attachment attachment, SADSRequest request, Log log) {
    String url = null;
    String uri = attachment.getSrc();
    if (uri == null) {
      RuntimeException re =
          new RuntimeException("attachments with no link to media are not allowed for viber");
      log.warn(re);
      throw re;
    }
    try {
      UrlUtils.merge(request.getResourceURI(), uri);
    } catch (Exception e) {
      RuntimeException re =
          new RuntimeException("error merging relative media link for attachment");
      log.warn(re);
      throw re;
    }
    return null;
  }

  @Override
  public void init(Properties config) throws Exception {
    client = (ViberApi) SADSInitUtils.getResource("client", config);
    sessionManager = (ServiceSessionManager) SADSInitUtils.getResource("session-manager", config);
    loader = SADSInitUtils.getResource("loader", config);
  }

  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  @Override
  public void destroy() {
  }
}
