// Tests the infrared receiver:

// Hardware setup:
//
// This script assumes an IR Decoder connected to D0. I bought part number CHQ-1838
// on Amazon. For IR Output, I connected a NPN transistor to A5, and two
// IR LEDs in series with the C-E side of the trasistor with 5V (and 75 ohm resistor).

// Show serial debugging:
// particle serial monitor

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
#include <MQTT.h>

const int PIN_OUTPUT = A5;
const int PIN_INPUT = D2;

////////////////////////////////////////////////////////////////////////////////
// MQTT
////////////////////////////////////////////////////////////////////////////////

char myIpString[24];
byte server[] = { 10, 5, 23, 6 };

void mqttCallback(char* topic, byte* payload, unsigned int length) {
    char p[length + 1];
    memcpy(p, payload, length);
    p[length] = NULL;
    String message(p);

	emit(message);
}

MQTT mqttClient(server, 1883, mqttCallback);
int mqtt_status = 0;

bool setupMqtt() {
    Particle.variable("mqttstatus", mqtt_status);

    // connect to the server
    if (mqttClient.connect("chromis")) {
        // subscribe
        mqttClient.subscribe("devices/particle/chromis/transmit");
        return true;
    }
    return false;
}

void loopMqtt() {
    mqtt_status = mqttClient.isConnected() ? 1 : 0;

    if (!mqttClient.loop()) {
        // Not connected, try to reconnect
        if (!setupMqtt()) {
            // Reconnect failed, wait a few seconds
            delay(5000);
        }
    }
}

bool mqttPublish(String button) {
    return mqttClient.publish("devices/particle/chromis/ir", button);
}

////////////////////////////////////////////////////////////////////////////////
// IRReceiver
////////////////////////////////////////////////////////////////////////////////

IRrecv irrecv(PIN_INPUT);
IRsend irsend;

decode_results results;

void setupIr() {
    Serial.begin(9600);
    irrecv.blink13(TRUE);
    irrecv.enableIRIn(); // Start the receiver
}

void loopIr() {
    if (irrecv.decode(&results)) {
        dumpSerial();
        irrecv.resume(); // Receive the next value
    }
}

////////////////////////////////////////////////////////////////////////////////
// MAIN PROGRAM
////////////////////////////////////////////////////////////////////////////////

void setup() {
    setupIr();

    pinMode(PIN_OUTPUT, OUTPUT);

    Particle.function("emit", emit);
    Particle.function("test", test);

	setupMqtt();
}

void loop() {
    loopIr();
	loopMqtt();
}

////////////////////////////////////////////////////////////////////////////////
// Particle functions
////////////////////////////////////////////////////////////////////////////////
// Emits an IR code corresponding to one that was dumped earlier
// i.e.
// Power Off: "NEC,20DFA35C,32"

int test(String command) {
    emit("NEC,FFFFFFFF,32");
    return 1;
}

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

////////////////////////////////////////////////////////////////////////////////
// Helper methods
////////////////////////////////////////////////////////////////////////////////
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
        mqttPublish(out);
    }
}
