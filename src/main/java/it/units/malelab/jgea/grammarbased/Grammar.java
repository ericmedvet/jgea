/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class Grammar<T> implements Serializable {

  public static final String RULE_ASSIGNMENT_STRING = "::=";
  public static final String RULE_OPTION_SEPARATOR_STRING = "|";

  private T startingSymbol;
  private Map<T, List<List<T>>> rules;

  public Grammar() {
    rules = new LinkedHashMap<>();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, List<List<T>>> rule : rules.entrySet()) {
      sb.append(rule.getKey())
              .append(" ")
              .append(rule.getKey().equals(startingSymbol) ? "*" : "")
              .append(RULE_ASSIGNMENT_STRING + " ");
      for (List<T> option : rule.getValue()) {
        for (T symbol : option) {
          sb.append(symbol)
                  .append(" ");
        }
        sb.append(RULE_OPTION_SEPARATOR_STRING + " ");
      }
      sb.delete(sb.length() - 2 - RULE_OPTION_SEPARATOR_STRING.length(), sb.length());
      sb.append("\n");
    }
    return sb.toString();
  }

  public T getStartingSymbol() {
    return startingSymbol;
  }

  public void setStartingSymbol(T startingSymbol) {
    this.startingSymbol = startingSymbol;
  }

  public Map<T, List<List<T>>> getRules() {
    return rules;
  }
  
  public static Grammar<String> fromFile(File file) throws FileNotFoundException, IOException {
    return fromFile(file, "UTF-8");
  }

  public static Grammar<String> fromFile(File file, String charset) throws FileNotFoundException, IOException {
    Grammar<String> grammar = new Grammar<>();
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    String line;
    while ((line = br.readLine()) != null) {
      String[] components = line.split(Pattern.quote(RULE_ASSIGNMENT_STRING));
      String toReplaceSymbol = components[0].trim();
      String[] optionStrings = components[1].split(Pattern.quote(RULE_OPTION_SEPARATOR_STRING));
      if (grammar.getStartingSymbol() == null) {
        grammar.setStartingSymbol(toReplaceSymbol);
      }
      List<List<String>> options = new ArrayList<>();
      for (String optionString : optionStrings) {
        List<String> symbols = new ArrayList<>();
        for (String symbol : optionString.split("\\s+")) {
          if (!symbol.trim().isEmpty()) {
            symbols.add(symbol.trim());
          }
        }
        if (!symbols.isEmpty()) {
          options.add(symbols);
        }
      }
      grammar.getRules().put(toReplaceSymbol, options);
    }
    br.close();
    return grammar;
  }

}
