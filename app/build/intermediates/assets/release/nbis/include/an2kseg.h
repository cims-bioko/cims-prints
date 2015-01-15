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


/***********************************************************************
      PACKAGE: ANSI/NIST 2000 Standard Reference Implementation

      FILE:    AN2KSEG.H

      AUTHORS: Margaret Lepley
      DATE:    03/14/2008


***********************************************************************/
#ifndef _AN2KSEG_H
#define _AN2KSEG_H

#include <an2k.h>

/* Data contained in Finger Segment Positions */
typedef struct {
   int finger; /* Finger index for this segment */
   int left;   /* Left boundary pixel inclusive. [First pixel is 0] */
   int right;  /* Right boundary pixel exclusive. */
   int top;    /* Top boundary pixel inclusive. */
   int bottom; /* Bottom boundary pixel exclusive. */
  /* Note: range endings (right/bottom) are always one more than */
  /* the last pixel index actually included */
} SEG;

#define SEG_ID    21

/***********************************************************************/
/* SEG.C : ROUTINES */
extern int lookup_type14_segments(SEG **, int *, RECORD *);

#endif /* !_AN2KSEG_H */
