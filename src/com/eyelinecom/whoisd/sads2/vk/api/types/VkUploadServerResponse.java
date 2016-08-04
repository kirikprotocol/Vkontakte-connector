package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 18:54
 * To change this template use File | Settings | File Templates.
 */
public class VkUploadServerResponse {

  @JsonProperty(value = "response")
  VkUploadServer response;

  @JsonProperty(value = "error")
  VkError error;

  @Override
  public String toString() {
    return "VkUploadServerResponse{" +
      "error=" + error +
      ", response=" + response +
      '}';
  }

  public boolean isError() {
    return error != null;
  }

  public VkUploadServer getResponse() {
    return response;
  }
}
