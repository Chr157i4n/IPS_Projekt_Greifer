#include <Arduino.h>

#include <Wire.h> //For I2C/SMBus

/*-----------------------------------------------------------------------------
 * SingleTact I2C Demo
 *
 * Copyright (c) 2016 Pressure Profile Systems
 * Licensed under the MIT license. This file may not be copied, modified, or
 * distributed except according to those terms.
 *
 *
 * Demonstrates sensor input by reading I2C and display value on comm port.
 *
 * The circuit: as described in the manual for PC interface using Arduino.
 *
 * To compile: Sketch --> Verify/Compile
 * To upload: Sketch --> Upload
 *
 * For comm port monitoring: Click on Tools --> Serial Monitor
 * Remember to set the baud rate at 57600.
 *
 * September 2016
 * ----------------------------------------------------------------------------- */


#define FORCE_LEVER_RATIO 2.16667


class ForceSensor
{
public:
    /*
     * constructor of the class
     */
    ForceSensor(short address = 0x04);

    float getValue(); // reads the sensor value, return the force in Newton

    float getValueAverage(int valueCount=4);

    short readDataFromSensor();

private:
    short i2cAddress = 0;
    int timeout = 2;
};