#include "TMC2209.hpp"


TMC2209::TMC2209(int pin_Tx, int pin_Rx, int pin_Step, int pin_Dir, int pin_En, int pin_StallGuard, int baudrate){
    _pin_Tx = pin_Tx;
    _pin_Rx = pin_Rx;
    _pin_Step = pin_Step;
    _pin_Dir = pin_Dir;
    _pin_En = pin_En;
    _pin_StallGuard = pin_StallGuard;
    _baudrate = baudrate;

    pinMode(_pin_Step, OUTPUT);
    pinMode(_pin_Dir, OUTPUT);
    pinMode(_pin_En, OUTPUT);
    pinMode(_pin_StallGuard, INPUT);
}

void TMC2209::makeAStep(){
    digitalWrite(_pin_Step, HIGH);
    delay(1);
    digitalWrite(_pin_Step, LOW);
    delay(1);
}

void TMC2209::setMotorEnabled(bool en){
    Serial.println((String)"Motor Enable: "+_pin_En);
    digitalWrite(_pin_En, !en);
}

void TMC2209::setDirection_pin(bool dir){
    _direction = dir;
    digitalWrite(_pin_Dir, _direction);
}

void TMC2209::reverseDirection_pin(){
    _direction = !_direction;
    digitalWrite(_pin_Dir, _direction);
}