#include <Arduino.h>
#include "MotorDriver.hpp"
#include "RS485.hpp"
#include "ForceSensor.hpp"

#define COMM_DELIMITER ' '

/*
* G0    -   Move the Motor a given amount of steps (X+-xxx, like X-100, or X100)
* G28   -   Move to Origin (Home)
*
* M17   -   Enable Motor Output
* M18   -   Disable Motor Output
* M42   -   Switch I/O pin
* M43   -   Read I/O pin
* M113  -   Host Keepalive
* M226  -   Wait for pin state
*/


RS485 rs485(2, 3, 7, 19200); // 1: rX 2: tX
MotorDriver motorDriver(5, 4, 10, 11, 12, 8, 115200);
ForceSensor forceSensor();

void parseLine(String message)
{
  Serial.println((String)"Parsing Line: "+message);
  
  int index_command_end = message.indexOf(COMM_DELIMITER);
  String command = "", parameters = "";

  if(index_command_end>0){
    command = message.substring(0,index_command_end);
    parameters = message.substring(index_command_end);
  } else {
    command = message;
  }


  if(command == "G0") // drive Motor
  {
    int index_X = parameters.indexOf('X');
    int index_X_end = parameters.indexOf(' ', index_X);
    String move_amount = parameters.substring(index_X+1, index_X_end);
    motorDriver.makeXSteps(move_amount.toInt());
    rs485.sendAnswer((String)"motor_move");
  }
  else if(command == "M17") // enable Motor Output
  {
    motorDriver.setMotorEnabled(true);
    rs485.sendAnswer((String)"motor_enabled");
  }
  else if(command == "M18") // disable Motor Output
  {
    motorDriver.setMotorEnabled(false);
    rs485.sendAnswer((String)"motor_disabled");
  }
  else if(command == "M42") // Switch I/O pin
  {
    int index_P = parameters.indexOf('P');
    int index_P_end = parameters.indexOf(' ', index_P);
    String pin = parameters.substring(index_P+1, index_P_end);

    int index_S = parameters.indexOf('S');
    int index_S_end = parameters.indexOf(' ', index_S);
    String state = parameters.substring(index_S+1, index_S_end);

    digitalWrite(pin.toInt(), state.toInt());
    rs485.sendAnswer((String)"pin_switched");
  }
  else if(command == "M43") // Read I/O pin
  {
    int index_P = parameters.indexOf('P');
    int index_P_end = parameters.indexOf(' ', index_P);
    String pin = parameters.substring(index_P+1, index_P_end);

    bool state = digitalRead(pin.toInt());
    rs485.sendAnswer((String)state);
    Serial.println((String)"A"+state);
  }
  else
  {
    rs485.sendError("command_not_available");
    Serial.println("Command not available");
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
  //("M43 P10");
  //delay(1000);
}