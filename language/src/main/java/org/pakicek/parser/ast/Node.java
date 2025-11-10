// org/pakicek/parser/ast/Node.java
package org.pakicek.parser.ast;

public interface Node {
    <T> T accept(Visitor<T> visitor);
}