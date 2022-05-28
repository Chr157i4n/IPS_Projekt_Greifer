#!/usr/bin/env python

# RS485 device:  /dev/ttyTool

import sys

from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn
import serial
import time


def log(message):
	sys.stdout.write(message+"\n")
	sys.stderr.write(message+"\n")
	print(message+"\n")


title = ""
port = "/dev/ttyTool"
KEEPALIVE = "M113"

counter = 0

try:
	ser = serial.Serial (port=port, baudrate=115200, parity=serial.PARITY_NONE, bytesize=8, stopbits=1, timeout=2.0, write_timeout=2.0,)
except:
	log("could not open port: "+port)
	ser = None

def init():
	pass

def read_answer():
	buffer=KEEPALIVE
	while(buffer == KEEPALIVE):
		buffer = serial.readline()
		log(":"+str(buffer))
	if(buffer==""):
		log("NO ANSWER")
	if(buffer[0]=="E"):
		log("ERROR")
	return buffer
	
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
		if(ser==None):
			return "back: " + message
		else:
			ser.reset_input_buffer()
			ser.write((message + "\n").encode())
			value = ser.readline()
			return value
	else:
		return "Error: No message set."

def get_measurement_value_test(channel):
	global counter

	log("Test Measurement from channel: "+channel+" requested")
	counter += 1
	return str(counter)

def get_measurement_value(channel):
	log("Measurement from channel: "+channel+" requested")
	serial.write("M43 P"+str(channel))
	value = read_answer()
	return value[1:]

def motor_move(distance):
	log("Move Motor "+str(distance)+" mm")
	serial.write("G0 X"+str(distance))
	value = read_answer()
	return value[1:]

def motor_power(enable):
	log("Motor Power: "+str(enable))
	serial.write("M1"+str(8-int(enable)))
	value = read_answer()
	return value[1:]

def motor_close(force):
	log("Motor Close: "+str(force))
	serial.write("G2 "+str(force))
	value = read_answer()
	return value[1:]

def motor_open():
	log("Motor Open")
	serial.write("G3")
	value = read_answer()
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
