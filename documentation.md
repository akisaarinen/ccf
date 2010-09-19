---
title: Documentation
layout: default
---

This page contains documentation for the CCF library.

<ul>
    <li><a href="#messaging_in_ccf">Messaging in CCF</a></li>
    <li><a href="#performance_analysis">Performance analysis</a></li>
    <li><a href="#introductiontosynchronization">Synchronization in real-time collaborative editing/a></li>
</ul>

Messaging in CCF
----------------

In addition to concurrency control, CCF provides interfaces for exchanging
information with a remote server in a robust way. These interfaces can be
categorized into three layers: transport, session, and application layer.

### Transport Layer

The transport layer specifies a connection interface for accessing remote
services by means of a message exchange pattern known as request-response. The
connection interface specifies a method, #send, that sends a request and blocks
until a response has been received or an error occurs. The connection interface
unifies possible error conditions, such as disconnections and timeouts, by
throwing an exception of type ConnectionException upon detection of an error.

The transport layer is responsible for performing serialization and
deserialization of requests and responses. However, in order to meet the
requirements of the session and application layer, as little as possible is
assumed of the structure of requests and responses while bearing in mind that
the actual wire format is specified by the transport layer. Currently, it seems
that a hierarchical data structure of strings serves this purpose well.

Protocols that rely on request-response message exchange patterns inherently
assume that no request can be carried out over the same communication path
until a response for the current response has been received. This obviously has
an impact on the maximum number of requests per second. Although, this comes
with a price, it complies with the requirement of delivering all messages over
HTTP. This requirement is set forth by network related restrictions of various
production environments. Also, HTTP/1.1 connections are by default persistent,
and thus, the only overhead of using HTTP in comparison to some other TCP/IP
request-response protocol is caused by the HTTP request headers. Surely, the
request-response is inferior to other messaging protocols that allow multiple
requests before a single response is received, however, benchmarking HTTP/1.1
shows that a protocol relying on request-response message exchange pattern is
viable for concurrency control among multiple clients (see the results below).

### Session Layer

As the transport layer cannot guarantee successful requests, the session layer
introduces semantics for recovering from errors or inconsistent states between
the client and server. This in turn allows the application layer rely on the
session layer for robust delivery of requests and notification of
disconnections and reconnections.

The session layer introduces the concept of channels in which operations
subject to operation transformation are exchanged. If the application layer
wishes to access a remote service specific to the application, it is required
to carry out this request within a channel. Only the session layer is allowed
to perform out-of-channel requests on a remote service, for example, heartbeats
and joinin and parting to and from a channel. Currently, a single session
instance maps to a single channel, but this limitation is not by design, only
an implementation detail.

For ensuring robust client-server communication, the session layer specifies a
set of fields that must be present in each request: unique client id, version
number, and channel id. The unique client id is used for identifying a single
client, while a single client may maintain one or more sessions. The version
number is introduced for ensuring that a request can be assumed to be valid. If
the server-side detects that the version number does not match the expected
one, the request is rejected and the error condition is signalled in the
response. Also, the session layer enforces that all communication is sequential
within the request-response context, that is, both the client and server must
process messages in order, and inconsistencies in the sequence result into
execution of recovery procedures.

The requests over the session layer may or may not be authenticated, and is
specific to the application. For example, the remote service may require each
request to include credidentials, such as a session key obtained by
out-of-session communication.

### Application Layer

The application layer relies on the session layer for performing requests and
for receiving notifications about the current state of the communication path.

If the session layer detects that the remote service may be unavailable, the
application layer is notified about it. Although, the application is allowed to
queue requests, it must prepare for the event that the remote service will not
become available.


Performance analysis
--------------------

### Request-Response Latencies over HTTP/1.1

On average a request-response took 18.8 ms with a standard deviation of 19.2
ms. The maximum response-request took 184 ms and the minimum 1 ms. The results
were obtained by performing 10,000 requests from a client to the server running
in their own processes. Both the request and response had a content length of
1024 bytes. The client was written in Scala using HttpClient version 4.0.1 of
Jakarta Commons, whereas the server used Sun's Java HTTP server. The client and
server ran on the same computer (Ubuntu 9.04, Intel Core 2 Duo 2.4 GHz),
however, the loopback device was not used.


<a id="introductiontosynchronization"></a>
Synchronization in real-time collaborative editing
----------------------------------------------------------------

<p class="author">Aki Saarinen, 5.9.2010, updated 19.9.2010.</p>

<b>Disclaimer:</b> This article is intended for those interested in the inner
workings of real-time collaborative software, and is a good read for those who
want to get familiar with how CCF and similar libraries work. 

Synchronization between multiple parties in a collaborative
group-editing system is an interesting problem that can be described
for example as "the activity of coordinating the potentially
interfering actions of processes that operate in parallel" \[2\]. Popular
approaches for solving this problem include various locking schemes,
global serialization of actions by a single party (usually the server)
and finally what's called operational transformations (OT).

