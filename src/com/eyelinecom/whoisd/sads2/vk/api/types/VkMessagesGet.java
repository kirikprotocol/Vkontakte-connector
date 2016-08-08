package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 08.08.16
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public class VkMessagesGet {

  @JsonProperty(value = "response")
  Response response;

  @JsonProperty(value = "error")
  VkError error;

  public boolean isError() {
    return error != null;
  }

  public String getErrorMessage() {
    return error == null ? null : error.errorMessage;
  }

  public static class Response {
    @JsonProperty(value = "count")
    Integer count;

    @JsonProperty(value = "items")
    Item[] items;
  }

  public static class Item {
    @JsonProperty(value = "id")
    Integer id;

    @JsonProperty(value = "user_id")
    Integer userId;

    @JsonProperty(value = "body")
    String body;
  }


}
