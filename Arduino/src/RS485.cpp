#include "RS485.hpp"

RS485::RS485(int pin_EnTxPin, int baudrate, int pin_Tx, int pin_Rx)
{
    _pin_Tx = pin_Tx;
    _pin_Rx = pin_Rx;
    pinEnTxPin = pin_EnTxPin;
    _baudrate = baudrate;

    pinMode(pinEnTxPin, OUTPUT);

#if USE_HARWARE_SERIAL == 1
    _sSerial = &Serial;
#else
    _sSerial = new SoftwareSerial(_pin_Rx, _pin_Tx);
    _sSerial->begin(_baudrate);
#endif

    

    setReceving();
}

void RS485::sendMessage(String message)
{
    delay(4);
    beginTransmission();
    _sSerial->print(message);
    _sSerial->print("\n"); // finish data packet
    delay(2);
    endTransmission();
}

String RS485::sendCommand(char command, String message)
{
    sendMessage(command + message);
    String answer = readAnswer();
    _sSerial->flush();
    delay(40);
    return answer;
}

void RS485::sendAnswer(String message){
    sendMessage('A'+message);
}

void RS485::sendError(String message){
    sendMessage('E'+message);
}

void RS485::sendWarning(String message){
    sendMessage('W'+message);
}

void RS485::flush()
{
    //Serial.println("flushing RS485 Buffer");
    while (_sSerial->available()>0)
    {
        _sSerial->read();
    }
    
    _sSerial->flush();
}

String RS485::readAnswer()
{
    // _sSerial->flush();

    while (!_sSerial->available())
    {
    }

    return readCommand();
}

String RS485::readCommand()
{
    buffer="";
    buffer = Serial.readStringUntil(endmarker);
    //Serial.println((String) "got: "+buffer.length()+" : "+buffer);
    flush();
    return buffer;
}