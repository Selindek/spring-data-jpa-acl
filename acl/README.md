Spring JPA ACL
==============

This package is an extension for Spring Data JPA. It provides a fast an easily customizable access-control system for projects using Spring Repositories.

ACL usually means 'Access Control List', but in this case it stands for 'Access Control Logic'. Although it could still use lists for permission control most of the access decisions are made by predefined rules.

Let's see the main differences between traditional AC-List implementations and this package:

1. This package uses only standard JPA methods and queries, so in theory it's compatible with any Relational DB that has JPA support.
2. All the access control logics are defined in the domain objects via annotations, so you can see the data-structure and the access rules in the same place
3. The permission decisions are happened in the DB side, so there is much less traffic between your application and the DB, and you can even use paginated queries on permission-filtered entities.
4. It's compatible with Spring Data Jpa Repositories so the ACL is automatically applied for all standard repository methods.


Basics
------

The basic idea behind this ACL package is that most of the time when you want to grant access a user to a given object in the DB, there is already some kind of relation between them.
Let's see som example:

- A user has full control to his documents (probably there is an author field in the document table). 
- Users in the same work-group can see (but cannot modify) each-others. (There must be a workgroup table with a ManyToMany relationship with users.)
- Administrators have full access to all objects. (They must have a special role attached to them somehow. Either directly or they are members of an admin group)
- If a user is a moderator in a topic he can edit all messages sent to that topic (Must be a relation between messages and topics) and also the replies to these messages (There must be a field in the message table what stores the original message is the current one is a reply.)

So instead of creating a new permission entry (or a bunch of entries) every time we are creating a new object, we simply use the existing relationships for access control.
Sometimes it means that you have to add a few extra relations to your tables when you design your DB structure, but hey, that's why it's called 'relational DB'!

