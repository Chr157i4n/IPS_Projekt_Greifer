#include <Arduino.h>

#include <Wire.h> //For I2C/SMBus

#define FORCE_LEVER_RATIO 2.16667
#define FORCE_CALIB 9.81*4/58


class ForceSensor
{
public:
    /*
     * constructor of the class
     */
    ForceSensor(short address = 0x04);

    float getValue(); // reads the sensor value, return the force in Newton

    float getValueRaw(); // reads the sensor value, return the force in Newton

    float getValueAverage(int valueCount=4);

    short readDataFromSensor();

private:
    short i2cAddress = 0;
    int timeout = 2;
};