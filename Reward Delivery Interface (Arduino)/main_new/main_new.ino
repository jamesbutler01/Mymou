#include <SoftwareSerial.h>
SoftwareSerial Piotr(10, 11); // RX, TX
 
// Initialise variables
int bufferInt = -1;
int channelNumber;
int numer, stan;
char wlacznik;
 
int pumpcommand(int numer, int stan)
{
  if(stan==0) {wlacznik = 73;} //wylacz pompe
  if(stan==1) {wlacznik = 72;} //wlacz pompe
  
  Piotr.write(48+numer);    // ktory kanal aktywowany
  Piotr.write(48+wlacznik); 
  Piotr.write(13);
}
 
void setup() {
// Configure bluetooth connection rate
  Serial.begin(9600);
  Piotr.begin(9600);
}
 
void loop() {
  // Check for input
  if(Serial.available() > 0){
    bufferInt = Serial.read();
 
   switch (bufferInt) {
    case 0: { 
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

