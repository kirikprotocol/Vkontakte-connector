package com.eyelinecom.whoisd.sads2.vk.registry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 04.08.16
 * Time: 2:12
 * To change this template use File | Settings | File Templates.
 */
public class VkBotSettings {
  private static final Pattern PATTERN = Pattern.compile("(\\d+):([0-9a-fA-F]+):?([0-9a-fA-F]+)?");

  public final String accessToken;
  public final int groupId;
  public final String confirmationCode;

  private VkBotSettings(String accessToken, int groupId, String confirmationCode) {
    this.accessToken = accessToken;
    this.groupId = groupId;
    this.confirmationCode = confirmationCode;
  }

  public static VkBotSettings get(String value) {
    if (value == null) return null;
    Matcher m = PATTERN.matcher(value);
    if (!m.matches()) return null;
    return new VkBotSettings(m.group(2), Integer.parseInt(m.group(1)), m.group(3));
  }
}
