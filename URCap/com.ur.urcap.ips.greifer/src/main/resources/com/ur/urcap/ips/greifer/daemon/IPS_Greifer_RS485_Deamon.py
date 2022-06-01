#!/usr/bin/env python

# RS485 device:  /dev/ttyTool

import sys

from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn
import serial
import time


def log(message):
	#sys.stdout.write(message+"\n")
	#sys.stderr.write(message+"\n")
	print(message)
	#print(message+"\n")


title = ""
port = "/dev/ttyTool"
KEEPALIVE = "M113"

counter = 0

while(True):
	try:
		ser = serial.Serial (port=port, baudrate=115200, parity=serial.PARITY_NONE, bytesize=8, stopbits=1, timeout=2.0, write_timeout=2.0,)
		break
	except:
		log("could not open port: "+port)
		#ser = None

def init():
	pass


	
def set_title(new_title):
	global title
	title = new_title
	return title

def get_title():
	tmp = ""
	if str(title):
		tmp = title
	else:
		tmp = "No title set"
	return tmp + " (Python)"

def get_message(name):
	if str(name):
		return "Moin " + str(name) + ", willkommen zu PolyScope!"
	else:
		return "No name set"

def send_message(message):
	if str(message):
		log("command: \""+str(message)+"\"")
		if(ser==None):
			value = "A" + message
		else:
			ser.reset_input_buffer()
			ser.write((message + "\n").encode())
			value = read_answer()
		log("return: \""+str(value)+"\"")
		return value
	else:
		return "Error: No message set."

def read_answer():
	buffer=KEEPALIVE
	while(buffer == KEEPALIVE):
		buffer = ser.readline()
		buffer = buffer.rstrip("\n")
		log(":"+str(buffer))
	if(buffer==""):
		log("NO ANSWER")
	if(buffer[0]=="E"):
		log("ERROR")
	return buffer

def get_measurement_value_test(channel):
	global counter
	log("Test Measurement from channel: "+channel+" requested")
	counter += 1
	return str(counter)

def get_measurement_value(channel):
	#log("Measurement from channel: "+channel+" requested")
	value = send_message("M43 P"+str(channel))
	return value[1:]

def motor_move(distance):
	#log("Move Motor "+str(distance)+" mm")
	value = send_message("G0 X"+str(distance))
	return value[1:]

def motor_power(enable):
	#log("Motor Power: "+str(enable))
	value = send_message("M1"+str(8-int(enable)))
	return value[1:]

def motor_close(force):
	#log("Motor Close: "+str(force))
	value = send_message("G2 "+str(force))
	return value[1:]

def motor_open():
	#log("Motor Open")
	value = send_message("G3")
	return value[1:]


log("MyDaemon daemon started")


class MultithreadedSimpleXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer):
	pass

server = MultithreadedSimpleXMLRPCServer(("127.0.0.1", 40405))
server.RequestHandlerClass.protocol_version = "HTTP/1.1"
server.register_function(set_title, "set_title")
server.register_function(get_title, "get_title")
server.register_function(get_message, "get_message")
server.register_function(send_message, "send_message")
server.register_function(get_measurement_value, "get_measurement_value")
server.register_function(get_measurement_value_test, "get_measurement_value_test")
server.register_function(motor_move, "motor_move")
server.register_function(motor_power, "motor_power")
server.register_function(motor_close, "motor_close")
server.register_function(motor_open, "motor_open")
server.serve_forever()
