#include <SoftwareSerial.h>


#include <CurieBLE.h>


/*
  DigitalReadSerial

  Reads a digital input on pin 2, prints the result to the Serial Monitor

  This example code is in the public domain.

  http://www.arduino.cc/en/Tutorial/DigitalReadSerial
*/

// digital pin 2 has a pushbutton attached to it. Give it a name:
int leftBrakeIn = 2;
int rightBrakeIn = 0;
int ThrottleIn = A2;
int Vout = 9;

BLEService EBIKEService("19B10010-E8F2-537E-4F6C-D104768A1214"); // create service

// create switch characteristic and allow remote device to read and write
BLECharCharacteristic RXCharacteristic("19B10011-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
// create button characteristic and allow remote device to get notifications
BLECharCharacteristic TXCharacteristic("19B10012-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify); // allows remote device to get notifications

// the setup routine runs once when you press reset:
void setup() {
  // initialize serial communication at 9600 bits per second:
  Serial.begin(9600);
  pinMode(leftBrakeIn, INPUT_PULLUP);
  pinMode(rightBrakeIn, INPUT_PULLUP);
  pinMode(Vout, OUTPUT);
  
  // begin initialization
  BLE.begin();

  // set the local name peripheral advertises
  BLE.setLocalName("EBIKE");
  // set the UUID for the service this peripheral advertises:
  BLE.setAdvertisedService(EBIKEService);

// add the characteristics to the service
  EBIKEService.addCharacteristic(RXCharacteristic);
  EBIKEService.addCharacteristic(TXCharacteristic);

  // add the service
  BLE.addService(EBIKEService);

  RXCharacteristic.setValue(0);
  TXCharacteristic.setValue(0);

  // start advertising
  BLE.advertise();

  Serial.println("Bluetooth device active, waiting for connections...");

}

// the loop routine runs over and over again forever:
void loop() {
  // poll for BLE events
  BLE.poll();
  
  // read the input pin:
  int leftBrakeValue = digitalRead(leftBrakeIn);
  int rightBrakeValue = digitalRead(rightBrakeIn);
  int ThrottleValue = analogRead(ThrottleIn);
  float Throttlevoltage = ThrottleValue * (5.0/1023.0);
    // print out the value you read:
  Serial.println(Throttlevoltage);

  int TXValue = leftBrakeValue<<11 | rightBrakeValue<<10 | ThrottleValue;

   boolean TXChanged = (TXCharacteristic.value() != TXValue);
 
  if (TXChanged) {
    //TX state changed, update characteristics
    RXCharacteristic.setValue(TXValue);

  }

  if (RXCharacteristic.written() ) {
    int RXValue = RXCharacteristic.value();
    float VoutValue = RXValue * (5.0/1023.0);
    analogWrite(Vout, VoutValue);
  }
  delay(2);        // delay in between reads for stability
}
