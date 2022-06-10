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

    void setDirectionPin(bool dir);

    void reverseDirectionPin();

    int getPosition(){
      return position;
    }

    void setPosition(int pos){
      position = pos;
    }

    void setLimit(Limit lim){
      limit = lim;
    }
    
  private:
    int pinStep = -1, pinDir = -1, pinEn = -1;
    int position = 0;
    Limit limit;
    bool direction = true;
};