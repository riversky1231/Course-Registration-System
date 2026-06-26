package com.codeying.stuselect.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CourseTimeSlot {

  private static final Pattern SLOT_PATTERN =
      Pattern.compile(
          "(?:周|星期|礼拜)?\\s*([一二三四五六日天1-7１-７])\\s*"
              + "(?:第)?\\s*([0-9０-９一二三四五六七八九十]+)\\s*"
              + "(?:[-~～至到]\\s*([0-9０-９一二三四五六七八九十]+))?\\s*节");

  private CourseTimeSlot() {}

  static boolean overlaps(final String left, final String right) {
    if (isBlank(left) || isBlank(right)) {
      return false;
    }
    List<Slot> leftSlots = parse(left);
    List<Slot> rightSlots = parse(right);
    if (leftSlots.isEmpty() || rightSlots.isEmpty()) {
      return left.trim().equals(right.trim());
    }
    for (Slot leftSlot : leftSlots) {
      for (Slot rightSlot : rightSlots) {
        if (leftSlot.overlaps(rightSlot)) {
          return true;
        }
      }
    }
    return false;
  }

  static List<Slot> parse(final String value) {
    if (isBlank(value)) {
      return List.of();
    }
    List<Slot> slots = new ArrayList<>();
    Matcher matcher = SLOT_PATTERN.matcher(value);
    while (matcher.find()) {
      int day = parseDay(matcher.group(1));
      int start = parseNumber(matcher.group(2));
      int end = matcher.group(3) == null ? start : parseNumber(matcher.group(3));
      if (day > 0 && start > 0 && end > 0) {
        slots.add(new Slot(day, Math.min(start, end), Math.max(start, end)));
      }
    }
    return List.copyOf(slots);
  }

  private static int parseDay(final String value) {
    return switch (value) {
      case "一", "1", "１" -> 1;
      case "二", "2", "２" -> 2;
      case "三", "3", "３" -> 3;
      case "四", "4", "４" -> 4;
      case "五", "5", "５" -> 5;
      case "六", "6", "６" -> 6;
      case "日", "天", "7", "７" -> 7;
      default -> -1;
    };
  }

  private static int parseNumber(final String value) {
    String normalized = normalizeDigits(value);
    try {
      return Integer.parseInt(normalized);
    } catch (NumberFormatException ignored) {
      return parseChineseNumber(normalized);
    }
  }

  private static String normalizeDigits(final String value) {
    StringBuilder builder = new StringBuilder();
    for (char ch : value.toCharArray()) {
      if (ch >= '０' && ch <= '９') {
        builder.append((char) ('0' + ch - '０'));
      } else {
        builder.append(ch);
      }
    }
    return builder.toString();
  }

  private static int parseChineseNumber(final String value) {
    if ("十".equals(value)) {
      return 10;
    }
    int tenIndex = value.indexOf('十');
    if (tenIndex >= 0) {
      int tens = tenIndex == 0 ? 1 : chineseDigit(value.charAt(tenIndex - 1));
      int ones = tenIndex == value.length() - 1 ? 0 : chineseDigit(value.charAt(tenIndex + 1));
      return tens > 0 && ones >= 0 ? tens * 10 + ones : -1;
    }
    return value.length() == 1 ? chineseDigit(value.charAt(0)) : -1;
  }

  private static int chineseDigit(final char ch) {
    return switch (ch) {
      case '一' -> 1;
      case '二' -> 2;
      case '三' -> 3;
      case '四' -> 4;
      case '五' -> 5;
      case '六' -> 6;
      case '七' -> 7;
      case '八' -> 8;
      case '九' -> 9;
      default -> -1;
    };
  }

  private static boolean isBlank(final String value) {
    return value == null || value.trim().isEmpty();
  }

  record Slot(int day, int start, int end) {

    boolean overlaps(final Slot other) {
      return day == other.day && start <= other.end && other.start <= end;
    }
  }
}
