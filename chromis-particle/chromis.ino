// Tests the infrared receiver:
// CHQ-1838

// Connect to serial port using "screen":
// screen -L /dev/tty.usbmodem???

// Found commands:
// For hdmi switch:
// 1: NEC,FF10EF,32
// 2: NEC,FF50AF,32
// 3: NEC,FF30CF,32
// 4: NEC,FF708F,32

// For LG TV:
// Power Toggle: NEC,20DF10EF,32
// Power Off: NEC,20DFA35C,32
// Power On: NEC,20DF23DC,32
// Vol+: NEC,20DF40BF,32
// Vol-: NEC,20DFC03F,32
// Mute: NEC,20DF906F,32
// HDMI1: NEC,20DF738C,32

#include <IRremote.h>

const int PIN_OUTPUT = A5;
const int PIN_INPUT = D0;

IRrecv irrecv(PIN_INPUT);
IRsend irsend;

decode_results results;

/////////////////////////////////////////////////////////////////////////////////////
// Main Arduino Loop
/////////////////////////////////////////////////////////////////////////////////////
void setup() {
    Serial.begin(9600);
    irrecv.blink13(TRUE);
    irrecv.enableIRIn(); // Start the receiver

    pinMode(PIN_OUTPUT, OUTPUT);
    Particle.function("tvOff", tvOff);
    Particle.function("tvOn", tvOn);
    Particle.function("hdmi1", hdmi1);
    Particle.function("hdmi2", hdmi2);
    Particle.function("hdmi3", hdmi3);
    Particle.function("hdmi4", hdmi4);
}

void loop() {
    if (irrecv.decode(&results)) {
        dumpSerial();
        irrecv.resume(); // Receive the next value
    }
}

/////////////////////////////////////////////////////////////////////////////////////
// Particle functions
/////////////////////////////////////////////////////////////////////////////////////
int tvOff(String extra) {
    irsend.sendNEC(0x20DFA35C, 32);
    irsend.sendNEC(REPEAT, 32);
    irsend.sendNEC(REPEAT, 32);
    irrecv.enableIRIn();
    return 1;
}

int tvOn(String extra) {
    irsend.sendNEC(0x20DF23DC, 32);
    irsend.sendNEC(REPEAT, 32);
    irsend.sendNEC(REPEAT, 32);
    irrecv.enableIRIn();
    return 1;
}

int hdmi1(String extra) {
    irsend.sendNEC(0xFF10EF, 32);
    irsend.sendNEC(REPEAT, 32);
    irsend.sendNEC(REPEAT, 32);
    irrecv.enableIRIn();
    return 1;
}

int hdmi2(String extra) {
    irsend.sendNEC(0xFF50AF, 32);
    irsend.sendNEC(REPEAT, 32);
    irsend.sendNEC(REPEAT, 32);
    irrecv.enableIRIn();
    return 1;
}

int hdmi3(String extra) {
    irsend.sendNEC(0xFF30CF, 32);
    irsend.sendNEC(REPEAT, 32);
    irsend.sendNEC(REPEAT, 32);
    irrecv.enableIRIn();
    return 1;
}

int hdmi4(String extra) {
    irsend.sendNEC(0xFF708F, 32);
    irsend.sendNEC(REPEAT, 32);
    irsend.sendNEC(REPEAT, 32);
    irrecv.enableIRIn();
    return 1;
}

/////////////////////////////////////////////////////////////////////////////////////
// Helper methods
/////////////////////////////////////////////////////////////////////////////////////
void dumpSerial() {
    switch (results.decode_type) {
        case NEC:
            Serial.print("NEC,");
            break;
        case SONY:
            Serial.print("SONY,");
            break;
        case RC5:
            Serial.print("RC5,");
            break;
        case RC6:
            Serial.print("RC6,");
            break;
        case DISH:
            Serial.print("DISH,");
            break;
        case SHARP:
            Serial.print("SHARP,");
            break;
        case PANASONIC:
            Serial.print("PANASONIC,");
            break;
        case JVC:
            Serial.print("JVC,");
            break;
        case SANYO:
            Serial.print("SANYO,");
            break;
        case MITSUBISHI:
            Serial.print("MITSUBISHI,");
            break;
        case UNKNOWN:
            Serial.print("UNKNOWN,");
            break;
        default:
            Serial.print("???,");
            break;
    }
    Serial.print(results.value, HEX);
    Serial.print(",");
    Serial.print(results.bits, DEC);
    Serial.println();
}
