#include <Arduino.h>
#include <SoftwareSerial.h>

#define USE_HARWARE_SERIAL 0

class RS485 
{
  public:

    /*
    * constructor of the class
    * the number of the pins for tx and rx is needed as parameter
    */
    RS485(int pin_Tx, int pin_Rx, int pin_EnTxPin, int baudrate);

    void sendMessage(String message);

    String sendCommand(char command, String message);

    void sendAnswer(String message);

    String readAnswer(); //waits for an answer and then reads commands

    String readCommand();

    void setReceving(){
        digitalWrite(_pin_EnTxPin, LOW); 
    }

    void setTransmitting(){
        digitalWrite(_pin_EnTxPin, HIGH); 
    }

    void beginTransmission(){
        setTransmitting();
    }

    void endTransmission(){
        setReceving();
    }

    
    
  private:
    int _pin_Tx = -1, _pin_Rx = -1, _pin_EnTxPin = -1, _baudrate = -1;
    
#if USE_HARWARE_SERIAL == 1
    HardwareSerial *_sSerial = NULL;
#else
    SoftwareSerial *_sSerial = NULL;
#endif

    String buffer = "";
    char endmarker = '\n';
};