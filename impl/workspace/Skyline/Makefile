#!/bin/bash

CLASSPATH = bin:org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.7:lib/colt.jar:lib/commons-email-1.3.1.jar

RUN = java -cp $(CLASSPATH)
TEST = ../../util/runtest

all:
	javac -d bin -cp $(CLASSPATH) -sourcepath src src/no/ntnu/skyline/Experiment1.java
	javac -d bin -cp $(CLASSPATH) -sourcepath src src/no/ntnu/skyline/Experiment2.java

experiment1:
	$(RUN) no.ntnu.skyline.Experiment1 1 > ../../results/experiment1/JAVA_01
	$(RUN) no.ntnu.skyline.Experiment1 2 > ../../results/experiment1/JAVA_02
	$(RUN) no.ntnu.skyline.Experiment1 4 > ../../results/experiment1/JAVA_03
	$(RUN) no.ntnu.skyline.Experiment1 6 > ../../results/experiment1/JAVA_04
	$(RUN) no.ntnu.skyline.Experiment1 12 > ../../results/experiment1/JAVA_05
	$(RUN) no.ntnu.skyline.Experiment1 24 > ../../results/experiment1/JAVA_06
	$(RUN) no.ntnu.skyline.Experiment1 32 > ../../results/experiment1/JAVA_07
	$(RUN) no.ntnu.skyline.Experiment1 64 > ../../results/experiment1/JAVA_08
	$(RUN) no.ntnu.skyline.Experiment1 128 > ../../results/experiment1/JAVA_09
	$(RUN) no.ntnu.skyline.Experiment1 256 > ../../results/experiment1/JAVA_10
	$(RUN) no.ntnu.skyline.Experiment1 512 > ../../results/experiment1/JAVA_11
	$(RUN) no.ntnu.skyline.Experiment1 1024 > ../../results/experiment1/JAVA_12

experiment2:

# Varierer N

experiment2_test1_indep:
	#$(TEST) test1_indep bbs indep 5000000 5
	#$(TEST) test1_indep apskyline indep 5000000 5
	$(TEST) test1_indep ppskyline indep 5000000 5
	$(TEST) test1_indep dapskyline indep 5000000 5
	$(TEST) test1_indep pskyline indep 5000000 5
	$(TEST) test1_indep pskyline_nopart indep 5000000 5
	#$(TEST) test1_indep bnl indep 5000000 5
experiment2_test1_corr:
	#$(TEST) test1_corr bbs corr 5000000 5
	#$(TEST) test1_corr apskyline corr 5000000 5
	$(TEST) test1_corr ppskyline corr 5000000 5
	$(TEST) test1_corr dapskyline corr 5000000 5
	$(TEST) test1_corr pskyline corr 5000000 5
	$(TEST) test1_corr pskyline_nopart corr 5000000 5
	#$(TEST) test1_corr bnl corr 5000000 5
experiment2_test1_acorr:
	#$(TEST) test1_acorr bbs acorr 5000000 5
	#$(TEST) test1_acorr apskyline acorr 5000000 5
	$(TEST) test1_acorr ppskyline acorr 5000000 5
	$(TEST) test1_acorr dapskyline acorr 5000000 5
	$(TEST) test1_acorr pskyline acorr 5000000 5
	$(TEST) test1_acorr pskyline_nopart acorr 5000000 5
	#$(TEST) test1_acorr bnl acorr 5000000 5
experiment2_houses:
	$(TEST) test1_houses ppskyline houses 127931 6
	$(TEST) test1_houses dapskyline houses 127931 6
	$(TEST) test1_houses pskyline houses 127931 6
	$(TEST) test1_houses pskyline_nopart houses 127931 6
	$(TEST) test1_houses bnl houses 127931 6
experiment2_nba:
	$(TEST) test1_nba ppskyline nba 17265 5
	$(TEST) test1_nba dapskyline nba 17265 5
	$(TEST) test1_nba pskyline nba 17265 5
	$(TEST) test1_nba pskyline_nopart nba 17265 5
	$(TEST) test1_nba bnl nba 17265 5
