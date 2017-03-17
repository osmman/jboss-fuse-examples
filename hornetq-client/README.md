# HornetQ

This example shows how to install drivers and connect to HornetQ server from JBoss Fuse (Karaf distribution).  

- Run `mvn clean install` to build project.
- After the build has finished, you have to add features XML
`features:addurl mvn:com.github.osmman.fuse/hornetq-client/1.0-SNAPSHOT/xml/features`
- Set connection information for HornetQ instance:
```
config:edit com.github.osmman.fuse.hornetq
config:propset hornetq.host localhost
config:propset hornetq.port 5445
config:propset hornetq.username user
config:propset hornetq.password password
config:update
```
- Now you can install `hornetq-cf` feature. `features:install hornetq-cf`

If If you want to verify that everything is correctly configured then you can use these commands to test a connection:
```
jms:info -u user -p password hornetq/ConnectionFactory
jms:info -u user -p password hornetq/XAConnectionFactory
```
