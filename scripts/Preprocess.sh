#!/bin/bash

#########################################################
# command:  Preprocess.sh inputFolder outputFolder    #
#
# farsi files should end with .fa or .fa[0-9]       #
#########################################################


export LD_LIBRARY_PATH=$E4MTPath/lib
confPath=Configs/E4MT.conf #you can change this to use a more updated version

if [ ! -x "$E4MTPath/bin/E4MT" ]; then
    echo "E4MT not found. please set E4MTPath environment variable to a location including E4MT binary, libraries"
fi

inputFolder=$( realpath "$1/" )
outputFolder=$( realpath "$2/" )

removeAllTags() {
    perl -pe 's|<(.+?)>(.*?)</\1>|\2|g;s|<| &lt; |g;s|>| &gt; |g' <&0
}
convertSpaces() {
    perl -pe "s|\xe2\x80\x8c| |g;s|  *| |g" <&0
}

for file in $( find $inputFolder -type f -print0 | xargs -0 -I{} printf "%s\n" {} ); do
    outFile=$( echo $file | sed "s#$inputFolder#$outputFolder#g" )
    mkdir -p $(dirname $outFile)
    Lang=en
    if [[ $file == *.fa || $file == *.fa[0-9]* ]] ; then
        Lang=fa
    fi
    $E4MTPath/bin/E4MT -r -t -f $file -l $Lang -c $confPath | removeAllTags | convertSpaces > $outFile

done



