package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import io.github.ericmedvet.jnb.core.StringParser;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
  private final static Logger L = Logger.getLogger(Utils.class.getName());

  private final static String FORMAT_REGEX = "%\\d*\\.*\\d*[sdf]";
  private final static String MAP_KEYS_REGEX = "[A-Za-z][A-Za-z0-9_]*";
  private final static Pattern INTERPOLATOR =
      Pattern.compile("\\{(?<mapKeys>" + MAP_KEYS_REGEX + "(\\." + MAP_KEYS_REGEX + ")*)(:(?<format>" + FORMAT_REGEX + "))?\\}");
  ;

  private Utils() {
  }

  public static Object getKeyFromParamMap(ParamMap paramMap, List<String> keyPieces) {
    if (keyPieces.size() == 1) {
      return paramMap.value(keyPieces.get(0));
    }
    NamedParamMap namedParamMap = paramMap.npm(keyPieces.get(0));
    if (namedParamMap == null) {
      return null;
    }
    return getKeyFromParamMap(namedParamMap, keyPieces.subList(1, keyPieces.size()));
  }

  public static String interpolate(String format, ParamMap map) {
    Matcher m = INTERPOLATOR.matcher(format);
    StringBuilder sb = new StringBuilder();
    int c = 0;
    while (m.find(c)) {
      sb.append(format, c, m.start());
      try {
        String mapKeys = m.group("mapKeys");
        String f = m.group("format") != null ? m.group("format") : "%s";
        Object v = getKeyFromParamMap(map, Arrays.stream(mapKeys.split("\\.")).toList());
        sb.append(f.formatted(v));
      } catch (RuntimeException e) {
        L.warning("Cannot interpolate name: %s".formatted(e));
        sb.append("I_ERR");
      }
      c = m.end();
    }
    sb.append(format, c, format.length());
    return sb.toString();
  }

  public static ParamMap augumentWith(ParamMap map, String key, Object value) {
    //build a new MapNamedParamMap with all the things
    if (value instanceof Number) {
      return map; // TODO
    }
    throw new IllegalArgumentException("Unsupported object type %s".formatted(value.getClass().getSimpleName()))
  }

  public static void main(String[] args) {
    ParamMap m = StringParser.parse("person(name=Eric)");
    System.out.println(m);
    if (m instanceof MapNamedParamMap mm) {

    }
  }

}
