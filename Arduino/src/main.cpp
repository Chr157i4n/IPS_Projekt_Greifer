#include <Arduino.h>
#include "TMC2209.hpp"
#include "RS485.hpp"

RS485 rs485(3, 2, 7, 19200); //1: rX 2: tX
TMC2209 tmc2209(5, 4, 10, 11, 12, 8, 19200);


void parseLine(String message)
{
  char command = message[0];
  message = message.substring(1);

  switch (toupper(command))
  {
      case 'T':
          rs485.sendAnswer("test back");
          break;
      case 'A':
          break;
      case 'B':
          break;
      case 'M':
          int measuredValue = analogRead(A0);
          rs485.sendAnswer((String) measuredValue);
          break;
  }
}



void setup() { 
  Serial.begin(115200);

  tmc2209.setMotorEnabled(true);
  tmc2209.setDirection_pin(true);

  for(int i=0; i<1000; i++){
    tmc2209.makeAStep();
    delay(10);
  }

  tmc2209.setMotorEnabled(false);
} 
 
void loop() 
{   
  // String answer = rs485.readCommand();
  // if(answer != ""){
  //   parseLine(answer);
  // }

}