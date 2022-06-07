#include <Arduino.h>
#include <SoftwareSerial.h>

#define USE_HARWARE_SERIAL 1

class RS485 
{
  public:

    /*
    * constructor of the class
    * the number of the pins for tx and rx is needed as parameter
    */
    RS485(int pin_EnTxPin, int baudrate = 19200, int pin_Tx = -1, int pin_Rx = -1);

    void sendMessage(String message);

    String sendCommand(char command, String message);

    void sendAnswer(String message);

    void sendError(String message);

    void keepAlive(){
        if(millis() > lastKeepAlive+1000){
            sendKeepAlive();
            lastKeepAlive = millis();
        }
    }

    void sendKeepAlive(){
        sendMessage("M113");
    }

    void flush();

    String readAnswer(); //waits for an answer and then reads commands

    String readCommand();

    void setReceving(){
        digitalWrite(pinEnTxPin, LOW); 
    }

    void setTransmitting(){
        digitalWrite(pinEnTxPin, HIGH); 
    }

    void beginTransmission(){
        setTransmitting();
    }

    void endTransmission(){
        setReceving();
    }

    
    
  private:
    int _pin_Tx = -1, _pin_Rx = -1, pinEnTxPin = -1, _baudrate = -1;
    unsigned long lastKeepAlive = 0;
    
#if USE_HARWARE_SERIAL == 1
    HardwareSerial *_sSerial = NULL;
#else
    SoftwareSerial *_sSerial = NULL;
#endif

    String buffer = "";
    char endmarker = '\n';
};