package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */
public class VkSaveMessagesPhoto {

  @JsonProperty(value = "id")
  Integer id;

  @JsonProperty(value = "album_id")
  Integer albumId;

  @JsonProperty(value = "owner_id")
  Integer ownerId;

  @JsonProperty(value = "user_id")
  Integer userId;

  @Override
  public String toString() {
    return "VkSaveMessagesPhoto{" +
      "albumId=" + albumId +
      ", id=" + id +
      ", ownerId=" + ownerId +
      ", userId=" + userId +
      '}';
  }

  public Integer getAlbumId() {
    return albumId;
  }

  public Integer getId() {
    return id;
  }

  public Integer getOwnerId() {
    return ownerId;
  }

  public Integer getUserId() {
    return userId;
  }
}
