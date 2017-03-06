

Spring data japa and Spring data rest was NOT designed for extending, so adding these extra features were a little bit 
difficult here or there.
We have the following hacks/ nasty workarounds:


ExportAwareRepositories
-----------------------

It extends a class where a private method should be overridden. 
This method Also uses private fields, so several more methods must be copy-pasted which use these variables

RepositoryAclPropertyReferenceController
----------------------------------------

We have to hide the original RepositoryPropertyReferenceController what is a auto-scanned controller. 
Almost all of its methods are modified, so the class itself cannot be constructed much more nicely.
However the original controller methods cannot be purged out, so we have two ambiguous methods defined for all
repository URL paths/methods. The hack-solution is the following:

I added an extra header requirement to the @RequestMapping annotation for the new methods and created a filter what
adds this exact header to all incoming requests. This way the the DispatcherServlet will delegate all requests to 
the new controller-methods, because they have a slightly better match with the request.

AclJpaQueryLookupStrategy
-------------------------

I had to extend the JpaQueryLookupStrategy class what is a final class. Moreover it uses some non-public classes from its
package, so I copy-pasted the whole class to the same package then modified the proper methods.

PartTreeAclJpaQuery
-------------------

Another public class where I had to modify some private methods/ inner classes which also uses private fields. It was more 
easy to copy-paste the whole class and make the required modifications in it.

AclPermissionEvaluator
----------------------

Creates its own map of EntityInformations for fast access. These objects are created elsewhere too but it's much convenient to 
access them directly locally. (This is the least nasty hack of all.)

SimpleAclJpaRepository
----------------------

Almost all of its methods must be overridden, so it would be more easy to simply create it as a new class. But the original 
SimpleJpaRepository.class is referenced several places, so I had to extend the original one.

AclQueryUtils
-------------

The original QueryUtils is a final static class with private constructor. All I had to do is modifying the input parameter
of two methods, but these methods also calls another private methods, so at the end it was easier to copy-paste the whole class.


DomainPropertyClassResolver
---------------------------

The original class (DomainClassResolver) has no public constructor, so I couldn't extend it. Also that class was referenced directly 
So I had to recreate the JacksonMappingAwarePropertySortTranslator class and its private methods, and it was also referenced 
directly so I had to recreate the MappingAwareDefaultedPropertyPageableArgumentResolver class too and finally I had to change the 
original one to the modified one in the RepositoryRestMvcConfiguration. (Several ArgumentResolvers are being creating together)
Extremely nasty hack, I had to recreate 3 classes because of 10 lines of code...
