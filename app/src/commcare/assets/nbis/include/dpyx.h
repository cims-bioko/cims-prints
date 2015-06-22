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

      FILE:    DPYX.H

      AUTHORS: Michael D. Garris
               Stan Janet
      DATE:    12/30/1990
      UPDATED: 05/23/2005 by MDG
      UPDATED: 04/25/2008 by Joseph C. Konczal - added display of SEG/ASEG data

***********************************************************************/
#ifndef _DPYX_H
#define _DPYX_H

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/keysym.h>
#include <X11/keysymdef.h>

#include <limits.h>
#include <display.h>
#include <dpydepth.h>
#include <event.h>
#include <dpy.h>

#define WIN_XY_INCR             25

#define BITMAP_UNIT_24          4 /* 4 bytes ==> 32 bits */

#define PT(x,y,w,h)             (((x)>=0)&&((x)<(w))&&((y)>=0)&&((y)<(h)))

#define ALL_BUTTONS  ((unsigned int)  (Button1Mask| \
                                Button2Mask| \
                                Button3Mask| \
                                Button4Mask| \
                                Button5Mask))


/* X-Window global references. */
extern Display *display;
extern char *display_name;
extern Window window, rw;
extern Visual *visual;
extern int screen;
extern Colormap def_cmap, cmap;
extern int cmap_size;
extern GC gc, boxgc, pointgc, seggc[3]; /* jck - added seggc */
extern unsigned long bp, wp;
extern unsigned int border_width;
extern int no_window_mgr;
extern int no_keyboard_input;

/************************************************************************/
/* dpyx.c */
extern void cleanup(void);
extern int xconnect(void);
extern int initwin(int wx, int wy, unsigned int ww,
                   unsigned int wh, unsigned int depth, unsigned long wp);
extern int set_gray_colormap(Display *, Colormap, unsigned int, unsigned long);
extern int gray_colormap(Colormap *, Display *, Visual **, unsigned int);

#endif  /* !_DPYX_H */
