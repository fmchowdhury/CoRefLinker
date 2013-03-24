/***********************************************************************/
/*                                                                     */
/*   svm_classify.c                                                    */
/*                                                                     */
/*   Classification module of Support Vector Machine.                  */
/*                                                                     */
/*   Author: Thorsten Joachims                                         */
/*   Date: 02.07.02                                                    */
/*                                                                     */
/*   Copyright (c) 2002  Thorsten Joachims - All rights reserved       */
/*                                                                     */
/*   This software is available for non-commercial use only. It must   */
/*   not be modified and distributed without prior permission of the   */
/*   author. The author is not responsible for implications from the   */
/*   use of this software.                                             */
/*                                                                     */
/************************************************************************/

# include "svm_common.h"
char docfile[200];
char modelfile[200];
char predictionsfile[200];

void read_input_parameters(int, char **, char *, char *, char *, long *, 
			   long *);
void print_help(void);


int main (int argc, char* argv[])
{

  DOC doc;   /* test example */
  long max_docs,max_words_doc,lld,llsv;
  long max_sv,max_words_sv,totdoc=0;
  long correct=0,incorrect=0,no_accuracy=0;
  long res_tp=0,res_fp=0,res_fn=0,res_tn=0,wnum,pred_format;
  long i,j;
  double t1,runtime=0;
  double dist,doc_label;
  char *line; 
  FILE *predfl,*docfl;
  MODEL model; 

  // <faisal 9-Feb-2011> 
  int relation_id = 0;

  read_input_parameters(argc,argv,docfile,modelfile,predictionsfile,
			&verbosity,&pred_format);
			
  /* STANDARD SVM KERNELS */
      			

  nol_ll(docfile,&max_docs,&max_words_doc,&lld); /* scan size of input file */
  max_words_doc+=2;
  lld+=2;
  nol_ll(modelfile,&max_sv,&max_words_sv,&llsv); /* scan size of model file */
  max_words_sv+=2;
  llsv+=2;

  model.supvec = (DOC **)my_malloc(sizeof(DOC *)*max_sv);
  model.alpha = (double *)my_malloc(sizeof(double)*max_sv);

  line = (char *)my_malloc(sizeof(char)*lld);
//  doc.words = (WORD *)my_malloc(sizeof(WORD)*(max_words_doc+10));

  read_model(modelfile,&model,max_words_sv,llsv);

  if(model.kernel_parm.kernel_type == 0) { /* linear kernel */
    /* compute weight vector */
    add_weight_vector_to_linear_model(&model);
  }
  
  if(verbosity>=2) {
    printf("Classifying test examples.."); fflush(stdout);
  }

  if ((docfl = fopen (docfile, "r")) == NULL)
  { perror (docfile); exit (1); }
  if ((predfl = fopen (predictionsfile, "w")) == NULL)
  { perror (predictionsfile); exit (1); }

  while((!feof(docfl)) && fgets(line,(int)lld,docfl)) {
//    if(line[0] == '#') continue;  /* line contains comments */
    doc.docnum = totdoc+1;  
    parse_document(line,&doc,&doc_label,&wnum,max_words_doc,&model.kernel_parm);
    totdoc++;

    if(model.kernel_parm.kernel_type == 0) {   /* linear kernel */
      for(j=0;(doc.vectors[0]->words[j]).wnum != 0;j++) {  /* Check if feature numbers   */
	if((doc.vectors[0]->words[j]).wnum>model.totwords) /* are not larger than in     */
	  (doc.vectors[0]->words[j]).wnum=0;               /* model. Remove feature if   */
      }                                        /* necessary.                 */
      t1=get_runtime();
      dist=classify_example_linear(&model,&doc);
      runtime+=(get_runtime()-t1);
    }
    else {                             /* non-linear kernel */
      t1=get_runtime();
      dist=classify_example(&model,&doc);
      runtime+=(get_runtime()-t1);
    }

    if(dist>0) {
	if(pred_format==0) { /* old weired output format */
		fprintf(predfl,"%.8g:+1 %.8g:-1\n",dist,-dist);
	}
      
	if(doc_label>0) correct++; else incorrect++;
	
	if(doc_label>0) res_tp++; else res_fp++;

      	//-- Start <faisal 9-Feb-2011> Inserting the following lines
	if(doc_label>0) 
		fprintf(predfl,"1.0 D-%d 1 %.8g\n",relation_id, dist);
	else
		fprintf(predfl,"0.0 D-%d 1 %.8g\n",relation_id, dist);

	relation_id++;
	//-- End <faisal 9-Feb-2011>
    }
    else {
	if(pred_format==0) { /* old weired output format */
	    fprintf(predfl,"%.8g:-1 %.8g:+1\n",-dist,dist);
     	}
     
	if(doc_label<0) correct++; else incorrect++;
      
	if(doc_label>0) {
		res_fn++; 
	      	//-- Start <faisal 9-Feb-2011> Inserting the following line
		fprintf(predfl,"0.0 D-%d 0 %.8g\n",relation_id, dist);
		relation_id++;
		//-- End <faisal 9-Feb-2011>
      	}
      	else {
		res_tn++;
		//-- Start <faisal 13-Feb-2011> Inserting the following line
		fprintf(predfl,"-1 D-%d 0 %.8g\n",relation_id, dist);
		relation_id++;
		//-- End <faisal 13-Feb-2011>
      	}	
    }

    if(pred_format==1) { /* output the value of decision function */
	//-- Start <faisal 9-Feb-2011> Commenting out the following lines
/*
	if(doc_label<0)
      	  fprintf(predfl,"1 %.8g\n",dist);
	else
      	  fprintf(predfl,"-1 %.8g\n",dist);
*/
	//-- End <faisal 9-Feb-2011>
    }
    if((int)(0.01+(doc_label*doc_label)) != 1) 
      { no_accuracy=1; } /* test data is not binary labeled */
    if(verbosity>=2) {
      if(totdoc % 100 == 0) {
	printf("%ld..",totdoc); fflush(stdout);
      }
    }
    freeExample(&doc);// free the trees in the data item
  }  
  free(line);

  for(i=1;i<model.sv_num;i++) {
  
//   if(model.supvec[i]->root!=NULL){
//	 freeTree(model.supvec[i]->root);  
//        freeList(model.supvec[i]);
//   }
       
    freeExample(model.supvec[i]);
  }
  free(model.supvec);
  free(model.alpha);
  if(model.kernel_parm.kernel_type == 0) { /* linear kernel */
    free(model.lin_weights);
  }

  if(verbosity>=2) {
    printf("done\n");

/*   Note by Gary Boone                     Date: 29 April 2000        */
/*      o Timing is inaccurate. The timer has 0.01 second resolution.  */
/*        Because classification of a single vector takes less than    */
/*        0.01 secs, the timer was underflowing.                       */
    printf("Runtime (without IO) in cpu-seconds: %.2f\n",
	   (float)(runtime/100.0));
    
  }
  if((!no_accuracy) && (verbosity>=1)) {
    float P,R;
    printf("Accuracy on test set: %.2f%% (%ld correct, %ld incorrect, %ld total)\n",(float)(correct)*100.0/totdoc,correct,incorrect,totdoc);
    printf("TP:%ld FP:%ld FN:%ld\n",res_tp,res_fp,res_fn);
    printf("Precision/recall on test set: %.2f%%/%.2f%%\n",P=(float)(res_tp)*100.0/(res_tp+res_fp),R=(float)(res_tp)*100.0/(res_tp+res_fn));
    printf("F1-measure on test set: %.2f%%\n",(float)(2*P*R)/(P+R));
  }

  return(0);
}

