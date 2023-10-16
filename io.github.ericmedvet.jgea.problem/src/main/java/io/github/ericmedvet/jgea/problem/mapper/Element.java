
package io.github.ericmedvet.jgea.problem.mapper;
public interface Element {

  enum MapperFunction implements Element {

    SIZE("size"), WEIGHT("weight"), WEIGHT_R("weight_r"), INT("int"), ADD("+"), SUBTRACT("-"), MULT("*"), DIVIDE("/")
    , REMAINDER(
        "%"), LENGTH("length"), MAX_INDEX("max_index"), MIN_INDEX("min_index"), GET("get"), SEQ("seq"), REPEAT(
        "repeat"), ROTATE_DX(
        "rotate_dx"), ROTATE_SX("rotate_sx"), SUBSTRING("substring"), SPLIT("split"), SPLIT_W("split_w"), APPLY(
        "apply");

    private final String grammarName;

    MapperFunction(String grammarName) {
      this.grammarName = grammarName;
    }

    public String getGrammarName() {
      return grammarName;
    }

  }

  enum Variable implements Element {
    GENOTYPE("g"), LIST_N("ln"), DEPTH("depth"), GL_COUNT_R("g_count_r"), GL_COUNT_RW("g_count_rw");

    private final String grammarName;

    Variable(String grammarName) {
      this.grammarName = grammarName;
    }

    public String getGrammarName() {
      return grammarName;
    }

  }

  record NumericConstant(double value) implements Element {

    @Override
    public String toString() {
      return String.format("%f", value);
    }

  }

  String toString();
}
