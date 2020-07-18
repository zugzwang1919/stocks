package com.wolfesoftware.stocks.model;

import com.wolfesoftware.stocks.exception.IllegalActionException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public abstract class PersistentEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = "createdate")
    protected LocalDateTime createDate;

    @Column(name = "updatedate")
    protected LocalDateTime updateDate;


    /**********************************************/

    @PrePersist
    public void prePersist()
    {
        if (id != null || createDate != null || updateDate != null)
            throw new IllegalActionException("Create attempted on an entity that has already been created.");

        LocalDateTime now = LocalDateTime.now();
        createDate = now;
        updateDate = now;
    }

    @PreUpdate
    // NOTE:  The individual repository layers must guarantee that the UPDATE is valid - notably, the user
    // NOTE:  has not changed - nor has the create date.

    public void preUpdate()
    {
        if (id == null || createDate == null || updateDate == null)
            throw new IllegalActionException("Update attempted on an entity that was never created.");

        updateDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
    public LocalDateTime getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentEntity)) return false;
        PersistentEntity that = (PersistentEntity) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