CCF is based on one of these OT algorithms. The main difference between OT and
other traditional approacehes is that OT algorithms allow users to make instant
changes to a local copy of the data, and then provide mechanisms for eventually
synchronizing all of these clients in such a way, that intentions of all users
are preserved whenever it's possible.  Conflict situations are usually merged
automatically, even though user interaction could be implemented when
necessary. This allows us to implement software with very short response times
to local changes, while still allowing real-time collaboration with any number
of users.

### CCF and Jupiter

CCF is based on an algorithm called Jupiter. It was introduced by
Nichols et. al in the context of the Jupiter collaboration system
already in 1995 \[1\]. A variant of this same algorithm was used in the
<a href="http://wave.google.com">Google Wave</a> collaboration
platform. Even though various more advanced algorithms have been
developed since the introduction of Jupiter, the relative simplicity of
this algorithm still makes it a powerful tool even today.

### Limitations in the Jupiter algorithm and CCF

CCF is suitable for synchronizing tree-like documents between multiple clients
using one central server. There are algorithms that work in a pure peer-to-peer
setting, but CCF won't. This restriction simplifies both the problem of
synchronization both from a theoretical and practical viewpoints. It's a
tradeoff that we wanted to make in order to reduce the complexity in CCF and
programs that use it.

### How it works? 

Let's use a simple example to take a look at how CCF works. Consider an
application that allows users to edit non-formatted basic text
collaboratively. Each user has a client running, which is then
connected to a central server over the Internet. The data structure of
this application can be represented as a flat list or characters, which
can also be considered as an ordered tree which has a fixed depth of 1.
Each action the user makes, is represented as an operation. For this
simple example, we have only two operations are available: insertion
and deletion. 

When a user writes a new character somewhere in the text, this will be
encoded as an insert operation. This insert contains the character that
is inserted, and the position where the user wanted to insert it, e.g.
<i>insert(4, 'a')</i>. Insert is applied to the local copy of the
user's document right away. Now, this operation along with information
of what was the document state when this operation was created (the
state is important, as we'll see), will be sent to the server. Upon
receiving this message, the server will decode it back to an operation
and state information. If nothing has happened concurrently, the server
will then apply this operation to its local copy of the document, and
then echo this same message to all other clients. Upon receiving,
clients also apply the operation and at this time, all clients are in a
synchronized state.

Now, let's consider a case where two users make a modification
simultaneously. First part is still similar, both operations get
encoded as an operation and will be applied to the local copies of the
users, after which operations along with their state information is
sent to the server. Now the magic starts to happen. One of these
messages will reach the server first, which means that it gets
processed exactly the same way as in our first example, because from
the server's point of view, nothing has happened concurrently (second
request has not yet reached the server). However, when the second
user's modification messagei reaches the server, it will notice that
the state at the time of creation of that operation is different from
server's current state. That's because of the first operation applied
by the first user. 

Now the server needs to <i>transform</i> the incoming operation in such
a way, that the <i>intentions of the user are preserved</i>. Let's say
the first operation was: <i>insert(4, 'a')</i> and the second operation
was <i>delete(8)</i>. After the insert has been applied, the given
index in the delete operation (8), is no longer valid. The insert to
position 4 has transformed all text behind it by one. Delete will be
transformed to <i>delete(9)</i> with help of a transformation function
and the state information. This transformed operation will now be
echoed to other clients.

Our second example considered a case where operations had happened in
the server before another operation reached the server. In this case,
the server needs to <i>transform</i> the incoming operation to preserve
its original intentions. This same thing can also happen in the client
side. Let's extend our previous example that the user whose operation
reached the server first, also inserted another character with
<i>insert(1, 'b')</i>, but that this operation didn't reach the server
before another client's delete. Now before echoed to this user, the
delete was transformed to <i>delete(9)</i> in the server. However, when
reaching the client, an <i>insert(1, 'b')</i> has been applied to the
client's local copy of the document. Incoming operation needs once
again to be transformed. We can detect this need from the state
information, and incoming delete will then be shifted once more to
<i>delete(10)</i>.

This simple scheme of transforming all incoming operations both in the
client and the server allows us to always apply any operation locally
right away, and upon receiving other clients' modifications, just
transform then appropriately.

### References

\[1\] Nichols, D. A., Curtis, P., Dixon, M., and Lamping, J. High-
latency, low-bandwidth windowing in the jupiter collaboration system.
In UIST '95: Proceedings of the 8th annual ACM symposium on User
interface and software technology (New York, NY, USA, 1995), ACM.

\[2\] Greenberg, S., and Marwood, D. Real time groupware as a dis-
tributed system: concurrency control and its effect on the interface. In
CSCW '94: Proceedings of the 1994 ACM conference on Computer supported
cooperative work (New York, NY, USA, 1994), ACM.
