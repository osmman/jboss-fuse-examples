Camel Router Project for Blueprint (OSGi)
=========================================

To build this project use

    mvn clean install

To deploy the project in OSGi. For example using Apache ServiceMix
or Apache Karaf. You can run the following command from its shell:

    feature:install jdbc jndi pax-jdbc-h2 jpa hibernate camel-jpa
    jdbc:ds-create -dn H2 -dbName fsw -url "jdbc:h2:mem:fsw;DB_CLOSE_DELAY=-1;MVCC=TRUE" jdbc/h2
    bundle:install -s mvn:com.github.osmman.karaf4/camel-jpa/1.0-SNAPSHOT

To trigger camel route you can use

    jdbc:execute jdbc/h2 "INSERT into books (id,name) values (1,'book1')"

For more help see the Apache Camel documentation

    http://camel.apache.org/
