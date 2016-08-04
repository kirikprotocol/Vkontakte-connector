package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 02.08.16
 * Time: 9:35
 * To change this template use File | Settings | File Templates.
 */
public class VkDoc {

  @JsonProperty(value = "id")
  private Integer id;

  @JsonProperty(value = "owner_id")
  private Integer ownerId;

  @JsonProperty(value = "title")
  private String title;

  @JsonProperty(value = "size")
  private Integer size;

  @JsonProperty(value = "ext")
  private String ext;

  @JsonProperty(value = "url")
  private String url;

  @JsonProperty(value = "access_key")
  private String access_key;

  public Integer getSize() {
    return size;
  }

  public String getUrl() {
    return url;
  }
}
