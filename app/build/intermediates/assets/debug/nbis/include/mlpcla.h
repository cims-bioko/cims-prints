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


#ifndef _MLPCLA_H
#define _MLPCLA_H

/*****************************************************************/
/* Author: Michael D. Garris                                     */
/* Date:   03/17/2005                                            */
/*                                                               */
/* To handle proper prototyping and argument passing to CLAPCK   */
/* routines used by MLP library codes.  E.g. MLP codes are       */
/* written in single percision integer while the CBLAS routines  */
/* are written using long ints.                                  */
/*****************************************************************/

#include <f2c.h>

/* Cblas library routines used by MLP library codes */
extern int sgemv_(char *, int *, int *, real *, 
	    real *, int *, real *, int *, real *, real *, int *);
extern int sscal_(int *, real *, real *, int *);
extern int saxpy_(int *, real *, real *, int *, real *, int *);
extern doublereal sdot_(int *, real *, int *, real *, int *);
extern doublereal snrm2_(int *, real *, int *);

/* mlpcla.c */
extern int mlp_sgemv(char, int, int, float, float *, int, float *,
                     int, float, float *, int);
extern int mlp_sscal(int, float, float *, int);
extern int mlp_saxpy(int, float, float *, int, float *, int);
extern float mlp_sdot(int, float *, int, float *, int);
extern float mlp_snrm2(int, float *, int);


#endif /* !_MLPCLA_H */
