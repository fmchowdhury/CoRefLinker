export TRAIN_DATA_DIR="-traindata  WRITE_HERE_FULL_PATH_OF_THE_TRAINING_DATA"
export TEST_DATA_DIR="-testdata  WRITE_HERE_FULL_PATH_OF_THE_TEST_DATA"

export CLinker_DIR=WRITE_HERE_FULL_PATH_OF_CLinker

export SVM_LIGHT_TK=$CLinker_DIR/SVM-Light-TK-1.5/SVM-Light-1.5-to-be-release

export OUT_DIR=$CLinker_DIR/tmp

export OUT_FILE=$OUT_DIR/trace

export PRED_FILE=$SVM_LIGHT_TK/svm_predictions
export All_PRED_FILE=$OUT_DIR/base.stat.in

export TRACE_FILE=$OUT_DIR/trace

export LIB_DIR=$CLinker_DIR/lib

#################################################
# Initialization of SVM-Light-TK parameters
#################################################

# values of 't':
# 5 = 1 tree + 1 vector kernels
# 0 = 1 vector kernel (linear)
# 50 = 2 vector kernels
# 502 = 2 vector + 1 tree kernels
#

# Do parameter tuning and set parameter values below. Default values (i.e. not tuned) are shown below.

T=1.0
t=0
C="V" # + 
F="4"
cost=0.2
b=1
lambda=0.4
mu=0.4

m=2096
U=0


#####################################################--------------------------
# Run CLinker to generate training and test instances for CLASSIFICATION
#####################################################--------------------------

echo "$(date) -> Generating instances....."

java  -Xmx2000m  -XX:MaxPermSize=512m -cp "./bin:$LIB_DIR/stanford-parser-2012-03-09-models.jar:$LIB_DIR/stanford-parser.jar" CoRef.CoRefMain $TRAIN_DATA_DIR  $TEST_DATA_DIR

#####################################################
# Train and CLASSIFY using SVM-Light-TK-1.5
#####################################################


# Run the tool
fnRun()
{
	Parameters="-t $1 -C $2 -F $3  -c $4 -b $5 -L $6 -M $7 -m $8 -U $9 -T $T -V L" 
	testdir=$TEST_DATA_DIR/predict

	cd $SVM_LIGHT_TK

	rm $OUT_FILE

		pos=`grep "^1" $CLinker_DIR/tmp/train.in | wc -l`
	        neg=`grep "^-1" $CLinker_DIR/tmp/train.in | wc -l`
        	cf=`echo $neg / $pos | bc -l`
	    #    echo "pos=$pos neg=$neg cf=$cf" >> $OUT_FILE
		
		echo "$(date) -> Learning model from the training data......"
		#./svm_learn $Parameters -j $cf $CLinker_DIR/tmp/train.in $CLinker_DIR/tmp/model
		
	#echo $testdir
	echo "$(date) -> Making prediction on test data......"
	for fname in $testdir/*
	do
		if [ ! -d $fname ]
		then						
			[[ $fname = *stat.in* ]] && flag="1" || [[ $fname = *pairs* ]] && flag="1" || flag="0"

			if [ "$flag" = "0" ]
			then			
				#echo "---- Testing $fname"
				rm $PRED_FILE
		#		echo "For $fname" >> $OUT_FILE
				./svm_classify $fname  $CLinker_DIR/tmp/model >> $OUT_FILE
				less $PRED_FILE > $fname.stat.in
				
		#		echo ""  >> $OUT_FILE
			fi			
		fi	
	done
}


echo "" > $TRACE_FILE

fnRun $t $C $F $cost $b $lambda $mu $m $U $T

echo "$(date) -> CLASSIFICATION done."

#####################################################
# Run CLinker to CLUSTER pairs into chains
#####################################################

cd $CLinker_DIR

java  -Xmx2000m  -XX:MaxPermSize=512m -cp "./bin:$LIB_DIR/stanford-parser-2012-03-09-models.jar:$LIB_DIR/stanford-parser.jar" CoRef.CoRefMain $TRAIN_DATA_DIR  $TEST_DATA_DIR -cluster

echo ""
echo ""
echo "$(date) -> Extracted coreference chains are written in the directory $TEST_DATA_DIR/chains_out"
