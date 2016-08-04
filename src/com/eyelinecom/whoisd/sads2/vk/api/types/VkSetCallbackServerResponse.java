package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 01.08.16
 * Time: 22:46
 * To change this template use File | Settings | File Templates.
 */
public class VkSetCallbackServerResponse {

  @JsonProperty(value = "response")
  private VkSetCallbackServer response;

  public VkSetCallbackServer getResponse() {
    return response;
  }
}
