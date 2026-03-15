package io.agentlens.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根的基类。
 * <p>
 * 聚合根是聚合的入口，聚合是一组实体与值对象组成的一致性边界。
 * 对聚合内数据的修改只能通过聚合根进行；可记录领域事件用于跨聚合通信。
 * </p>
 *
 * @param <ID> 聚合根标识符类型
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    /** 本聚合内产生的领域事件（未发布） */
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    /** 乐观锁版本号 */
    private long version = 0;

    protected AggregateRoot(ID id) {
        super(id);
    }

    /** 添加领域事件（发布前暂存） */
    protected void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /** 获取未发布的领域事件（不可修改） */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /** 清空已发布的领域事件 */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public long getVersion() {
        return version;
    }

    protected void incrementVersion() {
        this.version++;
    }
}
