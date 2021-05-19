import sys
import urllib2
import json
import RPi.GPIO as GPIO
from gpiozero import Button, OutputDevice
import time
import Adafruit_DHT
from gpiozero import LED
import time

#Thingspeak
API_write = 'TABU7D2OUI5TVGG5'
writeURL = 'https://api.thingspeak.com/update?api_key=%s' % API_write

API_read = 'GATCL5J9R1VLH7ML&results=2'
readautoURL = 'https://api.thingspeak.com/channels/939407/fields/4/last.json?api_key=%s' % API_read
readpumpURL = 'https://api.thingspeak.com/channels/939407/fields/5/last.json?api_key=%s' % API_read

#GPIO_SETUP
GPIO.setmode(GPIO.BCM)

soil = 17
GPIO.setup(soil, GPIO.IN)

relay = 15
GPIO.setup(relay, GPIO.OUT)
GPIO.output(relay, False)

#Global variables
auto_status = -1
pump_status = -1

#Soil_Moisture
def get_soil_status():
    global soil
    if GPIO.input(soil):
        print("no water detected")
        return 0
    else:
        print("water detected")
        return 1

#read auto data
def read_auto_data(url):
    get_data = urllib2.urlopen(url)
    data = get_data.read()
    get_data.close()
    result = data.find('field4')
    index = result+9
    if int(data[index]) == 0:
        print("Auto: OFF")
    else:
        print("Auto: ON")
    return int(data[index])

#read pump data
def read_pump_data(url):
    get_data = urllib2.urlopen(url)
    data = get_data.read()
    get_data.close()
    result = data.find('field5')
    index = result+9
    if int(data[index]) == 0:
        print("Pump: OFF")
    else:
        print("Pump: ON")
    return int(data[index])


#DHT22
def DHT22_data():
    humi, temp = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, 16)
    print(humi)
    print(temp)
    return humi, temp

#Pump
def pump(turn):
    global relay
    if turn == 1:
        GPIO.setup(relay, GPIO.OUT)
    else:
        GPIO.setup(relay, GPIO.IN)

#Auto/Manual
def auto(turn):
    global pump_status
    global out
    if turn == 0:
        pump_status = read_pump_data(readpumpURL)
        if pump_status == 0:
            pump(0)
        else:
            pump(1)
    else:
        if out == 0:
            pump(1)
        else:
            pump(0)

while True:
    try:
        out = get_soil_status()
        auto_status = read_auto_data(readautoURL)
        humi, temp = DHT22_data()
        if isinstance(humi, float) and isinstance(temp, float):
            humi = '%.2f' % humi
            temp = '%.2f' % temp
            conn = urllib2.urlopen(writeURL + '&field1=%s&field2=%s&field3=%s' % (humi, temp, out))
            conn.close()
        else:
            print ('Error')
            time.sleep(15)
        if auto_status == 0:
            auto(0)
        else:
            auto(1)
        time.sleep(15)
    except:
        break

GPIO.cleanup()

