package com.eyelinecom.whoisd.sads2.viber.connector;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.ProfileUtil;
import com.eyelinecom.whoisd.sads2.common.SADSUrlUtils;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.ChatCommand;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.events.Event;
import com.eyelinecom.whoisd.sads2.events.LinkEvent;
import com.eyelinecom.whoisd.sads2.events.MessageEvent.TextMessageEvent;
import com.eyelinecom.whoisd.sads2.exception.NotFoundResourceException;
import com.eyelinecom.whoisd.sads2.executors.connector.AbstractHTTPPushConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.ProfileEnabledMessageConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.input.AbstractInputType;
import com.eyelinecom.whoisd.sads2.input.InputContact;
import com.eyelinecom.whoisd.sads2.input.InputFile;
import com.eyelinecom.whoisd.sads2.input.InputLocation;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.session.SessionManager;
import com.eyelinecom.whoisd.sads2.utils.ConnectorUtils;
import com.eyelinecom.whoisd.sads2.viber.api.types.*;
import com.eyelinecom.whoisd.sads2.viber.registry.ViberServiceRegistry;
import com.eyelinecom.whoisd.sads2.viber.resource.ViberApi;
import com.eyelinecom.whoisd.sads2.viber.util.MarshalUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.Protocol.VIBER;
import static com.eyelinecom.whoisd.sads2.common.ProfileUtil.inProfile;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.CLEAR_PROFILE;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.INVALIDATE_SESSION;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.SET_DEVELOPER_MODE;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.SHOW_PROFILE;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.WHO_IS;
import static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 3:53
 
 */
public class ViberMessageConnector extends HttpServlet {

  private final static Log log = new Log4JLogger(org.apache.log4j.Logger.getLogger(ViberMessageConnector.class));

  private ViberMessageConnectorImpl connector;