experiment2_zillow:
	#$(TEST) test1_zillow ppskyline zillow 2245109 5
	$(TEST) test1_zillow dapskyline zillow 2245109 5
	#$(TEST) test1_zillow pskyline zillow 2245109 5
	#$(TEST) test1_zillow bnl zillow 2245109 5
	#$(TEST) test1_zillow pskyline_nopart zillow 2245109 5

experiment2_cpu:
	sudo /srv/admin/bin/cpuctl disable 1 3 5 7 9 11 13 15 17 19 21 23
	#$(TEST) cpu ppskyline acorr 5000000 5
	#$(TEST) cpu dapskyline acorr 5000000 5
	#$(TEST) cpu pskyline acorr 5000000 5
	$(TEST) cpu bnl acorr 5000000 5
	sudo /srv/admin/bin/cpuctl enable 1 3 5 7 9 11 13 15 17 19 21 23

experiment2_threadcount:
	$(TEST) threadcount adapskyline acorr 5000000 5

experiment2_dimensionality:
	$(TEST) test6_dimensionality adapskyline acorr 5000000 2
	$(TEST) test6_dimensionality adapskyline acorr 5000000 3
	$(TEST) test6_dimensionality adapskyline acorr 5000000 4
	$(TEST) test6_dimensionality adapskyline acorr 5000000 5
	#$(TEST) test6_dimensionality dapskyline acorr 5000000 2
	#$(TEST) test6_dimensionality pskyline acorr 5000000 2
	#$(TEST) test6_dimensionality bnl acorr 5000000 2
	#$(TEST) test6_dimensionality ppskyline acorr 5000000 2
	#$(TEST) test6_dimensionality dapskyline acorr 5000000 3
	#$(TEST) test6_dimensionality pskyline acorr 5000000 3
	#$(TEST) test6_dimensionality bnl acorr 5000000 3
	#$(TEST) test6_dimensionality ppskyline acorr 5000000 3
	#$(TEST) test6_dimensionality dapskyline acorr 5000000 4
	#$(TEST) test6_dimensionality pskyline acorr 5000000 4
	#$(TEST) test6_dimensionality bnl acorr 5000000 4
	#$(TEST) test6_dimensionality ppskyline acorr 5000000 4
	#$(TEST) test6_dimensionality dapskyline acorr 5000000 5
	#$(TEST) test6_dimensionality pskyline acorr 5000000 5
	#$(TEST) test6_dimensionality bnl acorr 5000000 5
	#$(TEST) test6_dimensionality ppskyline acorr 5000000 5

