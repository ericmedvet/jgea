/*
 * Copyright (C) 2019 Eric Medvet <eric.medvet@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
