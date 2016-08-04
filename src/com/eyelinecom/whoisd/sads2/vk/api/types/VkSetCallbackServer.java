package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 01.08.16
 * Time: 22:46
 * To change this template use File | Settings | File Templates.
 */
public class VkSetCallbackServer {

  @JsonProperty(value = "state_code")
  private Integer stateCode;

  @JsonProperty(value = "state")
  private String state;

  public int getStateCode() {
    return stateCode;
  }
}
