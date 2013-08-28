//
//  AphexAuralExciterBigBottom.h
//  Aphex Aural Exciter Big Bottom
//
//  Created by David Simpao on 4/30/11.
//  Copyright 2011 Aphex. All rights reserved.
//

#ifndef __AphexAuralExciterBigBottom
#define __AphexAuralExciterBigBottom


// Channel definitions:  Use these to specify the channel in the function calls.

#define CHANNEL1 0
#define CHANNEL2 1



// Initialization function:  Call this once per channel to initialize it.  Parameter values should go from 0.0f to 1.0f.

void  aphexAuralExciterBigBottomInit (int channel,float padFixed,
                                      int autoNormalization,float tuneAE, float harmonics,
                                      float mixAE, float drive, float tuneBB, float mixBB);


// Set functions:  Call these to set a parameter on a channel.  Float parameter values should go from 0.0f to 1.0f.

void  aphexAuralExciterBigBottomSetPadFixed          (int channel, float padFixed);
void  aphexAuralExciterBigBottomSetAutoNormalization (int channel,   int autoNormalization);

void  aphexAuralExciterSetTune                       (int channel, float tuneAE);
void  aphexAuralExciterSetHarmonics                  (int channel, float harmonics);
void  aphexAuralExciterSetMix                        (int channel, float mixAE);

void  aphexBigBottomSetTune                          (int channel, float tuneBB);
void  aphexBigBottomSetDrive                         (int channel, float drive);
void  aphexBigBottomSetMix                           (int channel, float mixBB);


// Process function:  Call this once per sample per channel.  Input sample should be from -1.0f to +1.0f.  Returns output sample.

float aphexAuralExciterBigBottomProcess (int channel, float input);


// Get function:  Call this periodically (say, 60 times a second) to get drive LED brightness.  Returns float between 0.0f and +1.0f.

float aphexBigBottomGetDrive            (int channel);


// Library version string

extern const char* aphexAuralExciterBigBottomLibraryVersion;

#endif // __AphexAuralExciterBigBottom
