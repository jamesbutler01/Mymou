# Bluetooth reward-system interface

* C# code to control the reward-delivery interface (Arduino Uno board)

* Controls up to 4 independent reward systems simultaneously 

  * Trivial to alter code to control more reward systems if desired

* Works via digital out channels 2-5 as channels 0 and 1 are used by the Bluetooth input

* Continuously monitors incoming Bluetooth signals and reacts like so: 

Command | Arduino Digital Pin | Action
------------ | ------------- | -------------  
0 | All | Low
1 | 2   | High
2 | 2   | Low
3 | 3   | High
4 | 3   | Low
5 | 4   | High
6 | 4   | Low
7 | 5   | High
8 | 5   | Low

* Also have main_ismatec which controls a 4-channel Ismatec Reglo peristaltic pump via RS232 cable, rather than 4 individual reward systems
