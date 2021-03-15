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

out = -1

#GPIO_SETUP
soil = 17
GPIO.setmode(GPIO.BCM)
GPIO.setup(soil, GPIO.IN)

relay = 15
GPIO.setup(relay, GPIO.OUT)

#Soil_Moisture
def measure():
    if GPIO.input(soil):
        print("no water detected")
        GPIO.output(relay, 1)
        return 0
    else:
        print("water detected")
        GPIO.output(relay, 0)
        GPIO.cleanup()
        return 1
        
#DHT22
def DHT22_data():
    humi, temp = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, 16)
    print(humi)
    print(temp)
    return humi, temp

while True:
    try:
        humi, temp = DHT22_data()
        out = measure()
        if isinstance(humi, float) and isinstance(temp, float):
            humi = '%.2f' % humi
            temp = '%.2f' % temp
            conn = urllib2.urlopen(baseURL + '&field1=%s&field2=%s&field3=%s' % (humi, temp, out))
            conn.close()
        else:
            print ('Error')
            sleep(100)
    except:
        break
GPIO.cleanup()