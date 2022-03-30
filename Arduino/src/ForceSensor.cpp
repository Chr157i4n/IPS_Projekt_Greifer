#include "ForceSensor.hpp"

ForceSensor::ForceSensor()
{
    Wire.begin(); // join i2c bus (address optional for master)
    // TWBR = 12; //Increase i2c speed if you have Arduino MEGA2560, not suitable for Arduino UNO
}

float ForceSensor::getValue()
{
    byte i2cAddress = 0x04; // Slave address (SingleTact), default 0x04
    short data = readDataFromSensor(i2cAddress);
    //Serial.print("I2C Sensor Data:");
    //Serial.print(data);
    //Serial.print("\n");
    //delay(100); // Change this if you are getting values too quickly
    float force = (float(data)-255)/512*450;

    return force;
}

short ForceSensor::readDataFromSensor(short address)
{
    byte i2cPacketLength = 6;  // i2c packet length. Just need 6 bytes from each slave
    byte outgoingI2CBuffer[3]; // outgoing array buffer
    byte incomingI2CBuffer[6]; // incoming array buffer

    outgoingI2CBuffer[0] = 0x01;            // I2c read command
    outgoingI2CBuffer[1] = 128;             // Slave data offset
    outgoingI2CBuffer[2] = i2cPacketLength; // require 6 bytes

    Wire.beginTransmission(address);     // transmit to device
    Wire.write(outgoingI2CBuffer, 3);    // send out command
    byte error = Wire.endTransmission(); // stop transmitting and check slave status
    if (error != 0)
        return -1;                              // if slave not exists or has error, return -1
    Wire.requestFrom(address, i2cPacketLength); // require 6 bytes from slave

    byte incomeCount = 0;
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
    }

    short rawData = (incomingI2CBuffer[4] << 8) + incomingI2CBuffer[5]; // get the raw data

    return rawData;
}
