package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 19:02
 * To change this template use File | Settings | File Templates.
 */
public class VkError {

  @JsonProperty(value = "error_code")
  Integer errorCode;

  @JsonProperty(value = "error_msg")
  String errorMessage;

  @Override
  public String toString() {
    return "VkError{" +
      "errorCode=" + errorCode +
      ", errorMessage='" + errorMessage + '\'' +
      '}';
  }
}
