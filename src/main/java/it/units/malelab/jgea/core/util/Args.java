/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.units.malelab.jgea.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class Args {

  private final static String PIECES_SEP = "-";
  private final static String OPTIONS_SEP = ",";
  private final static String RANGE_SEP = ":";
  private final static String KEYVAL_SEP = "=";

  private Args() {
    /* prevent instantiation */
  }

  public static String p(String s, int n) {
    String[] pieces = s.split(PIECES_SEP);
    if (n < pieces.length) {
      return pieces[n];
    }
    return null;
  }

  public static int i(String s) {
    return Integer.parseInt(s);
  }

  public static boolean b(String s) {
    return Boolean.parseBoolean(s);
  }

  public static double d(String s) {
    return Double.parseDouble(s);
  }

  public static String a(String[] args, String name, String defaultValue) {
    for (String arg : args) {
      String[] pieces = arg.split(KEYVAL_SEP);
      if (pieces[0].equals(name)) {
        return pieces[1];
      }
    }
    return defaultValue;
  }

  public static int[] ri(String s) {
    String[] pieces = s.split(RANGE_SEP);
    if (pieces.length > 1) {
      return IntStream.range(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1])).toArray();
    } else {
      return new int[]{Integer.parseInt(pieces[0])};
    }
  }

  public static List<String> l(String s) {
    return Arrays.stream(s.split(OPTIONS_SEP)).collect(Collectors.toList());
  }

  public static List<Integer> i(List<String> strings) {
    return strings.stream().map(Integer::parseInt).collect(Collectors.toList());
  }

  public static List<Double> d(List<String> strings) {
    return strings.stream().map(Double::parseDouble).collect(Collectors.toList());
  }

  public static List<Boolean> b(List<String> strings) {
    return strings.stream().map(Boolean::parseBoolean).collect(Collectors.toList());
  }

}
