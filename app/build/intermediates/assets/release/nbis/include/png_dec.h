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

      FILE:    PNG_DEC.H

      AUTHORS: Kenneth Ko
      DATE:    01/14/2008
      UPDATE:  10/07/2008 by Joseph C. Konczal - added struct and prototype

***********************************************************************/
#ifndef _PNG_DEC_H
#define _PNG_DEC_H

#include <jpegl.h>
#include <png.h>

/*********************************************************************/

struct png_mem_io_struct {
   unsigned char *cur;		/* current location in the input buffer */
   unsigned char *end;		/* points to the byte after the last one,
				   i.e., start location + length */
};

void png_mem_read_data(png_structp, png_bytep, png_size_t);
int png_decode_mem(IMG_DAT **, int *, unsigned char *, const int);
int read_png_file(char *, IMG_DAT **);
int get_raw_image(png_bytep *, png_info *,  IMG_DAT **);

#endif /* !_PNG_DEC_H */ 
