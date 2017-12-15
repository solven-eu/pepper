#!/bin/sh

#https://stackoverflow.com/questions/1011985/line-endings-messed-up-in-git-how-to-track-changes-from-another-branch-after-a

# git filter-branch --tree-filter '~/Scripts/fix-line-endings.sh' -- --all
find . -type f -a \( -name '*.tpl' -o -name '*.php' -o -name '*.js' -o -name '*.css' -o -name '*.sh' -o -name '*.txt' -iname '*.html' \) | xargs fromdos



# Another method: https://github.com/cnaj/demo-crlf-rewrite
# https://stackoverflow.com/questions/7672907/why-has-git-filter-branch-not-rewritten-tags
git filter-branch --tag-name-filter cat --tree-filter 'echo "* text=auto" > .gitattributes; git rm -q --cached -rf --ignore-unmatch *' -- --all
