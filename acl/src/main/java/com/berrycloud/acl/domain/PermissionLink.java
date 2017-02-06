package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Embeddable
public abstract class PermissionLink<O extends AclOwner<OID>, OID extends Serializable, T extends AclEntity<TID>, TID extends Serializable, P extends AclPermission<PID>, PID extends Serializable> {

  private PermissionId<OID, TID, PID> id;
  private O owner;
  private T target;
  private P permission;

  public PermissionLink() {
  }

  public PermissionLink(O owner, T target, P permission) {
    this.owner = owner;
    this.target = target;
    this.permission = permission;
    this.id = new PermissionId<OID,TID,PID>(owner.getId(), target.getId(), permission.getId());
  }

  @EmbeddedId
  @AttributeOverrides({ @AttributeOverride(name = "owner", column = @Column(name = "owner", nullable = false)),
      @AttributeOverride(name = "target", column = @Column(name = "target", nullable = false)),
      @AttributeOverride(name = "permission", column = @Column(name = "permission", nullable = false)) })
  public PermissionId<OID, TID, PID> getId() {
    return id;
  }

  public void setId(final PermissionId<OID, TID, PID> id) {
    this.id = id;
  }

  @ManyToOne()
  @JoinColumn( name = "owner", nullable = false, insertable = false, updatable = false)
  public O getOwner() {
    return owner;
  }

  public void setOwner(final O owner) {
    this.owner = owner;
  }

  @ManyToOne()
  @JoinColumn( name = "target", nullable = false, insertable = false, updatable = false)
  public T getTarget() {
    return target;
  }

  @Transient
  public String getFaszom() {
    return "WWW";
  }

  public void setTarget(final T target) {
    this.target = target;
  }

  @ManyToOne()
  @JoinColumn(name = "permission", nullable = false, insertable = false, updatable = false)
  public P getPermission() {
    return permission;
  }

  public void setPermission(final P permission) {
    this.permission = permission;
  }

}
