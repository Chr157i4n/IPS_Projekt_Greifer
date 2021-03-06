#include "ForceSensor.hpp"

ForceSensor::ForceSensor(short address)
{
    Wire.begin(); // join i2c bus (address optional for master)
    // TWBR = 12; //Increase i2c speed if you have Arduino MEGA2560, not suitable for Arduino UNO
    i2cAddress = address;
}

float ForceSensor::getValue()
{
    float force = getValueRaw();
    force = (float(force)-255)/512*450;
    force = force/FORCE_LEVER_RATIO*FORCE_CALIB;

    return force;
}

float ForceSensor::getValueRaw(){
    float data = -1;
    short tries = 0;
    while(data == -1){
        tries++;
        if(tries>=10)
            return -1;
        data = readDataFromSensor();

    }
    return data;
}

float ForceSensor::getValueAverage(int valueCount){
    float value = 0;
    for(int i=0; i<valueCount; i++){
        float newvalue = getValue();
        //Serial.println((String)"value: "+value);
        if(newvalue == -1) return -1;
        value += newvalue;
        delayMicroseconds(100);
    }
    value /= valueCount;
    //Serial.println((String)"value average: "+value);
    return value;
}

short ForceSensor::readDataFromSensor()
{
    int i2cPacketLength = 6;  // i2c packet length. Just need 6 bytes from each slave
    byte outgoingI2CBuffer[3]; // outgoing array buffer
    byte incomingI2CBuffer[6]; // incoming array buffer

    outgoingI2CBuffer[0] = 0x01;            // I2c read command
    outgoingI2CBuffer[1] = 128;             // Slave data offset
    outgoingI2CBuffer[2] = i2cPacketLength; // require 6 bytes

    Wire.beginTransmission(i2cAddress);     // transmit to device
    Wire.write(outgoingI2CBuffer, 3);    // send out command
    byte error = Wire.endTransmission(); // stop transmitting and check slave status
    if (error != 0)
        return -1;                              // if slave not exists or has error, return -1

    Wire.requestFrom(i2cAddress, i2cPacketLength); // require 6 bytes from slave

    byte incomeCount = 0;
    unsigned long startTime = millis();
    while (incomeCount < i2cPacketLength) // slave may send less than requested
    {
        if (Wire.available())
        {
            incomingI2CBuffer[incomeCount] = Wire.read(); // receive a byte as character
            incomeCount++;
        }
        else
        {
            delayMicroseconds(10); // Wait 10us
        }

        if(millis() > startTime+timeout){
            return -1;
        }
    }

    short rawData = (incomingI2CBuffer[4] << 8) + incomingI2CBuffer[5]; // get the raw data

    return rawData;
}