experiment2_cardinality:
	$(TEST) test6_cardinality adapskyline acorr 50000 5
	$(TEST) test6_cardinality adapskyline acorr 100000 5
	$(TEST) test6_cardinality adapskyline acorr 500000 5
	$(TEST) test6_cardinality adapskyline acorr 1000000 5
	#$(TEST) test6_cardinality adapskyline acorr 5000000 5
	$(TEST) test6_cardinality adapskyline acorr 10000000 5
	$(TEST) test6_cardinality adapskyline acorr 15000000 5
	#$(TEST) test6_cardinality dapskyline acorr 50000 5
	#$(TEST) test6_cardinality pskyline acorr 50000 5
	#$(TEST) test6_cardinality bnl acorr 50000 5
	#$(TEST) test6_cardinality ppskyline acorr 50000 5
	#$(TEST) test6_cardinality dapskyline acorr 100000 5
	#$(TEST) test6_cardinality pskyline acorr 100000 5
	#$(TEST) test6_cardinality bnl acorr 100000 5
	#$(TEST) test6_cardinality ppskyline acorr 100000 5
	#$(TEST) test6_cardinality dapskyline acorr 500000 5
	#$(TEST) test6_cardinality pskyline acorr 500000 5
	#$(TEST) test6_cardinality bnl acorr 500000 5
	#$(TEST) test6_cardinality ppskyline acorr 500000 5
	#$(TEST) test6_cardinality dapskyline acorr 1000000 5
	#$(TEST) test6_cardinality pskyline acorr 1000000 5
	#$(TEST) test6_cardinality bnl acorr 1000000 5
	#$(TEST) test6_cardinality ppskyline acorr 1000000 5
	#$(TEST) test6_cardinality dapskyline acorr 5000000 5
	#$(TEST) test6_cardinality pskyline acorr 5000000 5
	#$(TEST) test6_cardinality bnl acorr 5000000 5
	#$(TEST) test6_cardinality ppskyline acorr 5000000 5
	#$(TEST) test6_cardinality dapskyline acorr 10000000 5
	#$(TEST) test6_cardinality pskyline acorr 10000000 5
	#$(TEST) test6_cardinality bnl acorr 10000000 5
	#$(TEST) test6_cardinality ppskyline acorr 10000000 5
	#$(TEST) test6_cardinality dapskyline acorr 15000000 5
	#$(TEST) test6_cardinality pskyline acorr 15000000 5
	#$(TEST) test6_cardinality bnl acorr 15000000 5
	#$(TEST) test6_cardinality ppskyline acorr 15000000 5
	#$(TEST) test6_cardinality adapskyline acorr 20000000 5
	#$(TEST) test6_cardinality dapskyline acorr 20000000 5
	#$(TEST) test6_cardinality pskyline acorr 20000000 5
	#$(TEST) test6_cardinality bnl acorr 20000000 5
	#$(TEST) test6_cardinality ppskyline acorr 20000000 5

experiment2_distribution:
	$(TEST) distributionzill adapskyline zillow 2245109 6
	$(TEST) distributionzill pskyline zillow 2245109 5
	$(TEST) distributionzill dapskyline zillow 2245109 6
	$(TEST) distributionzill bnl zillow 2245109 5
	$(TEST) distributionzill ppskyline zillow 2245109 6
	#$(TEST) distribution adapskyline houses 127931 6
	#$(TEST) distribution adapskyline nba 17265 5
	#$(TEST) distribution adapskyline corr 5000000 5
	#$(TEST) distribution adapskyline indep 5000000 5
	#$(TEST) distribution adapskyline zillow 2245109 5
	#$(TEST) distribution adapskyline acorr 5000000 5
	#$(TEST) distribution dapskyline zillow 2245109 5
	#$(TEST) distribution pskyline zillow 2245109 5
	#$(TEST) distribution bnl zillow 2245109 5
	#$(TEST) distribution ppskyline zillow 2245109 5
	#$(TEST) distribution dapskyline acorr 5000000 5
	#$(TEST) distribution pskyline acorr 5000000 5
	#$(TEST) distribution bnl acorr 5000000 5
	#$(TEST) distribution ppskyline acorr 5000000 5
	#$(TEST) distribution dapskyline corr 5000000 5
	#$(TEST) distribution pskyline corr 5000000 5
	#$(TEST) distribution bnl corr 5000000 5
	#$(TEST) distribution ppskyline corr 5000000 5
	#$(TEST) distribution dapskyline indep 5000000 5
	#$(TEST) distribution pskyline indep 5000000 5
	#$(TEST) distribution bnl indep 5000000 5
	#$(TEST) distribution ppskyline indep 5000000 5

experiment2_test2_indep:
	$(TEST) test2_indep ppskyline indep 10000000 5
	$(TEST) test2_indep pskyline indep 10000000 5
	$(TEST) test2_indep bnl indep 10000000 5
experiment2_test2_indep_expansion:
	$(TEST) test2_indep ppskyline2 indep 10000000 5
experiment2_test2_corr:
	$(TEST) test2_corr ppskyline corr 10000000 5
	$(TEST) test2_corr pskyline corr 10000000 5
	$(TEST) test2_corr bnl corr 10000000 5
