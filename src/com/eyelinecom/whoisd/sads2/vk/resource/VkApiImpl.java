package com.eyelinecom.whoisd.sads2.vk.resource;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Loader;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkCallbackServerSettings;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkSaveMessagesPhoto;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkSaveMessagesPhotoResponse;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkSetCallbackServerResponse;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkUploadResponse;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkUploadServerResponse;
import com.eyelinecom.whoisd.sads2.vk.connector.VkCallbackRequest;
import com.eyelinecom.whoisd.sads2.vk.util.MarshalUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 18.07.16
 * Time: 7:11
 * To change this template use File | Settings | File Templates.
 */
public class VkApiImpl implements VkApi {

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VkApiImpl.class);

  private static final AtomicLong guidGenerator = new AtomicLong(System.currentTimeMillis());

  private final HttpDataLoader loader;
  private final Properties properties;
  private final String connectorUrl;

  public VkApiImpl(HttpDataLoader loader, Properties properties) {
    this.loader = loader;
    this.properties = properties;
    this.connectorUrl = properties.getProperty("connector.url");
  }

  @Override
  public void send(String message, int userId, String accessToken) {
    try {
      Loader.Entity data = loader.load("https://api.vk.com/method/messages.send?message=" + message + "&user_id=" + userId +
        "&guid=" + guidGenerator.getAndIncrement() + "&access_token=" + accessToken + "&v=5.0");
      // TODO: check response?
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void send(String message, int userId, String accessToken, String latitude, String longitude) {
    try {
      Loader.Entity data = loader.load("https://api.vk.com/method/messages.send?message=" + message + "&user_id=" + userId +
        "&lat=" + latitude + "&long=" + longitude +
        "&guid=" + guidGenerator.getAndIncrement() + "&access_token=" + accessToken + "&v=5.0");
      // TODO: check response?
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String connectorUrl() {
    return connectorUrl;
  }

  @Override
  public String getCallbackConfirmationCode(String token, String groupId) {
    try {
      Loader.Entity data = loader.load("https://api.vk.com/method/groups.getCallbackConfirmationCode?group_id=" + groupId + "&access_token=" + token + "&v=5.0");
      String responseString = new String(data.getBuffer());
      // TODO: proper parsing
      Pattern p = Pattern.compile("\"code\"\\s*:\\s*\"([0-9a-f]+)\"");
      Matcher m = p.matcher(responseString);
      if (m.find()) {
        return m.group(1);
      } else throw new RuntimeException("No confirmation code: " + responseString);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getCallbackServer(String token, String groupId) {
    try {
      Loader.Entity data = loader.load("https://api.vk.com/method/groups.getCallbackServerSettings?group_id=" + groupId + "&access_token=" + token + "&v=5.0");
      String json = new String(data.getBuffer());// {"response":{"server_url":"http:\/\/109.174.72.36\/vkontakte\/alfa.test","secret_key":""}}
      log.debug("getCallbackServer response: " + json);
      VkCallbackServerSettings settings = MarshalUtils.unmarshal(json, VkCallbackServerSettings.class);
      return settings.getServerUrl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String uploadPhoto(String token, byte[] data) {
    try {

      Loader.Entity entity;
      entity = loader.load("https://api.vk.com/method/photos.getMessagesUploadServer?access_token=" + token + "&v=5.0");
      String result = new String(entity.getBuffer());
      VkUploadServerResponse uploadServerResponse = VkCallbackRequest.readUploadServerResponse(result);
      log.debug("uploadServerResponse: " + uploadServerResponse);
      if (uploadServerResponse.isError()) return null;
      String uploadUrl = uploadServerResponse.getResponse().getUploadUrl();
      log.debug("uploadUrl: " + uploadUrl);

      ByteArrayPartSource partSource = new ByteArrayPartSource("image.jpg", data);
      FilePart part = new FilePart("photo", partSource, "image/jpeg", "UTF-8");
      ArrayList<Part> parts = new ArrayList<>();
      Map<String, String> parameters = UrlUtils.getParametersMap(uploadUrl);
      for (Map.Entry<String, String> en : parameters.entrySet()) {
        parts.add(new StringPart(en.getKey(), en.getValue()));
      }
      parts.add(part);
      entity = loader.postMultipart(uploadUrl, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), parts);
      result = new String(entity.getBuffer());
      log.debug("upload result: " + result);

      VkUploadResponse uploadResponse = VkCallbackRequest.readUploadResponse(result);
      log.debug("uploadResponse: " + uploadResponse);
      if (uploadResponse.getPhoto().equals("[]")) return null;

      entity = loader.load("https://api.vk.com/method/photos.saveMessagesPhoto?access_token=" + token + "&v=5.0" +
            "&server=" + uploadResponse.getServer() +
            "&hash=" + uploadResponse.getHash() +
            "&photo=" + uploadResponse.getPhoto());
      result = new String(entity.getBuffer());
      log.debug("photos.saveMessagesPhoto: " + result);
      VkSaveMessagesPhotoResponse saveMessagesPhotoResponse =  VkCallbackRequest.readSaveMessagesPhoto(result);
      log.debug("saveMessagesPhotoResponse: " + saveMessagesPhotoResponse);

      VkSaveMessagesPhoto[] photos = saveMessagesPhotoResponse.getResponse();
      if (photos == null || photos.length == 0) return null;
      int id = photos[0].getId();
      int ownerId = photos[0].getOwnerId();
      return "photo" + ownerId + "_" + id + "_" + token;
    } catch (Exception e) {
      log.error("Failed to upload photo", e);
      return null;
    }
  }

  @Override
  public int setCallbackServer(String token, String groupId, String url) {
    try {
      String apiUrl = "https://api.vk.com/method/groups.setCallbackServer?group_id=" + groupId +
        "&server_url=" + URLEncoder.encode(url, "UTF-8") + "&access_token=" + token + "&v=5.0";
      Loader.Entity data = loader.load(apiUrl);
      String responseString = new String(data.getBuffer());
      log.debug("groups.setCallbackServer response: " + responseString);
      VkSetCallbackServerResponse response = VkCallbackRequest.readSetCallbackServerResponse(responseString);
      // TODO: chekc response
      return response.getResponse().getStateCode();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendAttachment(String attachmentId, int userId, String accessToken) {
    try {
      Loader.Entity data = loader.load("https://api.vk.com/method/messages.send?attachment=" +
        attachmentId + "&user_id=" + userId + "&guid=" + guidGenerator.getAndIncrement() +
        "&access_token=" + accessToken + "&v=5.0");
      log.debug("send attachment result: " + new String(data.getBuffer()));
      // TODO: check response?
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class Factory implements ResourceFactory {

    @Override
    public Object build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
      final HttpDataLoader loader = SADSInitUtils.getResource("loader", properties);
      return new VkApiImpl(loader, properties);
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }
}
