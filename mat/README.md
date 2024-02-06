A Stand-Alone version of Eclipse MAT. Comparable to ~vshor/mat (https://bitbucket.org/vshor/mat)~ but with a much more up-to-date version, and some changes to reduce the Heap requirements of MAT.

# License

This is a fork from the original MAT project: https://git.eclipse.org/c/mat/org.eclipse.mat.git/

It is licensed under `Eclipse Public License`, as available in https://git.eclipse.org/c/mat/org.eclipse.mat.git/tree/LICENSE

# Synchronization

synced as of commit #98f84e9ae8ff1a40e3f13e451ad7b830e70d5218
https://github.com/blacelle/org.eclipse.mat/commit/98f84e9ae8ff1a40e3f13e451ad7b830e70d5218

Original repo: https://git.eclipse.org/c/mat/org.eclipse.mat.git

## Synchronization procedure:

### How to sync https://github.com/blacelle/org.eclipse.mat

```
   git clone git@github.com:blacelle/org.eclipse.mat.git
   cd org.eclipse.mat
   git remote add eclipse	https://git.eclipse.org/r/mat/org.eclipse.mat.git
   git fetch eclipse
   git push origin eclipse/master:master
   git push origin --tags
```

# Usage

It can be used by calling the class:

        org.eclipse.mat.snapshot.MainSnapshotPreparer <pathToHProf>

Analysis over OffHeap:

OQL: `SELECT dbb AS DirectByteBuffer, dbb.capacity AS capacity FROM java.nio.DirectByteBuffer dbb`
