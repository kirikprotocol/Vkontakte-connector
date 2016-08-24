package com.eyelinecom.whoisd.sads2.vk.connector;

import com.eyelinecom.whoisd.sads2.common.StoredHttpRequest;
import com.eyelinecom.whoisd.sads2.events.Event;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkCallBack;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkSaveMessagesPhotoResponse;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkSetCallbackServerResponse;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkUploadResponse;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkUploadServerResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 18.07.16
 * Time: 4:01
 * To change this template use File | Settings | File Templates.
 */
public class VkCallbackRequest extends StoredHttpRequest {

  private final String serviceId;
  private final VkCallBack callback;

  private transient Profile profile;
  private transient Event event;

  public VkCallbackRequest(HttpServletRequest request) throws IOException {
    super(request);
    final String[] parts = getRequestURI().split("/");
    serviceId = parts[parts.length - 1];
    String content = getContent();
    callback = readCallback(content);
  }

  public VkCallBack getCallback() {
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

  public static VkCallBack readCallback(String json) throws IOException {
    return mapper.readerFor(VkCallBack.class).readValue(mapper.readTree(json));
  }

  public static VkUploadServerResponse readUploadServerResponse(String json) throws IOException {
    return mapper.readerFor(VkUploadServerResponse.class).readValue(mapper.readTree(json));
  }

  public static VkUploadResponse readUploadResponse(String json) throws IOException {
    return mapper.readerFor(VkUploadResponse.class).readValue(mapper.readTree(json));
  }

  public static VkSaveMessagesPhotoResponse readSaveMessagesPhoto(String json) throws IOException {
    return mapper.readerFor(VkSaveMessagesPhotoResponse.class).readValue(mapper.readTree(json));
  }


  public static VkSetCallbackServerResponse readSetCallbackServerResponse(String json) throws IOException {
    return mapper.readerFor(VkSetCallbackServerResponse.class).readValue(mapper.readTree(json));
  }

}
