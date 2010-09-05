---
title: Submitting Patches
layout: default
---

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


Generic guidelines
------------------

When submitting patches to the CCF project, please take a look at these
guidelines first. There are several good articles available about making good
commits and patch series, which are a good read to anyone. Please take a look
at these articles:

* [The perfect patch](http://userweb.kernel.org/~akpm/stuff/tpp.txt) by Andrew Morton
* [Submitting Patches to Git](http://repo.or.cz/w/git.git?a=blob_plain;f=Documentation/SubmittingPatches;hb=HEAD)

As a summary, make sure you do at least the following:

* One patch series per topic. Don't combine totally unrelated things to the same series.
* Make small, atomic commits in the series. This keeps reviewing easier and makes 
  it possible to grasp what's going on.
* Add "Signed-off-by" -line to the end of the commit. <code>git commit
  -s</code> is the easy way. This is how we can identify your commits after one
  of the maintainers has merged it to the master repository.

Patch naming guidelines
-----------------------

If the commit is related to non-core parts of the ccf, please prefix them appropriately with one of the following:
* app - Test application - [example commit](http://github.com/akisaarinen/ccf/commit/d71849762848748b71fd239e6ef2658b24adf95a)
* build - Project-file related changes - [example commit](http://github.com/akisaarinen/ccf/commit/d71849762848748b71fd239e6ef2658b24adf95a)
* perftest - Performance test application - [example commit](http://github.com/akisaarinen/ccf/commit/4f979c19e587ccca411eba8f16f11c2d9691d167)
* scripts - Helper scripts - [example commit](http://github.com/akisaarinen/ccf/commit/38d4dd5081efc77d4d5a59815707f75296f81502)

If the commit touches the core parts of CCF, please try to make modifications to one layer at a time, when possible. This makes reviewing the patches easier and also keeps the commit history cleaner. The following subsystems are commonly modified separately:
 
* operation - Modifications to operation subsystem - [example commit](http://github.com/akisaarinen/ccf/commit/c9c89ea5bc41db74bd0d685c4ab41c743e7de455)
* session - Session-layer related changes - [example commit](http://github.com/akisaarinen/ccf/commit/38d4dd5081efc77d4d5a59815707f75296f81502)
* transport - Transport-layer related changes - [example commit](http://github.com/akisaarinen/ccf/commit/38d4dd5081efc77d4d5a59815707f75296f81502)
* tree - Modification to the tree-changing operations - [example commit](http://github.com/akisaarinen/ccf/commit/c9c89ea5bc41db74bd0d685c4ab41c743e7de455)

These are not hard-and-fast rules, but something to think about when making the commits.

