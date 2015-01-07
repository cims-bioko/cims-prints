/*******************************************************************************

License: 
This software was developed at the National Institute of Standards and 
Technology (NIST) by employees of the Federal Government in the course 
of their official duties. Pursuant to title 17 Section 105 of the 
United States Code, this software is not subject to copyright protection 
and is in the public domain. NIST assumes no responsibility  whatsoever for 
its use by other parties, and makes no guarantees, expressed or implied, 
about its quality, reliability, or any other characteristic. 

This software has been determined to be outside the scope of the EAR
(see Part 734.3 of the EAR for exact details) as it has been created solely
by employees of the U.S. Government; it is freely distributed with no
licensing requirements; and it is considered public domain.  Therefore,
it is permissible to distribute this software as a free download from the
internet.

Disclaimer: 
This software was developed to promote biometric standards and biometric
technology testing for the Federal Government in accordance with the USA
PATRIOT Act and the Enhanced Border Security and Visa Entry Reform Act.
Specific hardware and software products identified in this software were used
in order to perform the software development.  In no case does such
identification imply recommendation or endorsement by the National Institute
of Standards and Technology, nor does it imply that the products and equipment
identified are necessarily the best available for the purpose.  

*******************************************************************************/


#ifndef _NFIQ_H
#define _NFIQ_H

/* UPDATED: 11/21/2006 by KKO */

#include <defs.h>
#include <lfs.h>
#include <mlp.h>

#ifndef DEFAULT_PPI
#define DEFAULT_PPI       500
#endif

#define NFIQ_VCTRLEN      11
#define NFIQ_NUM_CLASSES  5
#define EMPTY_IMG         1
#define EMPTY_IMG_QUAL    5
#define TOO_FEW_MINUTIAE  2
#define MIN_MINUTIAE      5
#define MIN_MINUTIAE_QUAL 5

/***********************************************************************/
/* NFIQ.C : NFIQ supporting routines */
extern int comp_nfiq_featvctr(float *, const int, MINUTIAE *,
                              int *, const int, const int, int *);
int comp_nfiq(int *, float *, unsigned char *,
              const int, const int, const int, const int, int *);
int comp_nfiq_flex(int *, float *, unsigned char *,
              const int, const int, const int, const int,
              float *, float *, const int, const int, const int,
              const char, const char, float *, int *);

/***********************************************************************/
/* ZNORM.C : Routines supporting Z-Normalization */
extern void znorm_fniq_featvctr(float *, float *, float *, const int);
extern int comp_znorm_stats(float **, float **, float *,
                            const int, const int);

/***********************************************************************/
/* NFIQGBLS.C : Global variables supporting NFIQ */
extern float dflt_znorm_means[];
extern float dflt_znorm_stds[];
extern char  dflt_purpose;
extern int   dflt_nInps;
extern int   dflt_nHids;
extern int   dflt_nOuts;
extern char  dflt_acfunc_hids;
extern char  dflt_acfunc_outs;
extern float dflt_wts[];

/***********************************************************************/
/* NFIQREAD.C */
extern int read_imgcls_file(char *, char ***, char ***, int *);
extern int read_znorm_file(char *, float *, float *, const int);

#endif /* !_NFIQ_H */
