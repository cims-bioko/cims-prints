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

#ifndef _DATAFILE_H
#define _DATAFILE_H

/* Stuff concerning PCASYS standard-format data files. */

/* File type code characters. */
#define PCASYS_MATRIX_FILE     'm'
#define PCASYS_COVARIANCE_FILE 'v'
#define PCASYS_CLASSES_FILE    'c'

/* Ascii vs. binary code characters. */
#define PCASYS_ASCII_FILE  'a'
#define PCASYS_BINARY_FILE 'b'

/* Dimension of the "description" buffers used by the asc2bin, bin2asc,
and datainfo commands. */
#define DESC_DIM 500


/* io_c.c */
extern void classes_write_ind(char *, char *, const int, int,
               unsigned char *, int, char **);
extern void classes_read_n(char *, int *);
extern void classes_read_ind(char *, char **, int *, unsigned char **,
               int *, char ***);
extern void classes_read_subvector_ind(char *, const int, const int,
               char **, unsigned char **, int *, char ***);
extern void number_classes(unsigned char *, const int, int *);
extern void classes_read_vec(char *, char **, int *, float **, int *, char ***);
extern void classes_read_pindex(char *, int *);
extern void classes_read_ncls(char *, int *);


/* io_m.c */
extern void matrix_writerow_init(char *, char *, const int, int, int, FILE **);
extern void matrix_writerow(FILE *, const int, const int, float *);
extern int matrix_write(char *, char *, const int, int, int, float *);
extern void matrix_readrow_init(char *, char **, int *, int *, int *, FILE **);
extern void matrix_readrow(FILE *, const int, int, float *);
extern void matrix_read_dims(char *, int *, int *);
extern void matrix_read(char *, char **, int *, int *, float **);
extern void matrix_read_submatrix(char *, const int, const int, const int,
               const int, char **, float **);

/* io_v.c */
extern void covariance_write(char *, char *, const int, int, int, float *);
extern void covariance_read_order_nvecs(char *, int *, int *);
extern void covariance_read(char *, char **, int *, int *, float **);
extern void covariance_read_old(char *, char **, int *, int *, float **);


#endif /* !_DATAFILE_H */
