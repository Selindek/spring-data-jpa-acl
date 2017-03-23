package com.berrycloud.acl.sample.all.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclRoleProvider;
import com.berrycloud.acl.annotation.AclSelf;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.domain.SimpleAclRole;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@AclSelf({ "read" })
// @NoAcl
public class Person implements AclUser<SimpleAclRole> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;
    @JsonIgnore
    private String password = "password";

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<SimpleAclRole> aclRoles = new HashSet<>();

    @AclOwner // generates warning
    private String firstName;
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @AclOwner()
    private Person createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person controlled;

    @OneToMany(mappedBy = "controlled", fetch = FetchType.LAZY)
    @AclOwner
    private List<Person> supervisors = new ArrayList<>();

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    @AclOwner
    @ManyToMany(fetch = FetchType.LAZY)
    @AclRoleProvider
    private List<TestGroup> groups = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private List<TestGroup> controlGroups = new ArrayList<>();

    //@AclOwner
    @ManyToOne(fetch = FetchType.LAZY)
    private TestGroup controlGroup;

    @JsonIgnore
    @OneToMany(mappedBy = "target", fetch = FetchType.LAZY)
    private List<PersonHasPersonPermission> personOwner;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<PersonHasPersonPermission> personTarget;

    public Person() {
    }

    public Person(String username, String firstName, String lastName) {
        this(username, firstName, lastName, null);
    }

    public Person(String username, String firstName, String lastName, Person createdBy) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdBy = createdBy;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // @AclOwner
    public Person getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
    }

    // public Person getSupervisor() {
    // return supervisor;
    // }
    //
    // public void setSupervisor(Person supervisor) {
    // this.supervisor = supervisor;
    // }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<PersonHasPersonPermission> getPersonOwner() {
        return personOwner;
    }

    public void setPersonOwner(List<PersonHasPersonPermission> personOwner) {
        this.personOwner = personOwner;
    }

    public List<PersonHasPersonPermission> getPersonTarget() {
        return personTarget;
    }

    public void setPersonTarget(List<PersonHasPersonPermission> personTarget) {
        this.personTarget = personTarget;
    }

    public List<TestGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<TestGroup> groups) {
        this.groups = groups;
    }

    public Person getControlled() {
        return controlled;
    }

    public void setControlled(Person controlled) {
        this.controlled = controlled;
    }

    public List<Person> getSupervisors() {
        return supervisors;
    }

    public void setSupervisors(List<Person> supervisors) {
        this.supervisors = supervisors;
    }

    // @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Set<SimpleAclRole> getAclRoles() {
        return aclRoles;
    }

    public void setAclRoles(Set<SimpleAclRole> aclRoles) {
        this.aclRoles = aclRoles;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<TestGroup> getControlGroups() {
        return controlGroups;
    }

    public void setControlGroups(List<TestGroup> controlGroups) {
        this.controlGroups = controlGroups;
    }

    public TestGroup getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(TestGroup controlGroup) {
        this.controlGroup = controlGroup;
    }

}
