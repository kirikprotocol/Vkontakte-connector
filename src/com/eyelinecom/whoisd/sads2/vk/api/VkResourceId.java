package com.eyelinecom.whoisd.sads2.vk.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 03.08.16
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class VkResourceId {


  private static final Pattern PATTERN = Pattern.compile("(photo|audio|doc|video)(\\-?\\d+)_(\\d+)_?([0-9a-fA-F]+)?");
  private final Type type;
  private final int id;
  private final int ownerId;
  private final String accessKey;

  public VkResourceId(Type type, int id, int ownerId, String accessKey) {
    this.type = type;
    this.id = id;
    this.ownerId = ownerId;
    this.accessKey = accessKey;
  }

  public static VkResourceId get(String s) {
    //System.out.println(Arrays.toString(Type.values()));
    if (s == null) return null;
    Matcher m = PATTERN.matcher(s);
    if (!m.matches()) return null;
    Type type = Type.get(m.group(1));
    int ownerId = Integer.parseInt(m.group(2));
    int id = Integer.parseInt(m.group(3));
    return new VkResourceId(type, id, ownerId, m.group(4));
  }

  public String getAccessKey() {
    return accessKey;
  }

  public int getId() {
    return id;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return type.name() + "" + ownerId + "_" + id + (accessKey != null ? "_" + accessKey : "");
  }

  public static enum Type {
    photo,
    audio,
    doc,
    video;

    private Type() {
      MapHolder.map.put(name(), this);
    }

    public static Type get(String type) {
      return MapHolder.map.get(type);
    }

    private static class MapHolder {
      public static final Map<String, Type> map = new HashMap<>();
    }

  }
}
