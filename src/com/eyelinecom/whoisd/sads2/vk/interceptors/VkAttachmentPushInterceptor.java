package com.eyelinecom.whoisd.sads2.vk.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.attachments.Attachment;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.session.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.vk.api.VkResourceId;
import com.eyelinecom.whoisd.sads2.vk.registry.VkServiceRegistry;
import com.eyelinecom.whoisd.sads2.vk.resource.VkApi;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.logging.Log;
import org.dom4j.Document;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.eyelinecom.whoisd.sads2.content.attachments.Attachment.Type.fromString;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 29.07.16
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class VkAttachmentPushInterceptor extends BlankInterceptor implements Initable {

  private static final org.apache.log4j.Logger globalLog = org.apache.log4j.Logger.getLogger(VkAttachmentPushInterceptor.class);

  private static final Cache<String, String> uploadPhotoCache = CacheBuilder.newBuilder()
    .maximumSize(100000)
    .expireAfterAccess(7, TimeUnit.DAYS)
    .removalListener(new RemovalListener<String, String>() {
      @Override
      public void onRemoval(RemovalNotification<String, String> notification) {
        globalLog.info("Removing from cache entry for " + notification.getKey() + ":" + notification.getValue());
      }
    }).build();


  private VkApi client;
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

    String token = VkServiceRegistry.getAccessToken(request.getServiceScenario().getAttributes());
    Integer userId = Integer.parseInt(request.getProfile().property("vkontakte", "id").getValue());

    for (Attachment attachment : attachments) {
      Attachment.Type type = fromString(attachment.getType());
      VkResourceId resourceId = VkResourceId.get(attachment.getSrc());
      switch (type) {
        case PHOTO:
          if (resourceId == null) resourceId = uploadPhoto(log, token, request.getResourceURI(), attachment);
          if (resourceId != null) client.sendAttachment(resourceId.toString(), userId, token);
          break;
        case VIDEO:
          client.sendAttachment(attachment.getSrc(), userId, token);
          break;
        case AUDIO:
          client.sendAttachment(attachment.getSrc(), userId, token);
          break;
        case DOCUMENT:
          client.sendAttachment(attachment.getSrc(), userId, token);
          break;
        case LOCATION:
          client.send("", userId, token, attachment.getLatitude(), attachment.getLongitude());
          break;
      }
      //client.send(vkAttachment);


    }
  }

  private VkResourceId uploadPhoto(final Log log, final String token, String resourceURI, Attachment attachment) {
    log.debug("Downloading photo src=\"" + attachment.getSrc() + "\"");

    FileUpload.ByteFileUpload upload = attachment.asFileUpload(globalLog, loader, resourceURI);
    if (upload == null || upload.getBytes() == null) return null;
    final byte[] data = upload.getBytes();
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    md.update(data, 0, data.length);
    final String md5hash = bytesToHex(md.digest());
    log.debug("Doanloaded image " + attachment.getSrc() + ": " + md5hash);
    try {
      String photoId = uploadPhotoCache.get(md5hash, new Callable<String>() {
        @Override
        public String call() throws Exception {
          String photoId = client.uploadPhoto(token, data);
          if (photoId == null) throw new RuntimeException("Failed to upload photo");
          globalLog.debug("Adding to vk upload photo cache entry: " + md5hash + ":" + photoId);
          return photoId;
        }
      });
      log.debug("Got photoId: " + photoId + " for image " + md5hash);
      return VkResourceId.get(photoId);
    } catch (ExecutionException e) {
      log.error("", e);
      return null;
    }
  }


  @Override
  public void init(Properties config) throws Exception {
    client = (VkApi) SADSInitUtils.getResource("client", config);
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
