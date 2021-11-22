#include <Arduino.h>

/*
* Rx pins should be directly connected with the PDN_UART Pin
* Tx should be connected with the PDN_UART with a 1k Ohm resistor
*
*/

class TMC2209 
{
  public:

    /*
    * constructor of the class
    * the number of the pins for tx and rx is needed as parameter
    */
    TMC2209(int pin_Tx, int pin_Rx, int pin_Step, int pin_Dir, int pin_En, int pin_StallGuard, int baudrate);

    void makeAStep();

    void setMotorEnabled(bool en);

    void setDirection_pin(bool dir);

    void reverseDirection_pin();
    
  private:
    int _pin_Tx = -1, _pin_Rx = -1, _pin_Step = -1, _pin_Dir = -1, _pin_En = -1, _pin_StallGuard = -1;
    int _baudrate = 19200;
    bool _direction = true;
};