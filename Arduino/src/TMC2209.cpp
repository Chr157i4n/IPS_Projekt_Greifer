#include "TMC2209.hpp"

int TMC2209::_pin_Tx = -1;
int TMC2209::_pin_Rx = -1;

TMC2209::TMC2209(int pin_Tx, int pin_Rx){
    _pin_Tx = pin_Tx;
    _pin_Rx = pin_Rx;
}