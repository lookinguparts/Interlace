# Interlace Sensors

When building with PlatformIO, first build and upload the filesystem image.

There is a test script in the "tools" folder, `tools/talktome.py` that will set the OSC target to the current computer.

# System Overview

The core sensor is a LIS3MDL 3-axis, high performance magnetometer.
https://www.st.com/en/mems-and-sensors/lis3mdl.html

The update rate of the LIS3MDL can go up to 1000Hz:
<img width="646" alt="image" src="https://github.com/user-attachments/assets/1caa1813-95ca-4716-b7b0-febdf918fad8">

We are running the magnetometer at high update rates, then applying a moving average filter at a cutoff frequency determined by the physical characteristics of the system. We want the moving average filter to be as slow as possible to improve steady state stability, while being fast enough to seem near instantly responsive to movement. This rate will be determined by the final characteristics of the system (TBD).

The output update rate over OSC can be up to and beyond 1000 Hz if desired.

The angular resolution of the system can be as precise as desired, up to the precision of a float.

Based upon initial testing, the sensor output in steady state (not moving) has a noise of about +-1 degree, and can be as high as +-3 degrees, this noise fluctuation is slow and smooth though due to the moving average filter applied to the sensor output.
