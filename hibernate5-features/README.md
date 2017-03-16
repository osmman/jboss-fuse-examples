# Hibernate 5 on JBoss Fuse

This example shows how to build feature support Hibernate 5.x on JBoss Fuse.  

- Run `mvn clean install` to build feature.xml
- After the build has finished, you had to add features XML
`features:addurl mvn:com.github.osmman.fuse.features/hibernate5/1.0-SNAPSHOT/xml/features`
- Now you can install Hibernate 5 feature
`features:install hibernate/5.0.11.Final-redhat-1`

