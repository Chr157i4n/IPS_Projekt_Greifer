#include "MotorDriver.hpp"


MotorDriver::MotorDriver(int pin_Step, int pin_Dir, int pin_En){
    _pin_Step = pin_Step;
    _pin_Dir = pin_Dir;
    _pin_En = pin_En;

    setMotorEnabled(false);
    pinMode(_pin_En, OUTPUT);
    setMotorEnabled(false);

    pinMode(_pin_Step, OUTPUT);
    pinMode(_pin_Dir, OUTPUT);
}

void MotorDriver::makeAStep(){
    //Serial.println((String)"Make one Step");
    digitalWrite(_pin_Step, HIGH);
    delayMicroseconds(500);
    digitalWrite(_pin_Step, LOW);
    delayMicroseconds(500);
}

void MotorDriver::makeXSteps(int steps){

    if(steps==0) return;

    setDirection_pin(steps>0);

    steps = abs(steps);

    for(int i=0; i<steps; i++){
        makeAStep();
        delay(1);
    }
}

void MotorDriver::setMotorEnabled(bool en){
    Serial.println((String)"Motor Enable: "+_pin_En+" to "+!en);
    digitalWrite(_pin_En, !en);
}

void MotorDriver::setDirection_pin(bool dir){
    Serial.println((String)"Motor Dir: "+_pin_Dir+" to "+dir);
    _direction = dir;
    digitalWrite(_pin_Dir, _direction);
    delay(10);
}

void MotorDriver::reverseDirection_pin(){
    _direction = !_direction;
    digitalWrite(_pin_Dir, _direction);
}