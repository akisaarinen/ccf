Concurrency Control Framework (CCF)
===================================

What is CCF?
------------

CCF is a library to make implementing collaborative software easier. It
provides mechanisms for real-time synchronization of data structures between
clients over the Internet. CCF is implemented in the Scala programming language.

Authors and licensing
---------------------

Origins of CCF lie in the Master's thesis of Aki Saarinen [1].  Later on there
has been also many other contributors, names of which can be found in the
version history. Most notable include Karim Osman and Harri Salokorpi.

CCF is licensed under the Apache License version 2.0.

[1] "Concurrency Control in Group Editors: Case Study in a Product Backlog Tool"
    Aki Saarinen, 2009, Helsinki University of Technology
    Available from http://www.akisaarinen.fi

Setting Up Development Environment
----------------------------------

1. Clone the git repository
2. Configure JVM
3. Fetch all depencencies

  $ ./sbt update

4. Run all tests:

  $ ./sbt test

Creating Library Package
------------------------

 $ ./sbt package

This will produce the package to:

  ./ccf/target/ccf-{version}.jar

Running the Test Application
----------------------------

The test application is a very simple collaborative text-editing application,
consisting of a server that binds to a port and a client that connects to the
server using HTTP. You may start multiple instances of the client application
locally and try out how the text is updated to the other instances in real
time. Best example of the operation is of course if you can run the client in
two machines and just specify the same server address for both clients.

Running the server application:

  $ ./sbt -p app run -s    (-s can be replaced with --server)

Optional parameters for server:

  --port <port> || -p <port>

Port defaults to 9999.

Running the client application:

 $ ./sbt -p app run       (no parameters == client)

Optional parameters for client:

  --host <host> || -h <host>
  --port <port> || -p <port>

Port defaults to 9999 and host to 'localhost'.
