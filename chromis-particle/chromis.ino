// Tests the infrared receiver:

// Hardware setup:
//
// This script assumes an IR Decoder connected to D0. I bought part number CHQ-1838
// on Amazon. For IR Output, I connected a NPN transistor to A5, and two
// IR LEDs in series with the C-E side of the trasistor with 5V (and 75 ohm resistor).

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
    Particle.function("emit", emit);
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
// Emits an IR code corresponding to one that was dumped earlier
// i.e.
// Power Off: "NEC,20DFA35C,32"

int emit(String command) {
	if (command == "") {
        return 0;
    }

    char inputStr[64];
    command.toCharArray(inputStr, 64);
    char *scheme = strtok(inputStr, ",");
    char *p = strtok(NULL, ",");
    int data = strtol(p, NULL, 16);
    p = strtok(NULL, ",");
    int bits = atoi(p);

    if (strcmp(scheme, "NEC")==0) {
        irsend.sendNEC(data, bits);
        irsend.sendNEC(REPEAT, bits);
        irsend.sendNEC(REPEAT, bits);
    }
    else if (strcmp(scheme, "SONY")==0) {
        irsend.sendSony(data, bits);
        irsend.sendSony(REPEAT, bits);
        irsend.sendSony(REPEAT, bits);
    }
    else if (strcmp(scheme, "RC5")==0) {
        irsend.sendRC5(data, bits);
        irsend.sendRC5(REPEAT, bits);
        irsend.sendRC5(REPEAT, bits);
    }
    else if (strcmp(scheme, "RC6")==0) {
        irsend.sendRC6(data, bits);
        irsend.sendRC6(REPEAT, bits);
        irsend.sendRC6(REPEAT, bits);
    }
    else if (strcmp(scheme, "DISH")==0) {
        irsend.sendDISH(data, bits);
        irsend.sendDISH(REPEAT, bits);
        irsend.sendDISH(REPEAT, bits);
    }
    else if (strcmp(scheme, "SHARP")==0) {
        irsend.sendSharp(data, bits);
        irsend.sendSharp(REPEAT, bits);
        irsend.sendSharp(REPEAT, bits);
    }
    else if (strcmp(scheme, "PANASONIC")==0) {
        irsend.sendPanasonic(data, bits);
        irsend.sendPanasonic(REPEAT, bits);
        irsend.sendPanasonic(REPEAT, bits);
    }
    else if (strcmp(scheme, "JVC")==0) {
        irsend.sendJVC(data, bits, 0);
        irsend.sendJVC(data, bits, 1);
        irsend.sendJVC(data, bits, 1);
    }
    // Neither Sanyo nor Mitsubishi is implemented yet

    irrecv.enableIRIn();
    return 1;
}

/////////////////////////////////////////////////////////////////////////////////////
// Helper methods
/////////////////////////////////////////////////////////////////////////////////////
void dumpSerial() {
    char out[32];

    switch (results.decode_type) {
        case NEC:
            sprintf(out, "NEC,%X,%d", results.value, results.bits);
            break;
        case SONY:
            sprintf(out, "SONY,%X,%d", results.value, results.bits);
            break;
        case RC5:
            sprintf(out, "RC5,%X,%d", results.value, results.bits);
            break;
        case RC6:
            sprintf(out, "RC6,%X,%d", results.value, results.bits);
            break;
        case DISH:
            sprintf(out, "DISH,%X,%d", results.value, results.bits);
            break;
        case SHARP:
            sprintf(out, "SHARP,%X,%d", results.value, results.bits);
            break;
        case PANASONIC:
            sprintf(out, "PANASONIC,%X,%d", results.value, results.bits);
            break;
        case JVC:
            sprintf(out, "JVC,%X,%d", results.value, results.bits);
            break;
        case SANYO:
            sprintf(out, "SANYO,%X,%d", results.value, results.bits);
            break;
        case MITSUBISHI:
            sprintf(out, "MITSUBISHI,%X,%d", results.value, results.bits);
            break;
        case UNKNOWN:
            sprintf(out, "UNKNOWN,%X,%d", results.value, results.bits);
            break;
        default:
            sprintf(out, "???,%X,%d", results.value, results.bits);
            break;
    }

    Serial.print(out);
    Serial.println();

    if (results.value != REPEAT) {
        Particle.publish("ir-detected", out);
    }
}
