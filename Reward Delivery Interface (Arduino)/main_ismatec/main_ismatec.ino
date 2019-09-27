/*
 * Arduino Uno code to control Ismatec Reglo peristaltic pump via Bluetooth
 * 
 * Receives commands from Bluetooth, then sends ASCII characters to pump via RS232 cable 
 * 
 * Bluetooth inputs:
 * "0" - Switch off all channels
 * "1" - Switch on channel 1
 * "2" - Switch off channel 1
 * "3" - Switch on channel 2
 * "4" - Switch off channel 2
 * "5" - Switch on channel 3
 * "6" - Switch off channel 3
 * "7" - Switch on channel 4
 * "8" - Switch off channel 4
 * 
 * Pump manual: http://www.ismatec.com/download/documents/4189_Ismatec_Reglo_SS.pdf
 * 
 */
 
#include <SoftwareSerial.h>
SoftwareSerial BluetoothSerial(10, 11); // RX, TX

// Initialise variables
int led_pin = 13;
int bufferInt = -1;
int channelNumber;
int pump_channel, on_or_off;
char command;

int pumpcommand(int pump_channel, int on_or_off) {
  
  // Configure pump to appropriate channel
  BluetoothSerial.write(49); // Pump ID 1
  BluetoothSerial.write(126);   // tilde prefix
  BluetoothSerial.write(48+pump_channel);    // Channel to operate
  end_of_message();
  
  // Decide what action to send
  if (on_or_off==0) {
    
    command = 73;
    digitalWrite(led_pin, LOW);
  
  } else if(on_or_off==1) {
  
    command = 72;
    digitalWrite(led_pin, HIGH);
    
  } 

  // Send the command
  BluetoothSerial.write(48+pump_channel);
  BluetoothSerial.write(command); 
  end_of_message();
}

// End of every pump message must be followed by these ASCII chars
void end_of_message() {
    BluetoothSerial.write(13); // Carriage return
    BluetoothSerial.write(10);  // New line feed
}

// Configures the RPM (0100[.]00) for a particular channel
void set_channel_speed(int pump_channel) {
  
    // Configure pump to appropriate channel
    BluetoothSerial.write(49); // Pump ID 1
    BluetoothSerial.write(126); // tilde prefix
    BluetoothSerial.write(48+pump_channel);  // Channel to operate
    end_of_message();
    
    int speed_char = 83;
    BluetoothSerial.write(48+pump_channel); // Channel to operate
    BluetoothSerial.write(speed_char); 
    BluetoothSerial.write(48); 
    BluetoothSerial.write(49); 
    BluetoothSerial.write(48); 
    BluetoothSerial.write(48); 
    BluetoothSerial.write(48); 
    BluetoothSerial.write(48); 
    end_of_message();
}


void setup() {
  // Configure bluetooth connection rate
  Serial.begin(9600);
    while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB
  }

  // Connect to bluetooth module
  BluetoothSerial.begin(9600);
  delay(200);

  // Set RPM of all channels to max
  set_channel_speed(1);
  set_channel_speed(2);
  set_channel_speed(3);
  set_channel_speed(4);

  // Also display behaviour on LED pin
  pinMode(led_pin, OUTPUT);

}

void loop() {
  
  // Check for input
  if(Serial.available() > 0){

    bufferInt = Serial.read();

    switch (bufferInt-48) {
      
        case 0: {
                  // All off
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

