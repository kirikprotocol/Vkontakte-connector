package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 16.08.16
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
public class VkUsersGet {
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

    @JsonProperty(value = "users")
    User[] users;
  }

  public static class User {
    @JsonProperty(value = "id")
    Integer id;

    @JsonProperty(value = "role")
    String role;
  }

}
