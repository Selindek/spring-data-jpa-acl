package com.berrycloud.acl.sample.all.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.domain.SimpleAclRole;

@Entity
public class TestGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @AclOwner
    private Person createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @AclOwner
    private Person supervisor;

    @ManyToOne
    private SimpleAclRole role;
    
    @AclOwner("read")
    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    private List<Person> members = new ArrayList<>();

    public TestGroup() {
    }

    public TestGroup(String name, Person createdBy) {
        super();
        this.name = name;
        this.createdBy = createdBy;
    }

    // @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
    }

    public Person getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Person supervisor) {
        this.supervisor = supervisor;
    }

    public List<Person> getMembers() {
        return members;
    }

    public void setMembers(List<Person> members) {
        this.members = members;
    }
    
    public SimpleAclRole getRole() {
        return role;
    }

    public void setRole(SimpleAclRole role) {
        this.role = role;
    }

}
