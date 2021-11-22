#include "TMC2209.hpp"


TMC2209::TMC2209(int pin_Tx, int pin_Rx, int pin_Step, int pin_Dir, int pin_En, int pin_StallGuard, int baudrate){
    _pin_Tx = pin_Tx;
    _pin_Rx = pin_Rx;
    _pin_Step = pin_Step;
    _pin_Dir = pin_Dir;
    _pin_En = pin_En;
    _pin_StallGuard = pin_StallGuard;
    _baudrate = baudrate;

    setMotorEnabled(false);
    pinMode(_pin_En, OUTPUT);
    setMotorEnabled(false);

    pinMode(_pin_Step, OUTPUT);
    pinMode(_pin_Dir, OUTPUT);
    pinMode(_pin_StallGuard, INPUT);
}

void TMC2209::makeAStep(){
    //Serial.println((String)"Make one Step");
    digitalWrite(_pin_Step, HIGH);
    delayMicroseconds(10);
    digitalWrite(_pin_Step, LOW);
    delayMicroseconds(10);
}

void TMC2209::makeXSteps(int steps){

    if(steps==0) return;

    setDirection_pin(steps>0);

    steps = abs(steps);

    for(int i=0; i<steps; i++){
        makeAStep();
        delay(1);
    }
}

void TMC2209::setMotorEnabled(bool en){
    Serial.println((String)"Motor Enable: "+_pin_En+" to "+!en);
    digitalWrite(_pin_En, !en);
}

void TMC2209::setDirection_pin(bool dir){
    Serial.println((String)"Motor Dir: "+_pin_Dir+" to "+dir);
    _direction = dir;
    digitalWrite(_pin_Dir, _direction);
    delay(10);
}

void TMC2209::reverseDirection_pin(){
    _direction = !_direction;
    digitalWrite(_pin_Dir, _direction);
}