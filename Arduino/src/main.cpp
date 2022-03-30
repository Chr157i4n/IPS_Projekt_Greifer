#include <Arduino.h>
#include "MotorDriver.hpp"
#include "RS485.hpp"
#include "ForceSensor.hpp"

/*
* predefined chars for communication:
* e = error
* a = answer
*/


RS485 rs485(2, 3, 7, 19200); // 1: rX 2: tX
MotorDriver motorDriver(5, 4, 10, 11, 12, 8, 19200);
ForceSensor forceSensor();

void parseLine(String message)
{
  Serial.println((String)"Parsing Line: "+message);

  char command = message[0];
  message = message.substring(1);

  switch (toupper(command))
  {
  case 'E':
  {
    Serial.println("ERROR");
    break;
  }
  case 'B':
  {
    motorDriver.setMotorEnabled(message.toInt());
    if(message.toInt()==1){
      rs485.sendAnswer((String)"motor_enabled");
    } else {
      rs485.sendAnswer((String)"motor_disabled");
    }
    break;
  }
  case 'C':
  {
    motorDriver.setDirection_pin(message.toInt());
    break;
  }
  case 'D':
  {
    motorDriver.makeXSteps(message.toInt());
    rs485.sendAnswer((String)"motor_move");
    break;
  }
  case 'R':
  {
    int measuredValue = analogRead(14+message.toInt());
    rs485.sendAnswer((String)measuredValue);
    break;
  }
  case 'T':
  {
    rs485.sendAnswer("RS485 test back");
    Serial.println("Serial test back");
    break;
  }
  default:
  {
    rs485.sendError("command_not_available");
    Serial.println("Command not available");
    break;
  }
  }

  rs485.flush();
}

void setup()
{
  Serial.begin(115200);
  Serial.println("setup");

  motorDriver.setDirection_pin(true);

  

  Serial.println("setup finished");
}

void loop()
{
  String answer = rs485.readCommand();
  if (answer.length()>0)
  {
    //Serial.println((String)"Message: "+answer);
    rs485.flush();
    parseLine(answer);
    rs485.flush();
  }
}