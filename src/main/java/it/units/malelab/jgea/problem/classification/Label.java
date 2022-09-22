package it.units.malelab.jgea.problem.classification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Label<T> {

  private final T value;
  private final LabelFactory<T> parentFactory;

  private Label(T value, LabelFactory<T> parentFactory) {
    this.value = value;
    this.parentFactory = parentFactory;
  }

  public Collection<Label<T>> values() {
    return new HashSet<>(parentFactory.values);
  }

  @Override
  public String toString() {
    return value.toString();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Label<?> label = (Label<?>) o;
    return Objects.equals(value, label.value) && Objects.equals(parentFactory, label.parentFactory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, parentFactory);
  }

  static class LabelFactory<T> {

    private final Set<Label<T>> values;

    public LabelFactory(Collection<T> values) {
      this.values = values.stream().map(v -> new Label<>(v, this)).collect(Collectors.toSet());
    }

    public Label<T> getLabel(T value) {
      Label<T> label = new Label<>(value, this);
      if (!values.contains(label)) {
        throw new IllegalArgumentException(String.format(
            "Value requested not among available ones: %s.",
            values
        ));
      }
      return label;
    }

  }

  static class IntLabelFactory extends LabelFactory<Integer> {

    public IntLabelFactory(Collection<Integer> values) {
      super(values);
    }

    public IntLabelFactory(int maxValue) {
      super(IntStream.range(0, maxValue).boxed().collect(Collectors.toSet()));
    }

  }

}
