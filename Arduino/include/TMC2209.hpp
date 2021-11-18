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
    TMC2209(int pin_Tx, int pin_Rx);

    
    
  private:
    static int _pin_Tx, _pin_Rx;
};