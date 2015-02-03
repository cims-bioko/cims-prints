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

      FILE:    JPEG2K.H

      AUTHORS: Kenneth Ko
      DATE:    12/15/2007

***********************************************************************/
#ifndef _JPEG2K_H
#define _JPEG2K_H

#include <jpegl.h>

#ifdef __NBIS_JASPER__
	#include <jasper/jasper.h>
#endif

#ifdef __NBIS_OPENJPEG__
	#include <openjpeg/openjpeg.h>
#endif

/*********************************************************************/

#ifdef __NBIS_JASPER__
	int jpeg2k_decode_mem(IMG_DAT **, int *, unsigned char *, const int);
	int img_dat_generate(IMG_DAT **, jas_image_t *);
#endif

#ifdef __NBIS_OPENJPEG__
	int openjpeg2k_decode_mem(IMG_DAT **, int *, unsigned char *, const int);
/*
	int image_to_raw(opj_image_t *, signed char *, unsigned char *);
*/
        int image_to_raw(opj_image_t *, unsigned char *);
        int img_dat_generate_openjpeg(IMG_DAT **oimg_dat, opj_image_t *image, unsigned char *);
	int get_file_format(char *);
#endif

#endif /* !_JPEG2K_H */ 
