package com.eyelinecom.whoisd.sads2.vk.connector;

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
import com.eyelinecom.whoisd.sads2.input.InputFile;
import com.eyelinecom.whoisd.sads2.input.InputLocation;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.session.SessionManager;
import com.eyelinecom.whoisd.sads2.utils.ConnectorUtils;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkAttachment;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkDoc;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkObject;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkPhoto;
import com.eyelinecom.whoisd.sads2.vk.registry.VkServiceRegistry;
import com.eyelinecom.whoisd.sads2.vk.resource.VkApi;
import com.eyelinecom.whoisd.sads2.vk.util.MarshalUtils;
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

import static com.eyelinecom.whoisd.sads2.Protocol.VKONTAKTE;
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
 * Date: 18.07.16
 * Time: 3:53
 * To change this template use File | Settings | File Templates.
 */
public class VkMessageConnector extends HttpServlet {

  private final static Log log = new Log4JLogger(org.apache.log4j.Logger.getLogger(VkMessageConnector.class));

  private VkMessageConnectorImpl connector;

  @Override
  public void destroy() {
    super.destroy();
    connector.destroy();
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    connector = new VkMessageConnectorImpl();

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

    final VkCallbackRequest request = new VkCallbackRequest(req);
    if ("confirmation".equals(request.getCallback().getType())) {
      // {"type":"confirmation","group_id":125262672}
      try {
        String confirmationCode = connector.getServiceRegistry().getConfirmationCode(request.getServiceId());
        // TODO: check group_id?
        resp.setStatus(200);
        resp.getWriter().print(confirmationCode);
      } catch (NotFoundResourceException e) {
        throw new RuntimeException(e);
      }
      return;
    }
    final SADSResponse response = connector.process(request);
    ConnectorUtils.fillHttpResponse(resp, response);
  }

