package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */
public class VkSaveMessagesPhotoResponse {

  @JsonProperty(value = "response")
  VkSaveMessagesPhoto[] response;

  public VkSaveMessagesPhoto[] getResponse() {
    return response;
  }

  @Override
  public String toString() {
    return "VkSaveMessagesPhotoResponse{" +
      "response=" + (response == null ? null : Arrays.asList(response)) +
      '}';
  }
}
