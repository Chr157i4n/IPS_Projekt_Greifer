#include "RS485.hpp"

RS485::RS485(int pin_Tx, int pin_Rx, int pin_EnTxPin, int baudrate = 19200)
{
    _pin_Tx = pin_Tx;
    _pin_Rx = pin_Rx;
    _pin_EnTxPin = pin_EnTxPin;
    _baudrate = baudrate;

    pinMode(_pin_EnTxPin, OUTPUT);

    _sSerial = new SoftwareSerial(_pin_Rx, _pin_Tx);
    _sSerial->begin(_baudrate);

    setReceving();
}

void RS485::sendMessage(String message)
{
    beginTransmission();
    _sSerial->print(message);
    _sSerial->print("\n"); // finish data packet
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
    sendMessage('a'+message);
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
    buffer = "";

    while (_sSerial->available())
    {
        char c = _sSerial->read();

        if (c == endmarker)
        {
            buffer += '\0';
            // Serial.println((String) "got: "+buffer.length()+" : "+buffer);
            return buffer;
        }
        else
        {
            buffer += c;
        }
    }
    return "";
}