package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 18.07.16
 * Time: 6:37
 * To change this template use File | Settings | File Templates.
 */
public class VkCallBack {
  String type;
  @JsonProperty(value = "object")
  VkObject object;
  @JsonProperty(value = "group_id")
  Integer groupId;

  public String getType() {
    return type;
  }

  public VkObject getObject() {
    return object;
  }

  public Integer getGroupId() {
    return groupId;
  }
}
