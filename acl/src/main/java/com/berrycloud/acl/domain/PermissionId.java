package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PermissionId<O extends Serializable, T extends Serializable, P extends Serializable> implements java.io.Serializable {

  private static final long serialVersionUID = 339519507979487644L;

  private static final int HASH_BASE = 11;
  private static final int HASH_MULTIPLIER = 13;

  private O owner;
  private T target;
  private P permission;

  public PermissionId() {
  }

  public PermissionId(final O owner, final T target, final P permission) {
    this.owner = owner;
    this.target = target;
    this.permission = permission;
  }

  @Column(nullable = false)
  public O getOwner() {
    return owner;
  }

  public void setOwner(final O owner) {
    this.owner = owner;
  }

  @Column(nullable = false)
  public T getTarget() {
    return target;
  }

  public void setTarget(final T target) {
    this.target = target;
  }

  @Column(nullable = false)
  public P getPermission() {
    return permission;
  }

  public void setPermission(final P permission) {
    this.permission = permission;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!(other instanceof PermissionId)) {
      return false;
    }
    final PermissionId<?, ?, ?> castOther = (PermissionId<?, ?, ?>) other;
    return eq(getOwner(), castOther.getOwner()) && eq(getTarget(), castOther.getTarget()) && eq(getPermission(), castOther.getPermission());
  }

  private boolean eq(final Serializable a, Serializable b) {
    return a == b || a != null && a.equals(b);
  }

  @Override
  public int hashCode() {
    int result = HASH_BASE;

    result = HASH_MULTIPLIER * result + (getOwner() == null ? 0 : getOwner().hashCode());
    result = HASH_MULTIPLIER * result + (getTarget() == null ? 0 : getTarget().hashCode());
    result = HASH_MULTIPLIER * result + (getPermission() == null ? 0 : getPermission().hashCode());
    return result;
  }

}
