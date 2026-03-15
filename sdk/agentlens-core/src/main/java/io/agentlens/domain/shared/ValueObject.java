package io.agentlens.domain.shared;

/**
 * 值对象的基类。
 * <p>
 * 值对象无身份，仅由属性定义；不可变；两个值对象若所有属性相等则相等（结构性相等）。
 * </p>
 */
public abstract class ValueObject {

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
