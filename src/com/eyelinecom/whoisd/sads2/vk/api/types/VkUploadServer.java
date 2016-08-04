package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 18:58
 * To change this template use File | Settings | File Templates.
 */
public class VkUploadServer {
  @JsonProperty(value = "upload_url")
  String uploadUrl;

  @JsonProperty(value = "album_id")
  Integer albumId;

  @JsonProperty(value = "group_id")
  Integer groupId;

  @Override
  public String toString() {
    return "VkUploadServer{" +
      "albumId=" + albumId +
      ", uploadUrl='" + uploadUrl + '\'' +
      ", groupId=" + groupId +
      '}';
  }

  public String getUploadUrl() {
    return uploadUrl;
  }
}
