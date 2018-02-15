# Machine Translation Evaluation scripts

This repository contains some scripts in order to evaluate a Machine Translation output.
It will report BLEU, TER, WER, and PER scores.

There are two scripts which must be run in sequence:

## Preprocess.sh

This script preprocess text files running E4MT on them to normalize at character-levevel and spell-correction.
To use this script you must first set the E4MT environment variable to a folder where E4MT binary and libraries reside
(e.g. $HOME/local if you have build and installed E4MT)

## Evaluate.sh

this script will evalute and compute all the score. keep note that all text files (including MT outputs and references) must be preprocessed and normalized prior to be evaluated

