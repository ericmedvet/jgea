
package io.github.ericmedvet.jgea.experimenter.builders;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.function.Function;
import java.util.logging.Logger;

public class Functions {

  private final static Logger L = Logger.getLogger(Functions.class.getName());

  private Functions() {
  }

  @SuppressWarnings("unused")
  public static Function<String, Object> fromBase64() {
    return s -> {
      try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(s)); ObjectInputStream ois =
          new ObjectInputStream(bais)) {
        return ois.readObject();
      } catch (Throwable t) {
        L.warning("Cannot deserialize: %s".formatted(t));
        return null;
      }
    };
  }

  @SuppressWarnings("unused")
  public static <T> Function<T, T> identity() {
    return t -> t;
  }
}
