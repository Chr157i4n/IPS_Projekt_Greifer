#include <Arduino.h>
#include <SoftwareSerial.h>

#define END_MARKER '\n'

const int ledPin =  13;  // Built-in LED
const int EnTxPin = 7;  // HIGH:Transmitter, LOW:Receiver
SoftwareSerial sSerial(3, 2); //1: rX 2: tX

String buffer,buffer2;
boolean ready = false;

void sendAnswer(char command, String message);
String readAnswer();


void setReceving(){
   digitalWrite(EnTxPin, LOW); 
}

void setTransmitting(){
   digitalWrite(EnTxPin, HIGH); 
}

void ParseLine()
{
  char command = buffer[0];
  String message = buffer.substring(1);

  switch (toupper(command))
  {
      case 'T':
          Serial.println((String) "test: "+message);
          sendAnswer('a',"test");
          break;
      case 'A':
          Serial.println((String) "answer: "+message);
          break;
      case 'B':
          break;
  }

  buffer="";
  sSerial.flush();
}

String sendMessage(String message){
  setTransmitting();
  
  sSerial.print(message);
  sSerial.print("\n"); //finish data packet

  String answer = readAnswer();
  delay(40);
  return answer;
}

String sendCommand(char command, String message){
  setTransmitting();
  
  sSerial.print(command);
  sSerial.print(message);
  sSerial.print("\n"); //finish data packet

  String answer = readAnswer();
  delay(40);
  return answer;
}

String readCommand(){
  setReceving();
  buffer = "";
 
  while (sSerial.available())
  {
    char c = sSerial.read();
    
    if (c == END_MARKER)
    {
      buffer += '\0';
      Serial.println((String) "got: "+buffer.length()+" : "+buffer);
      ParseLine();
    } else {
      buffer += c;
    }
  }
  return buffer;
}

void sendAnswer(char command, String message){
  setTransmitting();
  delay(10);

  sSerial.print(command);
  sSerial.print(message);
  sSerial.print("\n"); //finish data packet
}

String readAnswer(){
  setReceving();
  //sSerial.flush();

  while(!sSerial.available()) {
    //Serial.println((String) "no data available");
    }

  return readCommand();
}

void setup() { 

  Serial.begin(115200);
  sSerial.begin(19200);
  pinMode(ledPin, OUTPUT);
  pinMode(EnTxPin, OUTPUT);
  digitalWrite(ledPin, LOW); 

  Serial.println("Transmitter");
} 
 
void loop() 
{   
  
  // String message = "test123";
  // //Serial.println((String) "transmitting: "+message);

  // sendCommand('m',message);
  // sendCommand('t',message);
  // delay(1000);

  while (Serial.available())
  {
    char c = Serial.read();
    
    if (c == END_MARKER)
    {
      buffer2 += '\0';
      //Serial.println((String) "got: "+buffer.length()+" : "+buffer);
      Serial.println("nachricht: "+buffer2);
      Serial.println((String)"antwort: "+sendMessage(buffer2));
      buffer2 = "";
    } else {
      buffer2 += c;
    }
  }
  
}