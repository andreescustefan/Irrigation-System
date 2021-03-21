import sys
import urllib2
import RPi.GPIO as GPIO
from gpiozero import Button, OutputDevice
import time
import Adafruit_DHT
from gpiozero import LED

#Thingspeak
API_write = 'TABU7D2OUI5TVGG5'
baseURL = 'https://api.thingspeak.com/update?api_key=%s' % API_write

#wet = -1

#GPIO_SETUP
GPIO.setmode(GPIO.BCM)

soil = 17
GPIO.setup(soil, GPIO.IN)

relay = 23
GPIO.setup(relay, GPIO.OUT)
GPIO.output(relay, False)

#Soil_Moisture
def get_status():
    global soil
    if GPIO.input(soil):
        print("no water detected")
        pump(1)
        return 0
    else:
        print("water detected")
        pump(0)
        return 1

#DHT22
def DHT22_data():
    humi, temp = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, 16)
    print(humi)
    print(temp)
    return humi, temp

def pump(turn):
    global relay
    if turn == 1:
        GPIO.setup(relay, GPIO.OUT)
    else:
        GPIO.setup(relay, GPIO.IN)

while True:
    try:
        humi, temp = DHT22_data()
        wet = get_status()
        if isinstance(humi, float) and isinstance(temp, float):
            humi = '%.2f' % humi
            temp = '%.2f' % temp
            conn = urllib2.urlopen(baseURL + '&field1=%s&field2=%s&field3=%s' % (humi, temp, wet))
            conn.close()
        else:
            print ('Error')
            sleep(100)
    except:
        break

GPIO.cleanup()
