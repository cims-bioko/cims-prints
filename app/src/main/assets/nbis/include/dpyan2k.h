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
      PACKAGE: ANSI/NIST 2007 Standard Reference Implementation

      FILE:    DPYAN2K.H

      AUTHORS: Michael D. Garris
               Stan Janet
      DATE:    03/07/2001
      UPDATED: 05/10/2005 by MDG
      UPDATED: 01/31/2008 by Kenneth Ko
      UPDATED: 02/27/2008 by Joseph C. Konczal
      UPDATED: 09/04/2008 by Kenneth Ko

***********************************************************************/
#ifndef _DYPYAN2K_H
#define _DYPYAN2K_H

#include <an2k.h>
#include <dpyimage.h>

/*********************************************************************/
/* dpyan2k.c */
extern int dpyan2k(const char *, const REC_SEL *const);
extern int dpyan2k_record(const int, const ANSI_NIST *);
extern int dpyan2k_binary_record(const int, const ANSI_NIST *);
extern int dpyan2k_tagged_record(const int, const ANSI_NIST *);

#endif /* !_DPYAN2K_H */
