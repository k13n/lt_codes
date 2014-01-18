LT Codes
====================

Erasure codes are a class of codes that allow information to be transferred
over lossy channels where missing information can be inferred from additional
data packets. This project implements one class of erasure codes known as LT
Codes that were introduced by Michael Luby. For more information have a look
at the corresponding [Wikipedia page](http://en.wikipedia.org/wiki/LT_codes).

The LT codes belong also to the class of **Fountain Codes**, which describe
erasure codes that are rateless in the sense that possibly infinitely many
packets can be generated from a source file. The name Fountain Codes stems
from the concept of a real water fountain that never stops spitting water.


Examples
--------------------

We built two sample applications, one encodes and decodes a single file
locally, whereas the other uses a UDP broadcast to send a file across
a network.


### Local Example

In this example the application reads a file, splits it up in a couple
of packets, encodes them, sends them to the client which then tries to
decode them. The server sends a predefined amount of packets to the client
and depending on the parameterization the client may or may not be able
to decode the original file.  To make this example a bit more realistic
we set up a virtual erasure channel whose loss rate can be defined as
parameter.

The main class that is used to execute the application is
**lt_codes.local.InMemoryFileTransfer** and it takes mainly three
parameters:

* **filename**: the name of the file that should be processed
* **erasure_probability**: the probability that a packet is lost, e.g. 0.1
  means that approximately 10% of the packets are lost during transmission
* **packet_overhead**: the number of packets to transmit, 1.4 means 40%
  more packets than the file actually has

With the following command the application can be executed:

```shell
java \
  -cp lib/*:lt_codes-1.0-SNAPSHOT.jar \
  lt_codes.local.InMemoryFileTransfer \
  --verbose --erasure_probability 0.1 --packet_overhead 1.40 <FILENAME>
```


### UDP Broadcast Example

Whereas the previous application transferred a file only within the program,
this application transfers a file from one sender to many receivers on the
same local subnet. For this to work the application uses the IP protocol's
broadcast feature, the sender transmits a packet to a designated broadcast
IP address and the local router forwards it automatically to all hosts. All
the receiver has to do is open a socket on a predefined port and wait for
the broadcasted packets to arrive.

For a list of designated IP broadcast address have a look on this page:
http://en.wikipedia.org/wiki/Multicast_address

As already mentioned this applications consists of two actors, the sender and
the receiver. Both of them take the same set of parameters:

* **filename**: the name of the file that should be processed
* **address**: the designated IP broadcast address
* **port**: the port used to open a socket

The sender is implemented as a *fountain*, meaning that it never stops
generating and transmitting new packets unless it is manually killed by
the user.  To start the sender on port 4445 and broadcasting to the IP
address 224.0.0.1 use the following command:

```shell
java \
  -cp lib/*:lt_codes-1.0-SNAPSHOT.jar \
  lt_codes.broadcast.BroadcastSender \
  --verbose --address 224.0.0.1 --port 4445 <FILENAME>
```

Similarly, the receiver can be started with the same set of parameters as
follows. Note that the receiver needs also to know the filename, because
this information is not transmitted in the header of the packets. If
you start the sender and receiver in the same directory use a different
(possibly non-existent) filename, otherwise the file is locked by the sender.

```shell
java \
  -cp lib/*:lt_codes-1.0-SNAPSHOT.jar \
  lt_codes.broadcast.BroadcastReceiver \
  --verbose --address 224.0.0.1 --port 4445 <FILENAME>
```
