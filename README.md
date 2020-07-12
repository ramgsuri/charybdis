# Charybdis

[![Build Status](https://travis-ci.org/omarkad2/charybdis.svg?branch=master)](https://travis-ci.org/omarkad2/charybdis)
[![codecov](https://codecov.io/gh/omarkad2/charybdis/branch/master/graph/badge.svg)](https://codecov.io/gh/omarkad2/charybdis)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ma.markware.charybdis/charybdis-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ma.markware.charybdis/charybdis-core)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/omarkad2/charybdis/issues)
[![License](https://img.shields.io/github/license/fridujo/spring-automocker.svg)](https://opensource.org/licenses/Apache-2.0)

*Charybdis* is an Object/Relational Mapping (ORM) framework for Cassandra database.
 
It provides an abstraction over the Datastax driver, and adds a set of tools in order 
to offer a seamless Model-Transformation between *POJOs* and database entities 
while ensuring optimal performance.

Charybdis uses Java annotation processing (APT) to generate the needed metadata for mapping and querying 
the database.

In this regard, Charybdis, unlike ORM libraries working at runtime, offers the following advantages:
- **High performance**: Use plain java methods to serialize and deserialize database entities at 
runtime, since most work is done at compile-time. 
- **Model validation**: Get error reports at build-time when models are incomplete 
or incorrect.
- **Seamless model-transformation**: Complex java data structures can be transformed seamlessly to 
Cassandra data types.

## Installation
### Maven
Add the following dependency to your **pom.xml**

```xml
<dependency>
    <groupId>ma.markware.charybdis</groupId>
    <artifactId>charybdis-core</artifactId>
    <version>1.0.2</version>
</dependency>
```

<!--### Gradle-->
<!--Add the following dependency to your **build.gradle**-->
<!--```groovy-->
<!--repositories {-->
<!--    mavenCentral()-->
<!--}-->

<!--dependencies {-->
<!--    testCompile('com.github.charybdis:charybdis-core:1.0.0')-->
<!--}-->
<!--```-->

## Compatibility
Charybdis is compatible with Apache Cassandra 2.1 and higher.
 
It requires Java 8 or higher.

## Usage
Let's design a real-world example that has the following:
- Keyspace **keyspace_demo**
- Table **user**
- User-defined type (UDT) **address** (column in user).
- User-defined type (UDT) **country** (field in address).

### Modeling
#### Keyspace modeling
Keyspace **keyspace_demo**:
```java
@Keyspace(name = "keyspace_demo")
public class KeyspaceDemo {}
```
After compile, this generates class `KeyspaceDemo_Keyspace` with the needed metadata.

#### Udt modeling
Udt **address**:
```java
@Udt(keyspace = "keyspace_demo", name = "address")
public class Address {

    @UdtField
    private Integer number; // Avoid using primitive types in model class!
    
    @UdtField
    private String street;
    
    @UdtField
    private String city;
    
    @UdtField
    private @Frozen Country country;
    
    // Public no-arg constructor, getter and setters ...
}
```
After compile, this generates class `Address_Udt` with the needed metadata.

Udt **country**:
```java
@Udt(keyspace = "keyspace_demo", name = "country")
public class Country {

    @UdtField(name = "country_name")
    private String countryName;
    
    @UdtField(name= "country_code")
    private String countryCode;
    
    // Public no-arg constructor, getter and setters ...
}
```
After compile, this generates class `Country_Udt` with the needed metadata.

#### Table modeling
Table **user**:

```java
@Table(keyspace = "keyspace_demo", name = "user")
public class User extends AbstractUser {

    @Column
    @PartitionKey
    @GeneratedValue // Generates id automatically when using Crud API
    private UUID id;

    @Column(name = "joining_date")
    @ClusteringKey(index = 0, order = ClusteringOrder.DESC)
    private Instant joiningDate;

    @Column
    private List<@Frozen Address> addresses;

    @Column
    @Index(name = "access_role") // Generates a secondary index on this column
    private RoleEnum role;

    @Column(name = "access_logs")
    private Map<Instant, String> accessLogs;
  
    // Public no-arg constructor, getter and setters ...
}
```
After compile, this generates class `User_Udt` with the needed metadata.

### Code Generation
After project is built, some classes are generated by Charybdis APT. 
These classes are implementations of Charybdis' metadata APIs.
Additionally, we generate DDL scripts, loaded in classpath, to help you create or reset your database.

The following Cql files are generated for the modelling above:
- ddl_create.cql
    ```cql
    CREATE KEYSPACE IF NOT EXISTS keyspace_demo WITH REPLICATION={'class' : 'SimpleStrategy', 'replication_factor' : 1};
    CREATE TYPE IF NOT EXISTS keyspace_demo.address(number int,street text,city text,country frozen<country>);
    CREATE TYPE IF NOT EXISTS keyspace_demo.country(country_name text,country_code text);
    CREATE TABLE IF NOT EXISTS keyspace_demo.user(id uuid,joining_date timestamp,addresses list<frozen<address>>,role text,access_logs map<timestamp,text>,creation_date timestamp,last_updated_date timestamp,PRIMARY KEY(id, joining_date))WITH CLUSTERING ORDER BY(joining_date DESC);
    CREATE INDEX IF NOT EXISTS user_role_idx ON keyspace_demo.user(role);
    ```
- ddl_drop.cql
    ```cql
    DROP KEYSPACE IF EXISTS keyspace_demo;
    DROP TYPE IF EXISTS keyspace_demo.country;
    DROP TYPE IF EXISTS keyspace_demo.address;
    DROP TABLE IF EXISTS keyspace_demo.user;
    DROP INDEX IF EXISTS keyspace_demo.user_role_idx;
    ```

### Querying
In order to query our Cassandra database, we can either use a **Dsl API** or **Crud API**. 
Both can be instantiated by providing an implementation of [SessionFactory](https://github.com/omarkad2/charybdis/blob/master/core/src/main/java/ma/markware/charybdis/session/SessionFactory.java) if 
none provided we fallback on [DefaultSessionFactory](https://github.com/omarkad2/charybdis/blob/master/core/src/main/java/ma/markware/charybdis/session/DefaultSessionFactory.java).
#### Dsl API

- Instantiate the API:
    ```java
    DslQuery dsl = new DefaultDslQuery();
    ```

- Insert:
    ```java
    List<Adress> addresses = List.of(...);
    boolean applied = dsl.insertInto(User_Table.user, User_Table.id, User_Table.joiningDate, User_Table.addresses)
                       .values(UUID.randomUUID(), Instant.now(), addresses)
                       .ifNotExists()
                       .execute();
    ```

- Update:
    ```java
    boolean applied = dsl.update(User_Table.user)
                       .set(User_Table.addresses.entry(0), new Address(...)) // Updates address at index 0.
                       .set(User_Table.role, RoleEnum.ADMIN)
                       .set(User_Table.accessLogs, User_Table.accessLogs.append(Map.of(Instant.now(), "Ubuntu"))) // Adds entry to column 'access_logs'
                       .execute();
    ```

- Select: 
    ```java
    User user = dsl.selectFrom(User_Table.user)
                       .where(User_Table.joiningDate.lt(Instant.parse("2020-01-01T00:00:00Z")))
                       .allowFiltering()
                       .fetchOne();
    ```
- Delete:
    ```java
    boolean applied = dsl.delete()
                       .from(User_Table.user)
                       .where(User_Table.id.eq(UUID.fromString("c9b593c0-f5cb-4e88-bd55-88dee10a4e97")))
                       .execute();
    ```
#### Crud API
- Instantiate the API:
    ```java
    EntityManager entityManager = new DefaultEntityManager();
    ```

- Insert:
    ```java
    User persistedUser = entityManager.create(User_Table.user, new User(...));
    ```

- Update:
    ```java
    persistedUser.setJoiningDate(Instant.now());
    persistedUser = entityManager.update(User_Table.user, persistedUser);
    ```

- Select: 
    ```java
    Optional<User> adminUser = entityManager.findOptional(User_Table.user, User_Table.id.eq(userId)
                                                                        .and(User_Table.joiningDate.lt(Instant.now()))
                                                                        .and(User_Table.role.eq(RoleEnum.ADMIN)))
    ```
- Delete:
    ```java
    boolean deleted = entityManager.delete(User_Table.user, persistedUser);
    ```

## Licensing
Charybdis is licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this project except in compliance with the License. 
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
