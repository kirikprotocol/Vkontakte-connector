package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 02.08.16
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class VkCallbackServerSettings {

  @JsonProperty(value = "response")
  Response response;

  public static class Response {

    @JsonProperty(value = "server_url")
    String serverUrl;

  }

  public String getServerUrl() {
    return response.serverUrl;
  }

}
