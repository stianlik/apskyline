all:
	cd doc; make; cd -;
	#cd impl/src; make; cd -;

deploy:
	rsync -arzv --delete --exclude=impl/results ./impl dascosa11.idi.ntnu.no:master
	rsync -arzv --delete ./Makefile dascosa11.idi.ntnu.no:master

plot:
	cd doc; make plot

clean:
	cd doc; make clean; cd -;
	cd impl/src; make clean; cd -;

# Experiment 1

experiment1: deploy
	ssh dascosa11.idi.ntnu.no 'cd master; nohup make _experiment1'
	make _experiment1_pull
_experiment1:
	mkdir -p impl/results/experiment1
	cd impl/stianlik; make clean; make && make experiment1
	cd impl/workspace/Skyline; make clean; make && make experiment1
_experiment1_pull:
	rsync -arzv --delete dascosa11.idi.ntnu.no:master/impl/results/experiment1/ ./impl/results/experiment1

# Experiment 2

experiment2: deploy
	ssh dascosa11.idi.ntnu.no 'cd master; nohup make _experiment2'
	make _experiment2_pull
_experiment2:
	mkdir -p impl/results/experiment2
	cd impl/workspace/Skyline; make clean; make && make experiment2
_experiment2_pull:
	rsync -arzv --delete dascosa11.idi.ntnu.no:master/impl/results/experiment2/ ./impl/results/experiment2
