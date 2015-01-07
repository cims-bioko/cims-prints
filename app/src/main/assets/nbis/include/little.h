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
licensing requirements; and it is considered public domain.Ê Therefore,
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

 
#ifndef _LITTLE_H
#define _LITTLE_H

/* Declarations of those functions in src/lib/utils/little.c with
non-int return values (including, void return value).  Stdio.h is
needed for FILE. */

#define INSTALL_DIR "/home/sarwar/android-NBIS/NBIS/Rel_3.3.1"
#define INSTALL_DATA_DIR "/home/sarwar/Desktop/DroidNBIS/nbis"
#define INSTALL_NBIS_DIR "/home/sarwar/android-NBIS/NBIS"

extern int creat_ch(char *);
extern void dptr2ptr_uchar(unsigned char **, unsigned char **, const int,
                 const int);
extern void erode(unsigned char *, const int, const int);
extern int exists(char *);
extern FILE *fopen_ch(char *, char *);
extern FILE * fopen_noclobber(char *filename);
extern char *get_datadir(void);
extern int isverbose(void);
extern char *lastcomp(char *);
extern int linecount(char *);
extern int linreg(int *, int *, const int, float *, float *);
extern char *malloc_ch(const int);
extern int open_read_ch(char *);
extern void rcfill(unsigned char *, const int, const int);
extern void rsblobs(unsigned char *, const int, const int);
extern void setverbose(const int verbose);
extern void sleepity(const int);
extern void summary(const int, const int, int *, FILE *, const int, char *);
extern char *tilde_filename(char [], const int);
extern void usage_func(char *, char *);
extern void Usage_func(const int, char *, char *);
extern void write_ihdr_std(unsigned char *, const int, const int, const int,
                 char *);

#endif /* !_LITTLE_H */
