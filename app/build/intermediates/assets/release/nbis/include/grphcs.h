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


#ifndef _GRPHCS_H
#define _GRPHCS_H

#ifndef _XLIB_H
#include <X11/Xlib.h>
#endif

/* grphcs.c */
extern void grphcs_init(SLEEPS *, const int, RGAR_PRS *);
extern Window grphcs_startwindow(const int, const int, const int);
extern void grphcs_origras(unsigned char *, const int, const int);
extern void grphcs_segras(unsigned char **, const int, const int);
extern void grphcs_enhnc_outsquare(unsigned char [WS][WS], const int,
                 const int, const int, const int y);
extern void grphcs_enhnc_sleep(void);
extern void grphcs_foundconup_sleep(void);
extern void grphcs_noconup_sleep(void);
extern void grphcs_bars(float **, float **, const int, const int, const int);
extern void grphcs_xy_to_dmt(float **, float **, float **, float **,
                 const int, const int, float *);
extern XImage *grphcs_dmt_to_bars(float **, float **, const int, const int,
                 const float, unsigned char *, const int, const int);
extern void grphcs_core_medcore(const int, const int, const int, const int h);
extern void grphcs_normacs(float *, const int, char *);
extern void letter_adjust_xy(const char, int *, int *);
extern void grphcs_sgmntwork_init(const int, const int);
extern void grphcs_sgmntwork_fg(unsigned char *, const int);
extern void grphcs_sgmntwork_edge(int *, int *, const int, const int);
extern void grphcs_sgmntwork_lines(float [3], float [3]);
extern void grphcs_sgmntwork_box(unsigned char *, const float, const float,
                 const int, const int, const int, const int, const int);
extern void grphcs_sgmntwork_finish(void);
extern void grphcs_pseudo_cfgyow(unsigned char **, const int, const int,
                 const int, const int h);
extern void grphcs_pseudo_cfgyow_reput(const int, const int);
extern void grphcs_pseudo_pseudoridge(float *, float *, const int, const int,
                 const int, const int);
extern void grphcs_titlepage(void);
extern void grphcs_featvec(float *, const int);
extern void grphcs_lastdisp(const int, const int, const float, char *,
                 const int);
extern void ImageBit8ToBit24Unit32(unsigned char **, unsigned char *,
                 const int, const int);
extern XImage *xcreateimage(unsigned char *, const int, const int, const int,
                 const int, const int, const int, const int);

#endif /* !_GRPHCS_H */
