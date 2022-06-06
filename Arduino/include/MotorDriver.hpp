#include <Arduino.h>

/*
* Rx pins should be directly connected with the PDN_UART Pin
* Tx should be connected with the PDN_UART with a 1k Ohm resistor
*
*/

struct Limit
{
  int min = 0;
  int max = 0;
};

class MotorDriver 
{
  public:

    /*
    * constructor of the class
    * the number of the pins for tx and rx is needed as parameter
    */
    MotorDriver(int pin_Step, int pin_Dir, int pin_En);

    int makeAStep(bool ignore_limit=false);

    void makeXSteps(int steps);

    void setMotorEnabled(bool en);

    void setDirection_pin(bool dir);

    void reverseDirection_pin();

    void setPosition(int pos){
      position = pos;
    }

    void setLimit(Limit lim){
      limit = lim;
    }
    
  private:
    int _pin_Step = -1, _pin_Dir = -1, _pin_En = -1;
    int position = 0;
    Limit limit;
    bool _direction = true;
};