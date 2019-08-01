#include <SoftwareSerial.h>
SoftwareSerial Piotr(10, 11); // RX, TX
 
// Initialise variables
int bufferInt = -1;
int switch_on = 72;
int switch_off = 73;
int channelNumber;
int chan_num, stan;
int end_message = 13;
int message_offset = 48;
char char_out;
 
int pumpcommand(int chan_num, int stan)
{
  if(stan==0) {char_out = switch_off;} 
  if(stan==1) {char_out = switch_on;}
  
  Piotr.write(message_offset+chan_num); 
  Piotr.write(message_offset+char_out); 
  Piotr.write(end_message);
}
 
void setup() {
// Configure bluetooth connection rate
  Serial.begin(9600);
  Piotr.begin(9600);
}
 
void loop() {
  // Check for input
  if(Serial.available() > 0){

    // Read input
    bufferInt = Serial.read();

  // Respond according to the input
   switch (bufferInt) {
    case 0: { 
      // Switch off all pumps
              pumpcommand(1,0); 
              pumpcommand(2,0); 
              pumpcommand(3,0); 
              pumpcommand(4,0); 
              break;
              }
    case 1: pumpcommand(1,1); break;
    case 2: pumpcommand(1,0); break;
    case 3: pumpcommand(2,1); break;
    case 4: pumpcommand(2,0); break;
    case 5: pumpcommand(3,1); break;
    case 6: pumpcommand(3,0); break;
    case 7: pumpcommand(4,1); break;
    case 8: pumpcommand(4,0); break;
   }
  }
}

