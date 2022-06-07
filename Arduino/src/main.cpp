#include <Arduino.h>
#include "MotorDriver.hpp"
#include "RS485.hpp"
#include "ForceSensor.hpp"

#define COMM_DELIMITER ' '
//#define DEBUG
#define ENDSTOP_PIN 8
#define SPINDLE_PITCH 1.5
#define MOTOR_SPEED_DELAY 1

#define LIMIT_MIN 0
#define LIMIT_MAX 35

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
* M114  -   Get Current Position
* M226  -   Wait for pin state
*/

RS485 rs485(9, 115200);
MotorDriver motorDriver(11, 13, 12);
ForceSensor forceSensor;

float close(int force){
  motorDriver.setDirectionPin(true);
  float currentForce = forceSensor.getValue();
  while(currentForce < force){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) break;
    delay(MOTOR_SPEED_DELAY);
    currentForce = forceSensor.getValue();
  }
  return currentForce;
}

float open(int force = 10, int steps_afterwards=800){
  motorDriver.setDirectionPin(false);
  float currentForce = forceSensor.getValue();
  while(currentForce > force){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) break;
    delay(MOTOR_SPEED_DELAY);
    currentForce = forceSensor.getValue();
  }
  for(int i=0; i<steps_afterwards; i++){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) break;
    delay(MOTOR_SPEED_DELAY);
  }
  currentForce = forceSensor.getValue();
  return currentForce;
}

long move(long steps){
  if(steps==0) return 0;
  motorDriver.setDirectionPin(steps>0);
  if(steps<0) steps=-steps;
  for(int i=0; i<steps; i++){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) break;
    delay(1);
  }
  return steps;
}

int home(int steps_afterwards=400){
  motorDriver.setDirectionPin(false);
  bool endstop_state = digitalRead(ENDSTOP_PIN);
    while(endstop_state){
      rs485.keepAlive();
      motorDriver.makeAStep(true);
      delay(MOTOR_SPEED_DELAY);
      endstop_state = digitalRead(ENDSTOP_PIN);
      //Serial.println((String)"Pinstate: "+endstop_state);
    }
    motorDriver.setDirectionPin(true);
    for(int i=0; i<steps_afterwards; i++){
      rs485.keepAlive();
      motorDriver.makeAStep(true);
      delay(MOTOR_SPEED_DELAY);
    }
    motorDriver.setPosition(0);
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
  }else if(command == "M17") // enable Motor Output
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

    pinMode(pin.toInt(), INPUT);
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
    if(sensor.toInt() == 0){  //motor position
      value = motorDriver.getPosition();
    }
    if(sensor.toInt() == 1){  //force sensor value
      value = forceSensor.getValue();
    }
    if(sensor.toInt() == 2){  //endstop 1 value
      value = digitalRead(8);
    }
    rs485.sendAnswer((String)value);
    //Serial.println((String)"A"+state);
  }else if(command == "M114") // Get Current Position
  {
    int index_X = parameters.indexOf('X');
    if(index_X != -1){
      rs485.sendAnswer((String)motorDriver.getPosition());
    }
    
  }else if(command == "M1337") // Test
  {
    rs485.sendAnswer((String)"leet");
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

  Limit motor_limit;
  motor_limit.min = LIMIT_MIN;
  motor_limit.max = LIMIT_MAX;
  motorDriver.setLimit(motor_limit);

  Wire.begin();
  pinMode(LED_BUILTIN, OUTPUT);

  motorDriver.setDirectionPin(true);
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