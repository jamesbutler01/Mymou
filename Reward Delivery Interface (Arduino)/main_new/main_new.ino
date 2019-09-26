
#include <SoftwareSerial.h>
SoftwareSerial BluetoothSerial(10, 11); // RX, TX

// Initialise variables
int bufferInt = -1;
int pump_channel, on_or_off;
int on_signal = 72;
int off_signal = 73;
int channel_number_offset = 48;
int carriage_return = 13;
int new_line_feed = 10;
int tilde = 126;
char command;

int pumpcommand(int pump_channel, int on_or_off)
{
  
  if(on_or_off==0) {command = off_signal;} //wylacz pompe
  if(on_or_off==1) {command = on_signal;} //wlacz pompe

  // Set pump to correct channel
  BluetoothSerial.write(tilde);
  BluetoothSerial.write(channel_number_offset+pump_channel); 
  end_of_message();
  
  // Now control that channel
  BluetoothSerial.write(channel_number_offset+pump_channel);   
  BluetoothSerial.write(command); 
  end_of_message();
}

// Carriage return and new line feed to be sent at end of every message
void end_of_message() {
    BluetoothSerial.write(carriage_return);
    BluetoothSerial.write(new_line_feed);
}

// Configures the RPM for a particular channel
void set_channel_speed(int pump_channel) {
  
    // Set pump to correct channel
    BluetoothSerial.write(tilde);
    BluetoothSerial.write(channel_number_offset+pump_channel); 
    end_of_message();
    
    int speed_char = 83;
    BluetoothSerial.write(channel_number_offset+pump_channel);   
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
  
    BluetoothSerial.begin(9600);
    delay(200);

    // Set RPM of all channels to max
    set_channel_speed(1);
    set_channel_speed(2);
    set_channel_speed(3);
    set_channel_speed(4);

}

void loop() {
  
    // Check for input
    if(Serial.available() > 0){
      
        bufferInt = Serial.read();
    
        switch (bufferInt-channel_number_offset) {
            case 0: 
              pumpcommand(1,0); 
              pumpcommand(2,0); 
              pumpcommand(3,0); 
              pumpcommand(4,0); 
              break;
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

