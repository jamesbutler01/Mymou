/*
 * Arduino Uno code to control 4 independent reward systems via Bluetooth
 * 
 * Receives commands from Bluetooth, then activates the various digital OUT pins of Arduino accordingly:
 * 
 * Bluetooth inputs:
 * "0" - Switch off all channels
 * "1" - Switch on channel 2
 * "2" - Switch off channel 2
 * "3" - Switch on channel 3
 * "4" - Switch off channel 3
 * "5" - Switch on channel 4
 * "6" - Switch off channel 4
 * "7" - Switch on channel 5
 * "8" - Switch off channel 5
 * 
 */

// Initialise variables
int ledPin = 13;
int channelTwo = 2;
int channelThree = 3;
int channelFour = 4;
int channelFive = 5;
int bufferInt = -1;


int pumpcommand(int pump_channel, int on_or_off) {

    if(on_or_off==0) {
      digitalWrite(pump_channel, LOW);
      digitalWrite(ledPin, LOW);
    } 
    else if(on_or_off==1) {
      digitalWrite(pump_channel, HIGH);
      digitalWrite(ledPin, HIGH);
    } 
}

void setup() {

  // Configure channels
  pinMode(channelTwo, OUTPUT);
  pinMode(channelThree, OUTPUT);
  pinMode(channelFour, OUTPUT);
  pinMode(channelFive, OUTPUT);
  pinMode(ledPin, OUTPUT);
  
  // Set all channels to off
  pumpcommand(channelTwo, 0);
  pumpcommand(channelThree, 0);
  pumpcommand(channelFour, 0);
  pumpcommand(channelFive, 0);
  
  // Configure bluetooth connection rate
  Serial.begin(9600); 

}


void loop() {
  // Check for input
  if(Serial.available() > 0){

    bufferInt = Serial.read();

    switch (bufferInt) {

      case 0: {
              pumpcommand(channelTwo, 0);
              pumpcommand(channelThree, 0);
              pumpcommand(channelFour, 0);
              pumpcommand(channelFive, 0);
              break;
             }
      case 1: pumpcommand(channelTwo, 1); break;
      case 2: pumpcommand(channelTwo,0); break;
      case 3: pumpcommand(channelThree,1); break;
      case 4: pumpcommand(channelThree,0); break;
      case 5: pumpcommand(channelFour,1); break;
      case 6: pumpcommand(channelFour,0); break;
      case 7: pumpcommand(channelFive,1); break;
      case 8: pumpcommand(channelFive,0); break;
    }
  
  }

} 
