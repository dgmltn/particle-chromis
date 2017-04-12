# Chromis

A universal IR remote control, named after the Flashing Chromis.

![alt tag](chromis.jpg)

### Hardware

This project assumes a Particle Photon or Spark Core with an IR Decoder connected to pin ```D0```. I bought part number CHQ-1838 on Amazon. The IRremote library is hard-coded to output a remote control signal on pin ```A5```. For IR Output, I connected a NPN transistor to ```A5```, and two IR LEDs (in series) with the C-E side of the trasistor with 5V (and 75Ω resistor). I used the Photon's 5V power supply to power everything.

### API

Chromis has a very simple interface. When it detects a remote control, it publishes
a Particle event:

```ir-detected``` with data representing the button that was detected, 
for example: ```NEC,FF48B7,32```. It also outputs this same data string as serial output.

To re-emit this same remote control signal, call the Particle function ```emit```, 
with the same button data:

```emit``` ⇢ ```NEC,FF48B7,32```

That's it! Happy remoting!

### Acknowledgements

* [Particle](http://particle.io) for their awesome and versatile (and my favorite) IoT device
* Ken Shirriff for the bulk of the [IRremote](http://www.righto.com/2009/08/multi-protocol-infrared-remote-library.html) Arduino code
* Paul Kourany, Dianel Gilbert, for the [SparkIntervalTimer](https://github.com/pkourany/SparkIntervalTimer) Arduino code
