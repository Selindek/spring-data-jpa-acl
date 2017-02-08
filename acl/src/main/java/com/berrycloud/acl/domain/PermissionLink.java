package com.berrycloud.acl.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class PermissionLink<O extends AclOwner<?>, T extends AclEntity<?>, P extends AclPermission<?>> {

  private Integer id;
  private O owner;
  private T target;
  private P permission;

  public PermissionLink() {
  }

  public PermissionLink(Integer id) {
    this.id = id;
  }
  
  public PermissionLink(O owner, T target, P permission) {
    this.owner = owner;
    this.target = target;
    this.permission = permission;
  }

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @ManyToOne()
  @JoinColumn( nullable = false, updatable = false)
  public O getOwner() {
    return owner;
  }

  public void setOwner(final O owner) {
    this.owner = owner;
  }

  @ManyToOne()
  @JoinColumn( nullable = false, updatable = false)
  public T getTarget() {
    return target;
  }

  public void setTarget(final T target) {
    this.target = target;
  }

  @ManyToOne()
  @JoinColumn(nullable = false, updatable = false)
  public P getPermission() {
    return permission;
  }

  public void setPermission(final P permission) {
    this.permission = permission;
  }

}
