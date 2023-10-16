package io.github.ericmedvet.jgea.sample;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
public class Args {

  private final static String PIECES_SEP = "-";
  private final static String OPTIONS_SEP = ",";
  private final static String RANGE_SEP = ":";
  private final static String KEYVAL_SEP = "=";

  private Args() {  }

  public static String a(String[] args, String name, String defaultValue) {
    for (String arg : args) {
      String[] pieces = arg.split(KEYVAL_SEP, 2);
      if (pieces[0].equals(name)) {
        return pieces[1];
      }
    }
    return defaultValue;
  }

  public static boolean b(String s) {
    return Boolean.parseBoolean(s);
  }

  public static List<Boolean> b(List<String> strings) {
    return strings.stream().map(Boolean::parseBoolean).toList();
  }

  public static double d(String s) {
    return Double.parseDouble(s);
  }

  public static List<Double> d(List<String> strings) {
    return strings.stream().map(Double::parseDouble).toList();
  }

  public static int i(String s) {
    return Integer.parseInt(s);
  }

  public static List<Integer> i(List<String> strings) {
    return strings.stream().map(Integer::parseInt).toList();
  }

  public static List<String> l(String s) {
    return Arrays.stream(s.split(OPTIONS_SEP)).toList();
  }

  public static String p(String s, int n) {
    String[] pieces = s.split(PIECES_SEP);
    if (n < pieces.length) {
      return pieces[n];
    }
    return null;
  }

  public static int[] ri(String s) {
    String[] pieces = s.split(RANGE_SEP);
    if (pieces.length > 1) {
      return IntStream.range(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1])).toArray();
    } else {
      return new int[]{Integer.parseInt(pieces[0])};
    }
  }

}
