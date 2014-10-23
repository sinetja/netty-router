Publish to local
----------------

While developing, you may need do local publish. Run
``sbt publish-local``.

To delete the local publish:

::

  $ find ~/.ivy2 -name *netty-router* -delete

Publish to Sonatype
-------------------

See:
https://github.com/sbt/sbt.github.com/blob/gen-master/src/jekyll/using_sonatype.md

Create ~/.sbt/0.13/sonatype.sbt (for SBT 0.12: ~/.sbt/sonatype.sbt) file:

::

  credentials += Credentials("Sonatype Nexus Repository Manager",
                             "oss.sonatype.org",
                             "<your username>",
                             "<your password>")

Then:

1. Copy content of
     publish/build.sbt.end   to the end of build.sbt
     publish/plugins.sbt.end to the end of project/plugins.sbt
2. Run ``sbt publish-signed``. Alternatively you can run ``sbt`` then from SBT
   command prompt run ``+ publish-signed``.
3. Login at https://oss.sonatype.org/ and from "Staging Repositories" select the
   newly published item, click "Close" then "Release".
