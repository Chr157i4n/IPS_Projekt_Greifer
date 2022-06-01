#include <Arduino.h>
#include "MotorDriver.hpp"
#include "RS485.hpp"
#include "ForceSensor.hpp"

#define COMM_DELIMITER ' '
//#define DEBUG
#define ENDSTOP_PIN 8
#define SPINDLE_PITCH 1.5

/*
* G0    -   Move the Motor a given amount of steps (X+-xxx, like X-100, or X100)
* G2    -   close (specify clamping force with Fxx)
* G3    -   clamp
* G28   -   Move to Origin (Home)
*
* M17   -   Enable Motor Output
* M18   -   Disable Motor Output
* M42   -   Switch I/O pin
* M44   -   Read Sensor value
* M43   -   Read I/O pin
* M113  -   Host Keepalive
* M226  -   Wait for pin state
*/

RS485 rs485(9, 115200);
MotorDriver motorDriver(11, 13, 12);
ForceSensor forceSensor;

float close(int force){
  motorDriver.setDirection_pin(true);
  float currentForce = forceSensor.getValue();
  while(currentForce < force){
    rs485.keepAlive();
    motorDriver.makeAStep();
    delay(1);
    currentForce = forceSensor.getValue();
  }
  return currentForce;
}

float open(int force = 10, int steps_afterwards=200){
  motorDriver.setDirection_pin(false);
  float currentForce = forceSensor.getValue();
  while(currentForce > force){
    rs485.keepAlive();
    motorDriver.makeAStep();
    delay(1);
    currentForce = forceSensor.getValue();
  }
  for(int i=0; i<steps_afterwards; i++){
    rs485.keepAlive();
    motorDriver.makeAStep();
    delay(1);
  }
  currentForce = forceSensor.getValue();
  return currentForce;
}

long move(long steps){
  if(steps==0) return 0;
  motorDriver.setDirection_pin(steps>0);
  if(steps<0) steps=-steps;
  for(int i=0; i<steps; i++){
    rs485.keepAlive();
    motorDriver.makeAStep();
    delay(1);
  }
  return steps;
}

int home(){
  bool endstop_state = digitalRead(ENDSTOP_PIN);
    while(endstop_state){
      rs485.keepAlive();
      motorDriver.makeAStep();
      delay(1);
      endstop_state = digitalRead(ENDSTOP_PIN);
      //Serial.println((String)"Pinstate: "+endstop_state);
  }
  return 0;
}

void parseLine(String message)
{
  Serial.println((String)"Parsing Line: "+message);
  message.trim();
  int index_command_end = message.indexOf(COMM_DELIMITER);
  String command = "", parameters = "";

  if(index_command_end>0){
    command = message.substring(0,index_command_end);
    parameters = message.substring(index_command_end);
  } else {
    command = message.substring(0,message.length());
  }

  //Serial.println((String)"command: "+command);

  if(command == "G0") // drive Motor
  {
    int index_X = parameters.indexOf('X');
    int index_S = parameters.indexOf('S');
    if(index_X>=0){
      int index_X_end = parameters.indexOf(' ', index_X);
      String move_amount_mm = parameters.substring(index_X+1, index_X_end);
      long steps = round(move_amount_mm.toFloat()*200/SPINDLE_PITCH);
      steps = move(steps);
      rs485.sendAnswer((String)steps);
    }else if(index_S>=0){
      int index_S_end = parameters.indexOf(' ', index_S);
      String move_amount_steps = parameters.substring(index_S+1, index_S_end);
      int steps = move(move_amount_steps.toInt());
      rs485.sendAnswer((String)steps);
    }
  }else if(command == "G2") // close
  {
    int index_F = parameters.indexOf('F');
    int index_F_end = parameters.indexOf(' ', index_F);
    String move_amount_force = parameters.substring(index_F+1, index_F_end);
    float actual_force = close(move_amount_force.toInt());
    rs485.sendAnswer((String)actual_force);
  }else if(command == "G3") // open
  {
    float actual_force = open();
    rs485.sendAnswer((String)actual_force);
  }else if(command == "G28") // open
  {
    float actual_force = home();
    rs485.sendAnswer((String)"homed");
  }else if(command == "M1337") // Test
  {
    rs485.sendAnswer((String)"leet");
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
    //Serial.println((String)"A"+state);
  }
  else if(command == "M44") // Read Sensor value
  {
    int index_S = parameters.indexOf('S');
    int index_S_end = parameters.indexOf(' ', index_S);
    String sensor = parameters.substring(index_S+1, index_S_end);
    float value = -1;
    if(sensor.toInt() == 1){
      value = forceSensor.getValue();
    }
    rs485.sendAnswer((String)value);
    //Serial.println((String)"A"+state);
  }
  else
  {
    rs485.sendError("command_not_available");
    //Serial.println("Command not available");
  }

  rs485.flush();
}

void setup()
{
  Serial.begin(115200);
  Serial.setTimeout(2000);
  Serial.println("---");

  Wire.begin();
  pinMode(LED_BUILTIN, OUTPUT);

  motorDriver.setDirection_pin(true);
}

void loop()
{
  String answer = rs485.readCommand();
  if (answer.length()>0)
  {
    digitalWrite(LED_BUILTIN, HIGH);
    #ifdef DEBUG
    Serial.println((String)"Message: "+answer);
    #endif
    parseLine(answer);
    digitalWrite(LED_BUILTIN, LOW);
  }
  delay(1);
}