Modular Audio/Visual Synthesis Guide
====================================


![Interlace](/assets/modular.jpg)

This guide is intended to provide an overview of VCV Rack / Chromatik modular audio and visual synthesis. 

For VCV Rack, you should install the trowaSoft cvOSCcv module.

You should verify that you have OSC input enabled in Chromatik.

![OSC](/assets/chromatik_osc_on.jpg)


You should set the cvOSCcv module to send OSC messages to the correct IP address and port.  The default for Chromatik would be port 3030 and IP 127.0.0.1.  
For this example, the CV signal is routed to output 1 which has been configured to /lx/modulation/H1Knobs/macro3.  H1Knobs is the customized name of
Macro Knobs modulator in Chromatik.

![cvOSCcv](/assets/cvOSCcv_config.jpg)

![macro knobs](/assets/chromatik_macro_knobs.jpg)

Make sure that you have 'AutoCon' enabled for cvOSCcv in the VCV Rack patch so
that it will auto-connect to the Chromatik OSC server.  Also, if you need multiple
cvOSCcv modules, you should pick a unique InPort setting for each instance.
Also, be sure to enable 'Convert Values' for each output on the cvOSCcv module.  
You can adjust the range of the input and output values as needed.

You should use the 'Scope' module (or something similar) in VCV Rack to visualize the CV signal.

![vcv scope](/assets/vcvrack_scope.jpg)
