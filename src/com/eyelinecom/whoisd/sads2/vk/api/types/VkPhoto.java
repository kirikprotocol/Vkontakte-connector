package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 02.08.16
 * Time: 9:50
 * To change this template use File | Settings | File Templates.
 */
public class VkPhoto {
  @JsonProperty(value = "id")
  private Integer id;

  @JsonProperty(value = "album_id")
  private Integer albumId;

  @JsonProperty(value = "owner_id")
  private Integer ownerId;

  @JsonProperty(value = "photo_75")
  private String photo75;

  @JsonProperty(value = "photo_130")
  private String photo130;

  @JsonProperty(value = "photo_604")
  private String photo604;

  @JsonProperty(value = "width")
  private Integer width;

  @JsonProperty(value = "height")
  private Integer height;

  @JsonProperty(value = "access_key")
  private String access_key;

  public String getUrl() {
    if (photo604 != null) return photo604;
    if (photo130 != null) return photo130;
    if (photo75 != null) return photo75;
    return null;
  }
}
