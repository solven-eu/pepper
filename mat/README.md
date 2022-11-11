synced as of commit #98f84e9ae8ff1a40e3f13e451ad7b830e70d5218
https://github.com/blasd/mat2/commit/98f84e9ae8ff1a40e3f13e451ad7b830e70d5218

Original repo: https://git.eclipse.org/c/mat/org.eclipse.mat.git

A Stand-Alone version of Eclipse MAT. Comparable to vshor_mat but with a much more up-to-date version

It can be used by calling the class: 

    org.eclipse.mat.snapshot.MainSnapshotPreparer <pathToHProf>
    
# Resync with MAT official repo

- Go to https://git.eclipse.org/c/mat/org.eclipse.mat.git
- Download the latest tag as ZIP (e.g. https://git.eclipse.org/c/mat/org.eclipse.mat.git/snapshot/org.eclipse.mat-R_1.13.0.zip)
- Unzip and go into root folder
- Execute:

    cp -r ./plugins/org.eclipse.mat.api/src/org/eclipse/mat/* /Users/blacelle/workspace3/pepper/mat/src/main/java/org/eclipse/mat/
    cp -r ./plugins/org.eclipse.mat.hprof/src/org/eclipse/mat/* /Users/blacelle/workspace3/pepper/mat/src/main/java/org/eclipse/mat/
    cp -r ./plugins/org.eclipse.mat.hprof/src/io/nayuki/deflate/* /Users/blacelle/workspace3/pepper/mat/src/main/java/io/nayuki/deflate/
    cp -r ./plugins/org.eclipse.mat.parser/src/org/eclipse/mat/* /Users/blacelle/workspace3/pepper/mat/src/main/java/org/eclipse/mat/
    cp -r ./plugins/org.eclipse.mat.report/src/org/eclipse/mat/* /Users/blacelle/workspace3/pepper/mat/src/main/java/org/eclipse/mat/
    cp -r ./plugins/org.eclipse.mat.tests/src/org/eclipse/mat/* /Users/blacelle/workspace3/pepper/mat/src/test/java/org/eclipse/mat/
    
    rm -r /Users/blacelle/workspace3/pepper/mat/src/main/java/org/eclipse/mat/hprof/ui BUT KEEP HprofPreferences.java
    rm -r /Users/blacelle/workspace3/pepper/mat/src/test/java/org/eclipse/mat/tests/ui/snapshot/panes/textPartitioning


    
    