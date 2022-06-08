#include "MotorDriver.hpp"


MotorDriver::MotorDriver(int pin_Step, int pin_Dir, int pin_En){
    pinStep = pin_Step;
    pinDir = pin_Dir;
    pinEn = pin_En;

    setMotorEnabled(false);
    pinMode(pinEn, OUTPUT);
    setMotorEnabled(false);

    pinMode(pinStep, OUTPUT);
    pinMode(pinDir, OUTPUT);
}

int MotorDriver::makeAStep(bool ignore_limit){
    
    int next_position = position + ((direction) ? 1 : -1);
    if((next_position<limit.min || next_position>limit.max) && !ignore_limit){
        Serial.println((String)"Out of Bounds: "+position);
        return -1;
    }

    //Serial.println((String)"Make one Step");
    //Serial.println((String)"pos: "+position);

    digitalWrite(pinStep, HIGH);
    delayMicroseconds(500);
    digitalWrite(pinStep, LOW);
    delayMicroseconds(500);

    position = next_position;
    return 0;
}

void MotorDriver::makeXSteps(int steps){

    if(steps==0) return;

    setDirectionPin(steps>0);

    steps = abs(steps);

    for(int i=0; i<steps; i++){
        makeAStep();
        delay(1);
    }
}

void MotorDriver::setMotorEnabled(bool en){
    Serial.println((String)"Motor Enable: "+pinEn+" to "+!en);
    digitalWrite(pinEn, !en);
}

void MotorDriver::setDirectionPin(bool dir){
    Serial.println((String)"Motor Dir: "+pinDir+" to "+dir);
    direction = dir;
    digitalWrite(pinDir, direction);
    delay(10);
}

void MotorDriver::reverseDirectionPin(){
    direction = !direction;
    digitalWrite(pinDir, direction);
}