experiment2_test2_acorr:
	$(TEST) test2_acorr ppskyline acorr 10000000 5
	$(TEST) test2_acorr pskyline acorr 10000000 5
	$(TEST) test2_acorr bnl acorr 10000000 5

experiment2_test3_indep:
	$(TEST) test3_indep ppskyline indep 50000000 5
	$(TEST) test3_indep pskyline indep 50000000 5
	$(TEST) test3_indep bnl indep 50000000 5
experiment2_test3_corr:
	$(TEST) test3_corr ppskyline corr 50000000 5
	$(TEST) test3_corr pskyline corr 50000000 5
	$(TEST) test3_corr bnl corr 50000000 5
experiment2_test3_acorr:
	$(TEST) test3_acorr ppskyline acorr 50000000 5
	$(TEST) test3_acorr pskyline acorr 50000000 5
	$(TEST) test3_acorr bnl acorr 50000000 5

experiment2_test4_indep:
	#$(TEST) test4_indep ppskyline indep 100000000 5
	#$(TEST) test4_indep pskyline indep 100000000 5
	#$(TEST) test4_indep bnl indep 100000000 5
	$(TEST) test4_indep dapskyline indep 100000000 5
experiment2_test4_corr:
	#$(TEST) test4_corr ppskyline corr 100000000 5
	#$(TEST) test4_corr pskyline corr 100000000 5
	#$(TEST) test4_corr bnl corr 100000000 5
	$(TEST) test4_corr dapskyline corr 100000000 5
experiment2_test4_acorr:
	$(TEST) test4_acorr ppskyline acorr 100000000 5
	$(TEST) test4_acorr pskyline acorr 100000000 5
	$(TEST) test4_acorr bnl acorr 100000000 5

# Varierer D

experiment2_test5_indep:
	$(TEST) test5_indep ppskyline indep 10000000 2
	$(TEST) test5_indep pskyline indep 10000000 2
	$(TEST) test5_indep bnl indep 10000000 2
experiment2_test5_corr:
	$(TEST) test5_corr ppskyline corr 10000000 2
	$(TEST) test5_corr pskyline corr 10000000 2
	$(TEST) test5_corr bnl corr 10000000 2
experiment2_test5_acorr:
	$(TEST) test5_acorr ppskyline acorr 10000000 2
	$(TEST) test5_acorr pskyline acorr 10000000 2
	$(TEST) test5_acorr bnl acorr 10000000 2

experiment2_test6_indep:
	$(TEST) test6_indep ppskyline indep 10000000 3
	$(TEST) test6_indep pskyline indep 10000000 3
	$(TEST) test6_indep bnl indep 10000000 3
experiment2_test6_corr:
	$(TEST) test6_corr ppskyline corr 10000000 3
	$(TEST) test6_corr pskyline corr 10000000 3
	$(TEST) test6_corr bnl corr 10000000 3
experiment2_test6_acorr:
	$(TEST) test6_acorr ppskyline acorr 10000000 3
	$(TEST) test6_acorr pskyline acorr 10000000 3
	$(TEST) test6_acorr bnl acorr 10000000 3

experiment2_test7_indep:
	$(TEST) test7_indep ppskyline indep 10000000 4
	$(TEST) test7_indep pskyline indep 10000000 4
	$(TEST) test7_indep bnl indep 10000000 4
experiment2_test7_corr:
	$(TEST) test7_corr ppskyline corr 10000000 4
	$(TEST) test7_corr pskyline corr 10000000 4
	$(TEST) test7_corr bnl corr 10000000 4
experiment2_test7_acorr:
	$(TEST) test7_acorr ppskyline acorr 10000000 4
	$(TEST) test7_acorr pskyline acorr 10000000 4
	$(TEST) test7_acorr bnl acorr 10000000 4

clean:
	rm -r bin/*
