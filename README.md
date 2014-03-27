Equalizer
=========

This is the driver for a custom EQ effect I made for an AOSP build. 
<br>
I created a new effect in the libeffect.so in the audioeffects bundle.  I altered the EQ effect to allow for more than 5 bands and I inserted a custom DSP in the EQ.
<br><br>
I included the libeffect code in this project.  Just copy and paste it into your AOSP project and build it to be able to use this driver.  
<br><br>
This app will not work unless you have a 6 band EQ which is not standard for AOSP, you will need to alter your libeffect.so to allow the EQ effect to have 6 bands.  
<br><br>
Number of bands lives here: 
<br>
https://android.googlesource.com/platform/frameworks/av/+/9803acb6b2c1b9c01444e0c8c0124adbe9a7157a/media/libeffects/lvm/lib/Bundle/lib/LVM.h
<br><br>
Number of bands also lives here, and you will need to make the presets support the number of bands you change the numbands to:
<br>
https://android.googlesource.com/platform/frameworks/av/+/9803acb6b2c1b9c01444e0c8c0124adbe9a7157a/media/libeffects/lvm/wrapper/Bundle/EffectBundle.h
<br><br>
Also you will need to turn off the gain correction here:
<br>
https://android.googlesource.com/platform/frameworks/av/+/9803acb6b2c1b9c01444e0c8c0124adbe9a7157a/media/libeffects/lvm/wrapper/Bundle/EffectBundle.cpp
<br><br>
Lastly, you might need to go into the AudioFlinger and alter the global effects on audio session id 0. 
