# GitGraphGui

A simple gui that graphs the git repository and automatically updates.  This is
explicitly created not as a replacement from the command line but rather a way
to hot reload the log graph while I rebase branches.

## Usage

When ran, you may specify a directory to run it on.  By default this is the
current directory.

Output is similar to `git log --graph --pretty=oneline --all` where ref hashes
and ref names (branches and tags) can be clicked on to copy to the clipboard.
