
This program utilizes an Arduino as an analog-to-digital converter to
sample an analog signal between 0-5 volts.  The software supports reading
from 1-6 analog inputs simultaneously with variable frequencies, limited
by the serial data transfer speed.  In practice about 1000 Hz frequencies
can be obtained.

Consists of two parts:  Arduino software for the A/D conversion and
graphical Java software for data acquisition and plotting.



This project is in a rather raw and unpolished state.  If currently does not
have scripts to build the application, but rather depends on being run
from Eclipse.


Arduino program:
----------------

Upload the program in arduino/MultiInput/MultiInput.pde to the Arduino.


Java software:
--------------

You need to have RXTX library installed.  On Debian-based systems use
    sudo apt-get install librxtx-java

To start the software, run the class gui.DataAnalyzer from Eclipse


Usage:
------

Click "Configure" to set up communication options.

Click "Start listening" to start data reading data from the Arduino.

Select a file using "Browse" and check "Save data to file" to save data to a file in CSV format.

Add text to the text entry box to add a comment to the saved data file.

Click "Plot" to display a real-time plot of the data.


Calibration:
------------

The boxes on the left side define linear scaling for the values:
     [input value]  [output value]
Clicking the "Cal" button sets the corresponding input value to the current input value.

Calibration using a known weight:
 1. Remove any weight on scale
 2. Set output value on first line to "0"
 3. Click "Cal" on first line
 4. Place known weight on scale
 5. Set output value on second line to known weight
 6. Click "Cal" on second line

You can use the buttons for preset calibration values:

5V    Scale input values to 0...5V (analog input to Arduino)
1:1   Set 1:1 scaling, output = input
Tare  Subtract current output value from all output values


