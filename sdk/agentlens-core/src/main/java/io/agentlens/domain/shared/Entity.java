package io.agentlens.domain.shared;

import java.util.Objects;

/**
 * 领域实体的基类。
 * <p>
 * 实体是具有「身份」的对象，其身份在生命周期内保持不变。
 * 两个实体若具有相同身份则视为相等，与属性值无关（符合 DDD 实体相等性规则）。
 * </p>
 *
 * @param <ID> 实体标识符的类型
 */
public abstract class Entity<ID> {

    /** 实体唯一标识，不可变 */
    protected final ID id;

    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id, "Entity ID cannot be null");
    }

    /** 获取实体标识 */
    public ID getId() {
        return id;
    }

    /** 按身份判断相等（非按属性） */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
