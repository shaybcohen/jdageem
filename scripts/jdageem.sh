#! /bin/bash

export PATH

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -Xmx2000m -cp $DIR/../lib/colt.jar:$DIR/../lib/commons-cli-1.2.jar:$DIR/../lib/commons-math-2.2.jar:$DIR/../lib/pcollections-2.1.2.jar:$DIR/../lib/concurrent.jar:$DIR/../lib/jdageem.jar:$DIR/../lib/trove.jar edu.cmu.cs.lti.ark.dageem.Dageem $@
