name = 'report'
plot = cd ../impl/util; ./plotty 
dir = '../results/processed'

all:
	pdflatex $(name).tex
	bibtex $(name).aux
	pdflatex $(name).tex
	pdflatex $(name).tex
	-rm ../report.pdf
	ln -s doc/$(name).pdf ../report.pdf

plot_1:
	$(plot) 1/runtime1 normal ../results/processed/1/runtime1/*
	$(plot) 1/segmented1 segmented ../results/processed/1/segmented1/*
	$(plot) 1/partitioning time_partitioning ../results/processed/1/partitioning/*
	$(plot) 1/segmented2 segmented ../results/processed/1/segmented2/*
	$(plot) 1/runtime2 normal ../results/processed/1/runtime2/*
	$(plot) 1/speedup speedup ../results/processed/1/speedup/*
plot_programming:
	$(plot) experiment1_time normal ../results/experiment1/*
	$(plot) experiment1_speedup speedup ../results/experiment1/*
plot_cpu:
	$(plot) cpu/runtime normal ../results/processed/cpu/filtered/*
	$(plot) cpu/speedup speedup ../results/processed/cpu/filtered/*
plot_threadcount:
	$(plot) 2/runtime normal ../results/processed/threadcount/filtered/*
	$(plot) 2/speedup speedup ../results/processed/threadcount/filtered/*
	$(plot) 2/pskyline_segmented segmented ../results/processed/threadcount/filtered/psky*
	$(plot) 2/apskyline_segmented segmented $(dir)/threadcount/filtered/pp* #$(dir)/threadcount/filtered/dap*
plot_dimensionality:
	$(plot) dimensionality/runtime dimensionality ../results/processed/dimensionality/filtered/*
	$(plot) dimensionality/pskyline_segmented dimensionality_segmented $(dir)/dimensionality/filtered/psk*
	$(plot) dimensionality/apskyline_segmented dimensionality_segmented $(dir)/dimensionality/filtered/*dap* $(dir)/dimensionality/filtered/pp*
plot_cardinality:
	$(plot) cardinality/runtime cardinality ../results/processed/cardinality/filtered/*
	$(plot) cardinality/perstuple cardinality_perstyple ../results/processed/cardinality/filtered/*
	$(plot) cardinality/pskyline_segmented cardinality_segmented $(dir)/cardinality/filtered/psk*
	$(plot) cardinality/apskyline_segmented cardinality_segmented $(dir)/cardinality/filtered/dap* $(dir)/cardinality/filtered/pp*
plot_distribution:
	$(plot) distribution/indep distribution ../results/processed/distribution/filtered/*_indep*
	$(plot) distribution/corr distribution ../results/processed/distribution/filtered/*_corr*
	$(plot) distribution/acorr distribution ../results/processed/distribution/filtered/*_acorr*
	$(plot) distribution/nba distribution ../results/processed/distribution/filtered/*_nba*
	$(plot) distribution/houses distribution ../results/processed/distribution/filtered/*_houses*
	$(plot) distribution/zillow distribution ../results/processed/distribution/filtered/*_zillow*
	$(plot) distribution/zillow6d distribution ../results/processed/distribution/zillow6d/*
plot_3:
	#$(plot) 3/indep_runtime normal ../results/processed/3/indep/*
	#$(plot) 3/indep_speedup speedup ../results/processed/3/indep/*
	#$(plot) 3/indep_segmented segmented ../results/processed/3/indep/pp*
	#$(plot) 3/indep_skyline skyline_size ../results/processed/3/indep/*
	#$(plot) 3/corr_runtime normal ../results/processed/3/corr/*
	#$(plot) 3/corr_speedup speedup ../results/processed/3/corr/*
	#$(plot) 3/corr_segmented segmented ../results/processed/3/corr/pp*
	#$(plot) 3/corr_skyline skyline_size ../results/processed/3/corr/*
	#$(plot) 3/acorr_runtime normal ../results/processed/3/acorr/*
	#$(plot) 3/acorr_speedup speedup ../results/processed/3/acorr/*
	#$(plot) 3/acorr_segmented segmented ../results/processed/3/acorr/pp*
	#$(plot) 3/acorr_skyline skyline_size ../results/processed/3/acorr/*
	#$(plot) 3/houses_runtime normal ../results/processed/3/houses/*
	#$(plot) 3/houses_nopart time_minus_partitioning ../results/processed/3/houses/*
	#$(plot) 3/houses_speedup speedup ../results/processed/3/houses/*
	#$(plot) 3/houses_segmented segmented ../results/processed/3/houses/pp*
	#$(plot) 3/houses_skyline skyline_size ../results/processed/3/houses/*
	#$(plot) 3/nba_runtime normal ../results/processed/3/nba/*
	#$(plot) 3/nba_nopart time_minus_partitioning ../results/processed/3/nba/*
	#$(plot) 3/nba_speedup speedup ../results/processed/3/nba/*
	#$(plot) 3/nba_segmented segmented ../results/processed/3/nba/pp*
	#$(plot) 3/nba_skyline skyline_size ../results/processed/3/nba/*
	$(plot) 3/zillow_runtime normal ../results/processed/3/zillow/*
	$(plot) 3/zillow_nopart time_minus_partitioning ../results/processed/3/zillow/*
	$(plot) 3/zillow_speedup speedup ../results/processed/3/zillow/*
	$(plot) 3/zillow_segmented segmented ../results/processed/3/zillow/dap*
	$(plot) 3/zillow_segmented_pskyline segmented ../results/processed/3/zillow/pskyline*
	$(plot) 3/zillow_skyline skyline_size ../results/processed/3/zillow/*
plot_4:
	$(plot) 4/10m_acorr_runtime normal ../results/processed/4/10m_acorr/*
	$(plot) 4/10m_acorr_speedup speedup ../results/processed/4/10m_acorr/*
	$(plot) 4/50m_acorr_runtime normal ../results/processed/4/50m_acorr/*
	$(plot) 4/50m_acorr_speedup speedup ../results/processed/4/50m_acorr/*
	$(plot) 4/2d_acorr_runtime normal ../results/processed/4/2d_acorr/*
	$(plot) 4/2d_acorr_speedup speedup ../results/processed/4/2d_acorr/*
	$(plot) 4/3d_acorr_runtime normal ../results/processed/4/3d_acorr/*
	$(plot) 4/3d_acorr_speedup speedup ../results/processed/4/3d_acorr/*
	$(plot) 4/4d_acorr_runtime normal ../results/processed/4/4d_acorr/*
	$(plot) 4/4d_acorr_speedup speedup ../results/processed/4/4d_acorr/*
plot_a:
	$(plot) a/indep_runtime normal ../results/experiment2/test1_indep1cpu/*
	$(plot) a/indep_speedup speedup ../results/experiment2/test1_indep1cpu/*
	$(plot) a/acorr_runtime normal ../results/experiment2/test1_acorr1cpu/*
	$(plot) a/acorr_speedup speedup ../results/experiment2/test1_acorr1cpu/*
	$(plot) a/corr_runtime normal ../results/experiment2/test1_corr1cpu/*
	$(plot) a/corr_speedup speedup ../results/experiment2/test1_corr1cpu/*

plot2.1:
	# Experiment 2: Test 1 uniform
	$(plot) experiment2_indep_runtime normal ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_runtime_segmented segmented ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_speedup speedup ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_skyline_size skyline_size ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_runtime_nopart time_minus_partitioning ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_speedup_nopart speedup_minus_partitioning ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_runtime_part time_partitioning ../results/experiment2/test1_indep/*
	$(plot) experiment2_indep_runtime_merge time_merge ../results/experiment2/test1_indep/*
	# Experiment 2: Test 1 correlated
	$(plot) experiment2_corr_runtime normal ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_runtime_segmented segmented ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_speedup speedup ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_skyline_size skyline_size ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_runtime_nopart time_minus_partitioning ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_speedup_nopart speedup_minus_partitioning ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_runtime_part time_partitioning ../results/experiment2/test1_corr/*
	$(plot) experiment2_corr_runtime_merge time_merge ../results/experiment2/test1_corr/*
	# Experiment 2: Test 1 anti-correlated
	$(plot) experiment2_acorr_runtime normal ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_runtime_segmented segmented ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_speedup speedup ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_skyline_size skyline_size ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_runtime_nopart time_minus_partitioning ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_speedup_nopart speedup_minus_partitioning ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_runtime_part time_partitioning ../results/experiment2/test1_acorr/*
	$(plot) experiment2_acorr_runtime_merge time_merge ../results/experiment2/test1_acorr/*

plot2.2uniform:
	$(plot) test2/indep_runtime normal ../results/experiment2/test2_indep/*
	$(plot) test2/indep_runtime_segmented segmented ../results/experiment2/test2_indep/*
	$(plot) test2/indep_speedup speedup ../results/experiment2/test2_indep/*
	$(plot) test2/indep_skyline_size skyline_size ../results/experiment2/test2_indep/*
	$(plot) test2/indep_runtime_nopart time_minus_partitioning ../results/experiment2/test2_indep/*
	$(plot) test2/indep_speedup_nopart speedup_minus_partitioning ../results/experiment2/test2_indep/*
	$(plot) test2/indep_runtime_part time_partitioning ../results/experiment2/test2_indep/*
	$(plot) test2/indep_runtime_merge time_merge ../results/experiment2/test2_indep/*
plot2.2corr:
	$(plot) test2/corr_runtime normal ../results/experiment2/test2_corr/*
	$(plot) test2/corr_runtime_segmented segmented ../results/experiment2/test2_corr/*
	$(plot) test2/corr_speedup speedup ../results/experiment2/test2_corr/*
	$(plot) test2/corr_skyline_size skyline_size ../results/experiment2/test2_corr/*
	$(plot) test2/corr_runtime_nopart time_minus_partitioning ../results/experiment2/test2_corr/*
	$(plot) test2/corr_speedup_nopart speedup_minus_partitioning ../results/experiment2/test2_corr/*
	$(plot) test2/corr_runtime_part time_partitioning ../results/experiment2/test2_corr/*
	$(plot) test2/corr_runtime_merge time_merge ../results/experiment2/test2_corr/*
plot2.2acorr:
	$(plot) test2/acorr_runtime normal ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_runtime_segmented segmented ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_speedup speedup ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_skyline_size skyline_size ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_runtime_nopart time_minus_partitioning ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_speedup_nopart speedup_minus_partitioning ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_runtime_part time_partitioning ../results/experiment2/test2_acorr/*
	$(plot) test2/acorr_runtime_merge time_merge ../results/experiment2/test2_acorr/*

plot2.3uniform:
	# Experiment 2: Test 3 uniform
	$(plot) test3/indep_runtime normal ../results/experiment2/test3_indep/*
	$(plot) test3/indep_runtime_segmented segmented ../results/experiment2/test3_indep/*
	$(plot) test3/indep_speedup speedup ../results/experiment2/test3_indep/*
	$(plot) test3/indep_skyline_size skyline_size ../results/experiment2/test3_indep/*
	$(plot) test3/indep_runtime_nopart time_minus_partitioning ../results/experiment2/test3_indep/*
	$(plot) test3/indep_speedup_nopart speedup_minus_partitioning ../results/experiment2/test3_indep/*
	$(plot) test3/indep_runtime_part time_partitioning ../results/experiment2/test3_indep/*
	$(plot) test3/indep_runtime_merge time_merge ../results/experiment2/test3_indep/*

plot2.4uniform:
	# Experiment 2: Test 4 uniform
	$(plot) test4/indep_runtime normal ../results/experiment2/test4_indep/*
	$(plot) test4/indep_runtime_segmented segmented ../results/experiment2/test4_indep/*
	$(plot) test4/indep_speedup speedup ../results/experiment2/test4_indep/*
	$(plot) test4/indep_skyline_size skyline_size ../results/experiment2/test4_indep/*
	$(plot) test4/indep_runtime_nopart time_minus_partitioning ../results/experiment2/test4_indep/*
	$(plot) test4/indep_speedup_nopart speedup_minus_partitioning ../results/experiment2/test4_indep/*
	$(plot) test4/indep_runtime_part time_partitioning ../results/experiment2/test4_indep/*
	$(plot) test4/indep_runtime_merge time_merge ../results/experiment2/test4_indep/*

plot2.5uniform:
	# Experiment 2: Test 5 uniform
	$(plot) test5/indep_runtime normal ../results/experiment2/test5_indep/*
	$(plot) test5/indep_runtime_segmented segmented ../results/experiment2/test5_indep/*
	$(plot) test5/indep_speedup speedup ../results/experiment2/test5_indep/*
	$(plot) test5/indep_skyline_size skyline_size ../results/experiment2/test5_indep/*
	$(plot) test5/indep_runtime_nopart time_minus_partitioning ../results/experiment2/test5_indep/*
	$(plot) test5/indep_speedup_nopart speedup_minus_partitioning ../results/experiment2/test5_indep/*
	$(plot) test5/indep_runtime_part time_partitioning ../results/experiment2/test5_indep/*
	$(plot) test5/indep_runtime_merge time_merge ../results/experiment2/test5_indep/*

clean:
	-rm *.aux *.log *.bbl *.blg *.out *.toc *.lof
