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

      FILE:    DPYIMAGE.H

      AUTHORS: Michael D. Garris
               Stan Janet
      DATE:    03/07/2001
      UPDATED: 04/23/2008 by Joseph C. Konczal - added display of SEG/ASEG data
      UPDATED: 09/10/2008 by Joseph C. Konczal

***********************************************************************/
#ifndef _DPYIMAGE_H
#define _DPYIMAGE_H

#include <dpyx.h>

/* X-Window global references. */
extern unsigned int dw, dh;
extern int window_up;
extern int got_click;
extern unsigned int depth;
extern unsigned int ww, wh, iw, ih;
extern int absx, absy, relx, rely;
extern int x_1, y_1;

/* X-Window Contols & command line globals. */
extern char *program;
extern char *filename;
extern int accelerator;
extern unsigned int init_ww, init_wh;
extern int nicevalue;
extern int pointwidth;
extern char *title;
extern int wx, wy;
extern int verbose;
extern int debug;
/* Deactivated from command line: defaults used only. */
extern int automatic;
extern unsigned int sleeptime;

extern int nist_flag;
extern int iafis_flag;

/************************************************************************/
/* dpyimage.c */
extern int dpyimagepts(char *, unsigned char *, unsigned int,
                 unsigned int, unsigned int, unsigned int, int, int *,
		 int *, int *, int, const SEGMENTS *const);
extern int ImageBit8ToBit24Unit32(char **, char *, int, int);
extern void XMGetSubImageDataDepth24(char *, int, int, int, int,
                 char *, int, int);
extern int event_handler(XImage *, unsigned char *,
                 int *, int *, int *, int, const SEGMENTS *const);
extern void refresh_window(XImage *, int *, int *, int,
                 const SEGMENTS *const);
extern int drag_image(XImage *, unsigned char *,
                 int, int, int *, int *, int, const SEGMENTS *const);
extern int move_image(XImage *, unsigned char *,
                 int, int, int *, int *, int, const SEGMENTS *const);
extern int button_release(XEvent *, XImage *,
                 unsigned char *, int *, int *, int, 
                 const SEGMENTS *const);
extern void button_press(XEvent *);

#endif