void read_input_parameters(int argc, char **argv, char *docfile, 
			   char *modelfile, char *predictionsfile, 
			   long int *verbosity, long int *pred_format)
{
  long i;
  
  /* set default */
  strcpy (modelfile, "svm_model");
  strcpy (predictionsfile, "svm_predictions"); 
  (*verbosity)=2;
  (*pred_format)=1;

  for(i=1;(i<argc) && ((argv[i])[0] == '-');i++) {
    switch ((argv[i])[1]) 
      { 
      case 'h': print_help(); exit(0);
      case 'v': i++; (*verbosity)=atol(argv[i]); break;
      case 'f': i++; (*pred_format)=atol(argv[i]); break;
      default: printf("\nUnrecognized option %s!\n\n",argv[i]);
	       print_help();
	       exit(0);
      }
  }
  if((i+1)>=argc) {
    printf("\nNot enough input parameters!\n\n");
    print_help();
    exit(0);
  }
  strcpy (docfile, argv[i]);
  strcpy (modelfile, argv[i+1]);
  if((i+2)<argc) {
    strcpy (predictionsfile, argv[i+2]);
  }
  if(((*pred_format) != 0) && ((*pred_format) != 1)) {
    printf("\nOutput format can only take the values 0 or 1!\n\n");
    print_help();
    exit(0);
  }
}

void print_help(void)
{
  printf("\nTree Kernel in SVM-light %s : SVM Classification module %s\n",VERSION,VERSION_DATE);
  printf("by Alessandro Moschitti, moschitti@info.uniroma2.it\n");
  printf("University of Rome \"Tor Vergata\"\n\n");

  copyright_notice();
  printf("   usage: svm_classify [options] example_file model_file output_file\n\n");
  printf("options: -h         -> this help\n");
  printf("         -v [0..3]  -> verbosity level (default 2)\n");
  printf("         -f [0,1]   -> 0: old output format of V1.0\n");
  printf("                    -> 1: output the value of decision function (default)\n\n");
}