  @Override
  public void destroy() {
    super.destroy();
    connector.destroy();
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    connector = new ViberMessageConnectorImpl();

    try {
      final Properties properties = AbstractHTTPPushConnector.buildProperties(servletConfig);
      connector.init(properties);

    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  protected void service(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

    final ViberCallbackRequest request = new ViberCallbackRequest(req);
    if (!"message".equals(request.getCallback().getEvent())) {
      resp.setStatus(200);
      return;
    }
    final SADSResponse response = connector.process(request);
    ConnectorUtils.fillHttpResponse(resp, response);
  }

  private class ViberMessageConnectorImpl
    extends ProfileEnabledMessageConnector<ViberCallbackRequest> {

    @Override
    protected SADSResponse buildQueuedResponse(ViberCallbackRequest request, SADSRequest sadsRequest) {
      return buildCallbackResponse(200, "ok");
    }

    @Override
    protected SADSResponse buildQueueErrorResponse(Exception e,
                                                   ViberCallbackRequest viberCallbackRequest,
                                                   SADSRequest sadsRequest) {
      return buildCallbackResponse(500, "");
    }

    @Override
    protected Log getLogger() {
      return log;
    }

    @Override
    protected String getSubscriberId(ViberCallbackRequest req) throws Exception {
      if (req.getProfile() != null) {
        return req.getProfile().getWnumber();
      }

      final String userId = String.valueOf(req.getCallback().getSender().getViberId());
      final String incoming = req.getCallback().getMessage().getText();

      if (ChatCommand.match(getServiceId(req), incoming, VIBER) == CLEAR_PROFILE) {
        // Reset profile of the current user.
        final Profile profile = getProfileStorage()
            .query()
            .where(property("viber", "id").eq(userId))
            .get();
        if (profile != null) {
          final boolean isDevModeEnabled = inProfile(profile).getDeveloperMode(req.getServiceId());
          if (isDevModeEnabled) {
            inProfile(profile).clear();
            inProfile(profile).setDeveloperMode(getServiceId(req), true);

            // Also clear the session.
            final SessionManager sessionManager = getSessionManager(VIBER, req.getServiceId());
            final Session session = sessionManager.getSession(profile.getWnumber(), false);
            if (session != null && !session.isClosed()) {
              session.close();
            }
          }
        }
      }

      final Profile profile = getProfileStorage()
        .query()
        .where(property("viber", "id").eq(userId))
        .getOrCreate();

      req.setProfile(profile);
      return profile.getWnumber();
    }

    @Override
    protected String getServiceId(ViberCallbackRequest req) throws Exception {
      return req.getServiceId();
    }

    @Override
    protected String getGateway() {
      return "Viber";
    }

    @Override
    protected String getGatewayRequestDescription(ViberCallbackRequest viberCallbackRequest) {
      return "Viber";
    }

    @Override
    protected boolean isTerminated(ViberCallbackRequest req) throws Exception {
      final String incoming = req.getCallback().getMessage().getText();

      final boolean isDevModeEnabled = req.getProfile() != null &&
          ProfileUtil.inProfile(req.getProfile()).getDeveloperMode(req.getServiceId());

      final ChatCommand command = ChatCommand.match(getServiceId(req), incoming, VIBER);
      return command == SET_DEVELOPER_MODE ||
          isDevModeEnabled && asList(SHOW_PROFILE, WHO_IS).contains(command);
    }

    @Override
    protected Long getEventOrder(ViberCallbackRequest req) {
      return req.getCallback().getTimestamp();
    }

    @Override
    protected Protocol getRequestProtocol(ServiceConfig config, String subscriberId, ViberCallbackRequest request) {
      return VIBER;
    }

    @Override
    protected String getRequestUri(ServiceConfig config,
                                   String wnumber,
                                   ViberCallbackRequest message) throws Exception {

      final String serviceId = config.getId();
      final String incoming = message.getCallback().getMessage().getText();
      final SessionManager sessionManager = getSessionManager(serviceId);
      final Profile profile = getProfileStorage().find(wnumber);
      final boolean isDevModeEnabled = inProfile(profile).getDeveloperMode(serviceId);

      Session session = sessionManager.getSession(wnumber);

      final ChatCommand cmd = ChatCommand.match(serviceId, incoming, VIBER);
      if (cmd == INVALIDATE_SESSION && isDevModeEnabled) {
        // Invalidate the current session.
        session.close();
        session = sessionManager.getSession(wnumber);

      } else {
        final ViberApi client = getClient();
        final String receiver = message.getCallback().getSender().getViberId();
        final String authToken = ViberServiceRegistry.getAccessToken(config.getAttributes());

        if (cmd == WHO_IS && isDevModeEnabled) {
          final String msg =
              StringUtils.join(
                  new String[] {
                      "Chat URL: " + ViberServiceRegistry.getChatUrl(client.getAccountInfo(authToken).getUri()) + ".",
                      "Service: " + serviceId + ".",
                      "MiniApps host: " + getRootUri()
                  },
                  "\n");
          client.sendMessage(msg, receiver, authToken);

        } else if (cmd == SHOW_PROFILE && isDevModeEnabled) {
          client.sendMessage(profile.dump(), receiver, authToken);
        } else if (cmd == SET_DEVELOPER_MODE) {
          final String value = ChatCommand.getCommandValue(incoming);
          final Boolean devMode = BooleanUtils.toBooleanObject(value);

          if (devMode != null) {
            inProfile(profile).setDeveloperMode(serviceId, devMode);

            client.sendMessage(
                "Developer mode is " + (devMode ? "enabled" : "disabled") + ".",
                receiver,
                authToken);

          } else {
            client.sendMessage(
                "Developer mode is " +
                    (inProfile(profile).getDeveloperMode(serviceId) ? "enabled" : "disabled") +
                    ".",
                receiver,
                authToken);
          }
        }
      }

      final String prevUri = (String) session.getAttribute(ATTR_SESSION_PREVIOUS_PAGE_URI);
      if (prevUri == null) {
        // No previous page means this is an initial request, thus serve the start page.
        message.setEvent(new TextMessageEvent(incoming));
        return super.getRequestUri(config, wnumber, message);

      } else {
        final Document prevPage =
          (Document) session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE);

        String href = null;
        String inputName = null;

        // Look for a button with a corresponding label.
        //noinspection unchecked
        for (Element e : (List<Element>) prevPage.getRootElement().elements("button")) {
          final String btnLabel = e.getTextTrim();
          final String btnIndex = e.attributeValue("index");

          if (equalsIgnoreCase(btnLabel, incoming) || equalsIgnoreCase(btnIndex, incoming)) {
            final String btnHref = e.attributeValue("href");
            href = btnHref != null ? btnHref : e.attributeValue("target");

            message.setEvent(new LinkEvent(btnLabel, prevUri));
          }
        }

        // Look for input field if any.
        if (href == null) {
          final Element input = prevPage.getRootElement().element("input");
          if (input != null) {
            href = input.attributeValue("href");
            inputName = input.attributeValue("name");
          }
        }

        // Nothing suitable to handle user input found, consider it a bad command.
        if (href == null) {
          final String badCommandPage =
            InitUtils.getString("bad-command-page", "", config.getAttributes());
          href = UrlUtils.merge(prevUri, badCommandPage);
          href = UrlUtils.addParameter(href, "bad_command", incoming);
        }

        if (message.getEvent() == null) {
          message.setEvent(new TextMessageEvent(incoming));
        }

        href = SADSUrlUtils.processUssdForm(href, StringUtils.trim(incoming));
        if (inputName != null) {
          href = UrlUtils.addParameter(href, inputName, incoming);
        }

        return UrlUtils.merge(prevUri, href);
      }
    }

    @Override
    protected SADSResponse getOuterResponse(ViberCallbackRequest viberCallbackRequest,
                                            SADSRequest request,
                                            SADSResponse response) {
      return buildCallbackResponse(200, "ok");
    }

    private SessionManager getSessionManager(String serviceId) throws Exception {
      return super.getSessionManager(VIBER, serviceId);
    }

    @Override
    protected void fillSADSRequest(SADSRequest sadsRequest, ViberCallbackRequest request) {
      try {
        handleFileUpload(sadsRequest, request);

      } catch (Exception e) {
        getLog(request).error(e.getMessage(), e);
      }

      super.fillSADSRequest(sadsRequest, request);
    }

    @Override
    protected Profile getCachedProfile(ViberCallbackRequest req) {
      return req.getProfile();
    }

    @Override
    protected Event getEvent(ViberCallbackRequest req) {
      return req.getEvent();
    }

    private void handleFileUpload(SADSRequest sadsRequest, ViberCallbackRequest req) throws Exception {
      final List<? extends AbstractInputType> mediaList = extractMedia(req);
      if (isEmpty(mediaList)) return;

      req.setEvent(mediaList.iterator().next().asEvent());

      final Session session = sadsRequest.getSession();
      final Document prevPage = (Document) session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE);
      final Element input = prevPage == null ? null : prevPage.getRootElement().element("input");
      final String inputName = input != null ? input.attributeValue("name") : "bad_command";

      final String mediaParameter = MarshalUtils.marshal(mediaList);
      sadsRequest.getParameters().put(inputName, mediaParameter);
      sadsRequest.getParameters().put("input_type", "json");
    }

    private List<? extends AbstractInputType> extractMedia(ViberCallbackRequest req) {
      final ViberMessage viberMessage = req.getCallback().getMessage();
      final List<AbstractInputType> mediaList = new ArrayList<>();

      String messageType = viberMessage.getType();

      if (ViberMessage.TYPE_LOCATION.equals(messageType)) {
        ViberLocation viberLocation = viberMessage.getLocation();
        final InputLocation location = new InputLocation();
        try {
          location.setLatitude(viberLocation.getParsedLat());
          location.setLongitude(viberLocation.getParsedLon());
          mediaList.add(location);
        } catch (Exception e) {
          log.warn("error parsing coordinates ", e);
        }
      } else if (ViberMessage.FILE_TYPES.contains(messageType)) {
        final InputFile file = new InputFile();
        file.setUrl(viberMessage.getMedia());
        if (ViberMessage.TYPE_FILE.equals(messageType)) {
          file.setMediaType("document");
        } else if (ViberMessage.TYPE_PICTURE.equals(messageType)) {
          file.setMediaType("photo");
        } else {
          file.setMediaType(messageType);
        }
        mediaList.add(file);
      } else if (ViberMessage.TYPE_CONTACT.equals(messageType)) {
        ViberContact viberContact = viberMessage.getContact();
        final InputContact contact = new InputContact();
        contact.setName(viberContact.getName());
        contact.setMsisdn(viberContact.getPhoneNumber());
        mediaList.add(contact);
      }
      return mediaList;
    }

    private SADSResponse buildCallbackResponse(int statusCode, String body) {
      final SADSResponse rc = new SADSResponse();
      rc.setStatus(statusCode);
      rc.setHeaders(Collections.<String, String>emptyMap());
      rc.setMimeType("text/plain");
      rc.setData(body.getBytes());
      return rc;
    }

    private ViberServiceRegistry getServiceRegistry() throws NotFoundResourceException {
      return getResource("viber-service-registry");
    }

    private ViberApi getClient() throws NotFoundResourceException {
      return getResource("viber-api");
    }

  }
}
