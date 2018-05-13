#!/bin/bash
die () {
    echo >&2 "$@"
    exit 1
}
[ "$#" -eq 1 ] || die "USAGE: $0 algorithm"
alg=$1
#mv ${alg}_acorr_N10000000_D5_0024	${alg}_acorr_N10000000_D5_0024
mv ${alg}_acorr_N1000000_D5_0024	${alg}_acorr_N01000000_D5_0024  
mv ${alg}_acorr_N100000_D5_0024		${alg}_acorr_N00100000_D5_0024
#mv ${alg}_acorr_N15000000_D5_0024	${alg}_acorr_N15000000_D5_0024
mv ${alg}_acorr_N5000000_D5_0024	${alg}_acorr_N05000000_D5_0024
mv ${alg}_acorr_N500000_D5_0024		${alg}_acorr_N00500000_D5_0024  
mv ${alg}_acorr_N50000_D5_0024		${alg}_acorr_N00050000_D5_0024
