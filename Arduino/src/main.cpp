#include <Arduino.h>
#include "TMC2209.hpp"
#include "RS485.hpp"

/*
* predefined chars for communication:
* e = error
* a = answer
*/


RS485 rs485(3, 2, 7, 19200); // 1: rX 2: tX
TMC2209 tmc2209(5, 4, 10, 11, 12, 8, 19200);

void parseLine(String message)
{
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
    tmc2209.setMotorEnabled(message.toInt());
    break;
  }
  case 'C':
  {
    tmc2209.setDirection_pin(message.toInt());
    break;
  }
  case 'D':
  {
    tmc2209.makeXSteps(message.toInt());
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
  }
}

void setup()
{
  Serial.begin(115200);
  Serial.println("setup");

  tmc2209.setDirection_pin(true);

  

  Serial.println("setup finished");
}

void loop()
{
  String answer = rs485.readCommand();
  if (answer != "")
  {
    parseLine(answer);
  }
}