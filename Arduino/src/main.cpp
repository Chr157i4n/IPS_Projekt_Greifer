#include <Arduino.h>
#include "MotorDriver.hpp"
#include "RS485.hpp"
#include "ForceSensor.hpp"


//#define DEBUG

#define COMM_DELIMITER ' '

#define ENDSTOP_PIN 8
#define SPINDLE_PITCH 1.5
#define GEAR_RATIO 2
#define MOTOR_SPEED_DELAY 1
#define TWO_GRIPPER_JAWS_FACTOR 2

#define LIMIT_MIN_MM 0
#define LIMIT_MAX_MM 48

#define PIN_END1 8
#define PIN_END2 7
#define PIN_MOTORDRIVER_EN 12
#define PIN_MOTORDRIVER_STEP 11
#define PIN_MOTORDRIVER_DIR 13
#define RS485_EN_TX 9

/*

Commands:
G0    -   Move the Motor a given amount of steps(S) / mm(X) (X+-xxx, like X-100, or X100)
G2    -   close (specify clamping force with Fxx)
G3    -   open
G28   -   Move to Origin (Home)
M17   -   Enable Motor Output
M18   -   Disable Motor Output
M42   -   Switch I/O pin
M44   -   Read Sensor value
M43   -   Read I/O pin
M113  -   Host Keepalive
M114  -   Get Current Position
M226  -   Wait for pin state


Answers begin with "A" when everything is ok
Error will be returned with a beginning "E"

Answers Codes:
A0    -   command successfully finished

Error Codes:
E10   -   Sensor communication error
E20   -   command not available
E30   -   parameter error
W40   -   out of Bound warning

*/



RS485 rs485(RS485_EN_TX, 115200);
MotorDriver motorDriver(PIN_MOTORDRIVER_STEP, PIN_MOTORDRIVER_DIR, PIN_MOTORDRIVER_EN);
ForceSensor forceSensor;

float steps_to_mm(int steps){
  return steps/200*SPINDLE_PITCH/GEAR_RATIO*TWO_GRIPPER_JAWS_FACTOR;
}

int mm_to_steps(float mm){
  return round(mm*200/SPINDLE_PITCH*GEAR_RATIO/TWO_GRIPPER_JAWS_FACTOR);
}

float close(int force){
  motorDriver.setDirectionPin(true);
  float currentForce = forceSensor.getValue();
  while(currentForce < force){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) return -1;
    delay(MOTOR_SPEED_DELAY);
    currentForce = forceSensor.getValueAverage(2);
    if(currentForce == -1){
      return -2;
    }
  }
  return currentForce;
}

float open(int force = 10, int steps_afterwards=800){
  motorDriver.setDirectionPin(false);
  float currentForce = forceSensor.getValue();
  while(currentForce > force){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) return -1;
    delay(MOTOR_SPEED_DELAY);
    currentForce = forceSensor.getValue();
  }
  for(int i=0; i<steps_afterwards; i++){
    rs485.keepAlive();
    if(motorDriver.makeAStep() == -1) return -1;
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
    if(motorDriver.makeAStep() == -1) return -1;
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
      long return_value = move(mm_to_steps(move_amount_mm.toFloat()));
      if(return_value == -1){
        rs485.sendWarning((String)"40");
      }else{
        rs485.sendAnswer((String)return_value);
      }
    }else if(index_S>=0){
      int index_S_end = parameters.indexOf(' ', index_S);
      String move_amount_steps = parameters.substring(index_S+1, index_S_end);
      int return_value = move(move_amount_steps.toInt());
      if(return_value == -1){
        rs485.sendWarning((String)"40");
      }else{
        rs485.sendAnswer((String)return_value);
      }
    }else{
      rs485.sendError((String)"30");
    }
  }else if(command == "G2") // close
  {
    int index_F = parameters.indexOf('F');
    if(index_F>=0){
      String move_amount_force = parameters.substring(index_F+1);
      float return_value = close(move_amount_force.toInt());
      if(return_value == -1){
        rs485.sendWarning((String)"40");
      }else if(return_value == -2){
        rs485.sendError((String)"10");
      }else{
        rs485.sendAnswer((String)return_value);
      }
    }else{
      rs485.sendError((String)"30");
    }
  }else if(command == "G3") // open
  {
    float return_value = open();
    if(return_value == -1){
        rs485.sendWarning((String)"40");
    }else{
      rs485.sendAnswer((String)return_value);
    }
  }else if(command == "G28") // open
  {
    float status = home();
    rs485.sendAnswer((String)status);
  }else if(command == "M17") // enable Motor Output
  {
    motorDriver.setMotorEnabled(true);
    rs485.sendAnswer((String)"0");
  }
  else if(command == "M18") // disable Motor Output
  {
    motorDriver.setMotorEnabled(false);
    rs485.sendAnswer((String)"0");
  }
  else if(command == "M42") // Switch I/O pin
  {
    int index_P = parameters.indexOf('P');
    int index_P_end = parameters.indexOf(' ', index_P);
    int pin = 0;
    if(index_P >= 0 && index_P_end >= 0)
      pin = parameters.substring(index_P+1, index_P_end).toInt();
    else{
      rs485.sendError((String)"30");
    }

    int index_S = parameters.indexOf('S');
    int index_S_end = parameters.indexOf(' ', index_S);
    int state = 0;
    if(index_S >= 0 && index_S_end >= 0)
      state = parameters.substring(index_S+1, index_S_end).toInt();
    else{
      rs485.sendError((String)"30");
    }

    digitalWrite(pin, state);
    rs485.sendAnswer((String)"0");
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
    int sensor = parameters.substring(index_S+1, index_S_end).toInt();
    float value = -1;
    if(sensor == 0){  //motor position in steps
      value = motorDriver.getPosition();
    }else if(sensor == 1){  //motor position in mm
      value = steps_to_mm(motorDriver.getPosition());
    }else if(sensor == 2){  //force sensor value raw
      value = forceSensor.getValueRaw();
    }else if(sensor == 3){  //force sensor value
      value = forceSensor.getValue();
    }else if(sensor == 4){  //endstop 1 value
      value = digitalRead(PIN_END1);
    }else if(sensor == 5){  //endstop 2 value
      value = digitalRead(PIN_END2);
    }else{
      value = -2;
    }
    
    if(value == -1){
      rs485.sendError((String)"10");
    }else if(value == -2){
      rs485.sendError((String)"30");
    }else{
      rs485.sendAnswer((String)value);
    }
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
    rs485.sendError("20");
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
  motor_limit.min = mm_to_steps(LIMIT_MIN_MM);
  motor_limit.max = mm_to_steps(LIMIT_MAX_MM);
  motorDriver.setLimit(motor_limit);

  Wire.begin();
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(PIN_END1, INPUT_PULLUP);
  pinMode(PIN_END2, INPUT_PULLUP);

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

  // parseLine("M17");
  // parseLine("G28");
  // parseLine("G2 F20");
  // delay(4000);
  // parseLine("G3");
  // parseLine("G2 F20");
  // parseLine("G28");
  // parseLine("G0 X2");
  // parseLine("M18");
  // delay(10000);

  // parseLine("M17");
  // parseLine("G28");
  // delay(1000);
  // parseLine("G0 X30");
  // delay(1000);
  // parseLine("G0 X-30");
  // parseLine("M18");
  // delay(5000);

  // parseLine("M44 S2");
  // parseLine("M44 S3");
  // delay(1000);

}