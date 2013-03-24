**************
 CoRefLinker
==============

CoRefLinker is a tool for coreference resolution for clinical text. It is an implementation of a variant of the influential mention-pair model for coreference resolution. It accepts input in the format of the i2b2 2011 shared task annotation (https://www.i2b2.org/NLP/Coreference/) and produces coreference chains in clinical documents.

Author: Md. Faisal Mahbub Chowdhury
  fmchowdhury@gmail.com

LICENSING TERMS 
----------------
This software is granted free of charge for research and education purposes. However you must obtain a license from the author to use it for commercial purposes.

Input Directory Structure
---------------------------
By Default CLinker assumes that all the input files (training or test) would be arraged as the following directory structure -

Parent_DIR
Parent_DIR/concepts  	(all the concept files should be here.)
Parent_DIR/chains   	(all the chain files should be here.)
Parent_DIR/docs   	(all the text documents should be here.)
Parent_DIR/parse_full   (all the parsed output files for the correspondign text documents should be here.)

Naming Input Files
--------------------
Lets say a text document has name "xyz123.txt". Then the other related files should be names as following -

concept file - xyz123.txt.con
chain file - xyz123.txt.chains
parsed output file - xyz123.txt.parsed

How To Run
------------
Set the parameters TRAIN_DATA_DIR and TEST_DATA_DIR in the run_clincker.sh 
Then run the file.

The output files of the system will be saved inside the directory "chains_out" inside TEST_DATA_DIR directory.

Evaluation
------------
We are not authorised to distribute the data and evaluation scripts of i2b2 challenge. Please contact the corresponding organizers for obtaining these resources.

How to cite
------------
If you use any part of the code or the ideas implemented of CLinker, please cite the following paper:

@article{chowdhury-jbi:2013,
 author = {Chowdhury, MFM and Zweigenbaum, P},
 title = {{A Controlled Greedy Supervised Approach for Co­reference Resolution on Clinical Text}},
 journal = {Journal of Biomedical Informatics (to appear)},
 year = {2013}
}

By default, CoRefLinker uses SVM-Light-TK toolkit for computing SVM models. You can use other SVM implementations instead of SVM-Light-TK by making necessary changes in run_clincker.sh. If you do use SVM-Light-TK, please read LICENSE_for_SVM-Light-TK-1.5.txt before using it.
