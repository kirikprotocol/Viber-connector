package com.eyelinecom.whoisd.sads2.viber.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.*;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.attachments.Attachment;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberLocation;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberMessage;
import com.eyelinecom.whoisd.sads2.viber.registry.ViberServiceRegistry;
import com.eyelinecom.whoisd.sads2.viber.resource.ViberApi;
import org.apache.commons.logging.Log;
import org.dom4j.Document;

import java.util.Collection;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.content.attachments.Attachment.Type.*;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created with IntelliJ IDEA.
 
 
 
 
 */
public class ViberAttachmentPushInterceptor extends BlankInterceptor implements Initable {

  private static final org.apache.log4j.Logger globalLog = org.apache.log4j.Logger.getLogger(ViberAttachmentPushInterceptor.class);


  private ViberApi client;



  @Override
  public void afterResponseRender(SADSRequest request,
                                  ContentResponse content,
                                  SADSResponse response,
                                  RequestDispatcher dispatcher) throws InterceptionException {
    try {
      sendAttachment(request, response);
    } catch (Exception e) {
      throw new InterceptionException(e);
    }

  }

  private void sendAttachment(SADSRequest request, SADSResponse response) {
    // final String serviceId = request.getServiceId();
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
    String uri = attachment.getSrc();
    if (uri == null) {
      RuntimeException re =
          new RuntimeException("attachments with no link to media are not allowed for viber");
      log.warn(re);
      throw re;
    }
    try {
      return UrlUtils.merge(request.getResourceURI(), uri);
    } catch (Exception e) {
      RuntimeException re =
          new RuntimeException("error merging relative media link for attachment");
      log.warn(re);
      throw re;
    }
  }

  @Override
  public void init(Properties config) throws Exception {
    client = SADSInitUtils.getResource("client", config);
  }


  @Override
  public void destroy() {
  }
}
