#!/bin/bash

# command:  Eval.sh OutputAddress ReferenceAddress [-p]
# the Reference should be one file or for multireferences they should be named as ReferenceAddress0 ReferenceAddress1 ... ReferenceAddressn
# -p could be given for ignoring punctuations

PWD=`pwd`
Dir=$(dirname $(readlink -f "$0"))

TestOutput=$1
TestRef=$2
punc=$3 

TestRefs=();
num="0";
if [ -e $TestRef ]; then
	TestRefs+=($TestRef)
	num="1"
else
	while [ -e $TestRef$num ]; do
	
		TestRefs=( "${TestRefs[@]}" "$TestRef$num" )
		num=$(($num+1));

	done
fi

echo "Number of References : " $num
echo  "Reference Files: " ${TestRefs[@]}

Evaluator=$Dir/bin

echo "------------------------------------------------------------------"

java -cp $Evaluator Evaluator $TestOutput $num ${TestRefs[@]} BLEU 4 -c $punc 2> /dev/null

echo "------------------------------------------------------------------"

java -cp $Evaluator Evaluator $TestOutput $num ${TestRefs[@]} TER 20 50 -c $punc 2> /dev/null

echo "------------------------------------------------------------------"
java -cp $Evaluator Evaluator $TestOutput $num ${TestRefs[@]} PER -c $punc 2> /dev/null

echo "------------------------------------------------------------------"
java -cp $Evaluator Evaluator $TestOutput $num ${TestRefs[@]} WER -c $punc 2> /dev/null

echo "------------------------------------------------------------------"
