
# Release process

## Tag and Publish

Do not -DskipTests, else it will skip javadoc which is mandatory for sonatype
Do not -T 8 else the prompt for GPG may be lost in the logs

        mvn release:clean release:prepare release:perform -DskipStyle -Darguments="-Dmaven.test.skip -DskipStyle -Dmaven.javadoc.skip=false"
        git push --follow-tags

## In case of error:

    mvn release:rollback

or

        mvn reset --hard HEAD~~
        git tag -d pepper-XXX
        git push --delete origin pepper-XXX
