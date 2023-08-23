# License

This is a fork from the original MAT project: https://git.eclipse.org/c/mat/org.eclipse.mat.git/

It is licensed under `Eclipse Public License`, as available in https://git.eclipse.org/c/mat/org.eclipse.mat.git/tree/LICENSE

# Synchronization

synced as of commit #98f84e9ae8ff1a40e3f13e451ad7b830e70d5218
https://github.com/blasd/mat2/commit/98f84e9ae8ff1a40e3f13e451ad7b830e70d5218

Original repo: https://git.eclipse.org/c/mat/org.eclipse.mat.git

A Stand-Alone version of Eclipse MAT. Comparable to vshor_mat but with a much more up-to-date version

It can be used by calling the class:

        org.eclipse.mat.snapshot.MainSnapshotPreparer <pathToHProf>

Analysis over OffHeap:

OQL: `SELECT dbb AS DirectByteBuffer, dbb.capacity AS capacity FROM java.nio.DirectByteBuffer dbb`
