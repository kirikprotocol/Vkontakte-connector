package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 02.08.16
 * Time: 9:32
 * To change this template use File | Settings | File Templates.
 */
public class VkAttachment {

  @JsonProperty(value = "type")
  private String type;

  @JsonProperty(value = "doc")
  private VkDoc doc;

  @JsonProperty(value = "photo")
  private VkPhoto photo;

  public VkDoc getDoc() {
    return doc;
  }

  public VkPhoto getPhoto() {
    return photo;
  }
}
