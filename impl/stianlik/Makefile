CC=gcc
CFLAGS=-O3 -Wall -pedantic -std=c99 -fopenmp

all: 
	$(CC) $(CFLAGS) linked_list.c gc.c bnl.c -o bnl

main.o:
	$(CC) -c $(CFLAGS) main.c -o main.o

experiment1:
	./bnl 1 > ../results/experiment1/C_01
	./bnl 2 > ../results/experiment1/C_02
	./bnl 4 > ../results/experiment1/C_03
	./bnl 6 > ../results/experiment1/C_04
	./bnl 12 > ../results/experiment1/C_05
	./bnl 24 > ../results/experiment1/C_06
	./bnl 32 > ../results/experiment1/C_07
	./bnl 64 > ../results/experiment1/C_08
	./bnl 128 > ../results/experiment1/C_09
	./bnl 256 > ../results/experiment1/C_10
	./bnl 512 > ../results/experiment1/C_11
	./bnl 1024 > ../results/experiment1/C_12

clean:
	-rm *.o
