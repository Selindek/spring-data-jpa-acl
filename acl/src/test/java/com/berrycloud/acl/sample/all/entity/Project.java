package com.berrycloud.acl.sample.all.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.berrycloud.acl.annotation.AclParent;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @AclParent
    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private List<TestGroup> groups = new ArrayList<>();

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

    public List<TestGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<TestGroup> groups) {
        this.groups = groups;
    }

}