  private class VkMessageConnectorImpl
    extends ProfileEnabledMessageConnector<VkCallbackRequest> {

    @Override
    protected SADSResponse buildQueuedResponse(VkCallbackRequest request, SADSRequest sadsRequest) {
      if ("confirmation".equals(request.getCallback().getType())) {
        String callbackConfirmationCode;
        try {
          callbackConfirmationCode = getServiceRegistry().getConfirmationCode(sadsRequest.getServiceId());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return buildCallbackResponse(200, callbackConfirmationCode);

      } else {
        return buildCallbackResponse(200, "ok");
      }
    }

    @Override
    protected SADSResponse buildQueueErrorResponse(Exception e,
                                                   VkCallbackRequest vkCallbackRequest,
                                                   SADSRequest sadsRequest) {
      return buildCallbackResponse(500, "");
    }

    @Override
    protected Log getLogger() {
      return log;
    }

    @Override
    protected String getSubscriberId(VkCallbackRequest req) throws Exception {
      if (req.getProfile() != null) {
        return req.getProfile().getWnumber();
      }

      final String userId = String.valueOf(req.getCallback().getObject().getUserId());
      final String incoming = req.getCallback().getObject().getBody();

      if (ChatCommand.match(getServiceId(req), incoming, VKONTAKTE) == CLEAR_PROFILE) {
        // Reset profile of the current user.
        final Profile profile = getProfileStorage()
            .query()
            .where(property("vkontakte", "id").eq(userId))
            .get();
        if (profile != null) {
          final boolean isDevModeEnabled = inProfile(profile).getDeveloperMode(req.getServiceId());
          if (isDevModeEnabled) {
            inProfile(profile).clear();
            inProfile(profile).setDeveloperMode(getServiceId(req), true);

            // Also clear the session.
            final SessionManager sessionManager = getSessionManager(VKONTAKTE, req.getServiceId());
            final Session session = sessionManager.getSession(profile.getWnumber(), false);
            if (session != null && !session.isClosed()) {
              session.close();
            }
          }
        }
      }

      final Profile profile = getProfileStorage()
        .query()
        .where(property("vkontakte", "id").eq(userId))
        .getOrCreate();

      req.setProfile(profile);
      return profile.getWnumber();
    }

    @Override
    protected String getServiceId(VkCallbackRequest req) throws Exception {
      return req.getServiceId();
    }

    @Override
    protected String getGateway() {
      return "Vkontakte";
    }

    @Override
    protected String getGatewayRequestDescription(VkCallbackRequest vkCallbackRequest) {
      return "Vkontakte";
    }

    @Override
    protected boolean isTerminated(VkCallbackRequest req) throws Exception {
      final String incoming = req.getCallback().getObject().getBody();

      final boolean isDevModeEnabled = req.getProfile() != null &&
          ProfileUtil.inProfile(req.getProfile()).getDeveloperMode(req.getServiceId());

      final ChatCommand command = ChatCommand.match(getServiceId(req), incoming, VKONTAKTE);
      return command == SET_DEVELOPER_MODE ||
          isDevModeEnabled && asList(SHOW_PROFILE, WHO_IS).contains(command);
    }

    @Override
    protected Protocol getRequestProtocol(ServiceConfig config, String subscriberId, VkCallbackRequest request) {
      return VKONTAKTE;
    }

    @Override
    protected String getRequestUri(ServiceConfig config,
                                   String wnumber,
                                   VkCallbackRequest message) throws Exception {

      final String serviceId = config.getId();
      final String incoming = message.getCallback().getObject().getBody();
      final SessionManager sessionManager = getSessionManager(serviceId);
      final Profile profile = getProfileStorage().find(wnumber);
      final boolean isDevModeEnabled = inProfile(profile).getDeveloperMode(serviceId);

      Session session = sessionManager.getSession(wnumber);

      final ChatCommand cmd = ChatCommand.match(serviceId, incoming, VKONTAKTE);
      if (cmd == INVALIDATE_SESSION && isDevModeEnabled) {
        // Invalidate the current session.
        session.close();
        session = sessionManager.getSession(wnumber);

      } else {
        final VkApi client = getClient();

        if (cmd == WHO_IS && isDevModeEnabled) {
          final String accessToken = VkServiceRegistry.getAccessToken(config.getAttributes());
          final Integer userId = message.getCallback().getObject().getUserId();
          final String groupId = getServiceRegistry().getGroupId(serviceId);

          final String msg =
              StringUtils.join(
                  new String[] {
                      "Chat URL: " + VkServiceRegistry.getChatUrl(groupId) + ".",
                      "Group ID: " + groupId + ".",
                      "Service: " + serviceId + ".",
                      "MiniApps host: " + getRootUri()
                  },
                  "\n");
          client.send(msg, userId, accessToken);

        } else if (cmd == SHOW_PROFILE && isDevModeEnabled) {
          final String accessToken = VkServiceRegistry.getAccessToken(config.getAttributes());
          final Integer userId = message.getCallback().getObject().getUserId();

          client.send(profile.dump(), userId, accessToken);

        } else if (cmd == SET_DEVELOPER_MODE) {
          final String value = ChatCommand.getCommandValue(incoming);
          final Boolean devMode = BooleanUtils.toBooleanObject(value);
          final String accessToken = VkServiceRegistry.getAccessToken(config.getAttributes());
          final Integer userId = message.getCallback().getObject().getUserId();

          if (devMode != null) {
            inProfile(profile).setDeveloperMode(serviceId, devMode);

            client.send(
                "Developer mode is " + (devMode ? "enabled" : "disabled") + ".",
                userId,
                accessToken);

          } else {
            client.send(
                "Developer mode is " +
                    (inProfile(profile).getDeveloperMode(serviceId) ? "enabled" : "disabled") +
                    ".",
                userId,
                accessToken);
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
    protected SADSResponse getOuterResponse(VkCallbackRequest vkCallbackRequest,
                                            SADSRequest request,
                                            SADSResponse response) {
      return buildCallbackResponse(200, "ok");
    }

    private SessionManager getSessionManager(String serviceId) throws Exception {
      return super.getSessionManager(VKONTAKTE, serviceId);
    }

    @Override
    protected void fillSADSRequest(SADSRequest sadsRequest, VkCallbackRequest request) {
      try {
        handleFileUpload(sadsRequest, request);

      } catch (Exception e) {
        getLog(request).error(e.getMessage(), e);
      }

      super.fillSADSRequest(sadsRequest, request);
    }

    @Override
    protected Profile getCachedProfile(VkCallbackRequest req) {
      return req.getProfile();
    }

    @Override
    protected Event getEvent(VkCallbackRequest req) {
      return req.getEvent();
    }

    private void handleFileUpload(SADSRequest sadsRequest, VkCallbackRequest req) throws Exception {
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

    private List<? extends AbstractInputType> extractMedia(VkCallbackRequest req) {
      final VkObject object = req.getCallback().getObject();
      final List<AbstractInputType> mediaList = new ArrayList<>();

      if (object.getGeo() != null && object.getGeo().hasCoordinates()) {
        final InputLocation location = new InputLocation();
        location.setLatitude(object.getGeo().getLatitude());
        location.setLongitude(object.getGeo().getLongitude());
        mediaList.add(location);
      }

      if (object.getAttachments() != null) {
        for (VkAttachment attachment : object.getAttachments()) {
          if (attachment.getPhoto() != null) {
            VkPhoto photo = attachment.getPhoto();
            final InputFile file = new InputFile();
            file.setMediaType("photo");
            file.setUrl(photo.getUrl());
            mediaList.add(file);
          }
          if (attachment.getDoc() != null) {
            VkDoc doc = attachment.getDoc();
            final InputFile file = new InputFile();
            file.setMediaType("document");
            file.setSize(doc.getSize());
            file.setUrl(doc.getUrl());
            mediaList.add(file);
          }
        }
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

    private VkServiceRegistry getServiceRegistry() throws NotFoundResourceException {
      return getResource("vkontakte-service-registry");
    }

    private VkApi getClient() throws NotFoundResourceException {
      return getResource("vkontakte-api");
    }

  }
}
