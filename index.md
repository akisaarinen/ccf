---
title: Concurrency Control Framework for Scala
layout: default
---

<a id="whatisccf"></a>
What is CCF?
------------

CCF is a Scala library aiming to make developing of collaborative
software easier. CCF uses operational transformation (OT) for achieving
concurrency control in systems in which collaborative editing takes
place. In addition to operational transformation, CCF offers higher
level APIs for synchronization of tree-like documents between multiple
clients over HTTP/1.1 protocol. CCF is distributed under the 
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.

<a id="download"></a>
Download
--------
The latest version of CCF is 0.2.0 and it was released on the 3rd of
September, 2010. You can download the 
[sources](http://github.com/akisaarinen/ccf/tarball/v0.2.0) or
clone the Git [repository](http://github.com/akisaarinen/ccf) by running:

    $ git clone http://github.com/akisaarinen/ccf.git

<a id="contributing"></a>
Contributing
------------

If you'd like to contribute to the CCF project, please feel free to do so!
We're currently working with a maintainer workflow, where two maintainers are
responsible for merging any changes to the master branch. 

Our current maintainers are:

* Aki Saarinen ([akisaarinen](http://github.com/akisaarinen/))
* Karim Osman ([kro](http://github.com/kro/))

To contribute, please do the following:

1. [Fork](http://help.github.com/forking/) your own clone of [CCF repository](http://github.com/akisaarinen/ccf)
2. Take a look at our [guidelines](submitting-patches.html).
3. Make and commit your changes to your own forked repository. 
4. Submit patches using git-generate-patches and git-send-email to our [Google Group](http://groups.google.com/group/scala-ccf) (scala-ccf@googlegroups.com) for peer review.
5. When acked in the discussion group, make a [pull request in github](http://help.github.com/pull-requests/).
6. Enjoy seeing your work in CCF!

