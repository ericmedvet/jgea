/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class Node<T> implements Serializable, Cloneable, Sized {

  private final T content;
  private final List<Node<T>> children = new ArrayList<>();
  private Node<T> parent;

  public Node(T content) {
    this.content = content;
  }

  public Node(Node<T> original) {
    if (original == null) {
      this.content = null;
      return;
    }
    this.content = original.getContent();
    for (Node<T> child : original.getChildren()) {
      children.add(new Node<>(child));
    }
  }

  public T getContent() {
    return content;
  }

  public List<Node<T>> getChildren() {
    return children;
  }

  public List<Node<T>> leafNodes() {
    if (children.isEmpty()) {
      return Collections.singletonList(this);
    }
    List<Node<T>> childContents = new ArrayList<>();
    for (Node<T> child : children) {
      childContents.addAll(child.leafNodes());
    }
    return childContents;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(content);
    if (!children.isEmpty()) {
      sb.append("{");
      for (Node<T> child : children) {
        sb.append(child.toString()).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append("}");
    }
    return sb.toString();
  }

  public int height() {
    int max = 0;
    for (Node<T> child : children) {
      max = Math.max(max, child.height());
    }
    return max + 1;
  }

  @Override
  public int size() {
    int size = 0;
    for (Node<T> child : children) {
      size = size + child.size();
    }
    return size + 1;
  }

  public List<Node<T>> getAncestors() {
    if (parent == null) {
      return Collections.EMPTY_LIST;
    }
    List<Node<T>> ancestors = new ArrayList<>();
    ancestors.add(parent);
    ancestors.addAll(parent.getAncestors());
    return Collections.unmodifiableList(ancestors);
  }

  public Node<T> getParent() {
    return parent;
  }

  public void propagateParentship() {
    for (Node<T> child : children) {
      child.parent = this;
      child.propagateParentship();
    }
  }

  public int childIndex() {
    if (parent == null) {
      return -1;
    }
    for (int i = 0; i < parent.getChildren().size(); i++) {
      if (this == parent.getChildren().get(i)) {
        return i;
      }
    }
    return -1; //should not happen;
  }

  public List<Integer> ancestorIndexes() {
    List<Integer> indexes = new ArrayList<>();
    if (parent!=null) {
      indexes.add(childIndex());
      indexes.addAll(parent.ancestorIndexes());
    } else {
      return Collections.EMPTY_LIST;
    }
    return indexes;
  }

  public Node<T> prunedSubTree(int n) {
    Node<T> subtree = new Node<T>(content);
    if (n > 0) {
      for (Node<T> child : children) {
        subtree.getChildren().add(child.prunedSubTree(n-1));
      }
    }
    return subtree;
  }
  
  public void prettyPrint(PrintStream ps) {
    propagateParentship();
    ps.printf("%" + (1 + this.getAncestors().size() * 2) + "s-%s%n", "", this.getContent());
    for (Node<T> child : this.getChildren()) {
      child.prettyPrint(ps);
    }
  }  

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + Objects.hashCode(this.content);
    hash = 53 * hash + Objects.hashCode(this.children);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Node<?> other = (Node<?>) obj;
    if (!Objects.equals(this.content, other.content)) {
      return false;
    }
    if (!Objects.equals(this.children, other.children)) {
      return false;
    }
    return true;
  }

  @Override
  public Object clone() {
    return new Node<>(this);
  }

}
