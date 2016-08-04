package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 18.07.16
 * Time: 6:38
 * To change this template use File | Settings | File Templates.
 */
public class VkObject {
  private Integer id;
  private Integer date;
  private Integer out;
  @JsonProperty(value = "user_id")
  private Integer userId;
  @JsonProperty(value = "read_state")
  private Integer readState;
  private String title;
  private String body;

  @JsonProperty(value = "geo")
  private VkGeo geo;

  @JsonProperty(value = "attachments")
  private VkAttachment[] attachments;


  public Integer getUserId() {
    return userId;
  }

  public String getBody() {
    return body;
  }

  public VkGeo getGeo() {
    return geo;
  }

  public VkAttachment[] getAttachments() {
    return attachments;
  }
}
