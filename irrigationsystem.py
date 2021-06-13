import sys
import urllib2
import json
import RPi.GPIO as GPIO
from gpiozero import Button, OutputDevice
import time
import Adafruit_DHT
from gpiozero import LED
import schedule
import time

#Thingspeak
API_write = 'TABU7D2OUI5TVGG5'
writeURL = 'https://api.thingspeak.com/update?api_key=%s' % API_write

API_read_schedule = '1H2NTVVL6RYRDV8Y&results=2'
readMoURL = 'https://api.thingspeak.com/channels/1396075/fields/1/last.json?api_key=%s' % API_read_schedule
readTuURL = 'https://api.thingspeak.com/channels/1396075/fields/2/last.json?api_key=%s' % API_read_schedule
readWeURL = 'https://api.thingspeak.com/channels/1396075/fields/3/last.json?api_key=%s' % API_read_schedule
readThURL = 'https://api.thingspeak.com/channels/1396075/fields/4/last.json?api_key=%s' % API_read_schedule
readFrURL = 'https://api.thingspeak.com/channels/1396075/fields/5/last.json?api_key=%s' % API_read_schedule
readSaURL = 'https://api.thingspeak.com/channels/1396075/fields/6/last.json?api_key=%s' % API_read_schedule
readSuURL = 'https://api.thingspeak.com/channels/1396075/fields/7/last.json?api_key=%s' % API_read_schedule
readConfigURL = 'https://api.thingspeak.com/channels/1396075/fields/8/last.json?api_key=%s' % API_read_schedule

API_read = 'GATCL5J9R1VLH7ML&results=2'
readmodeURL = 'https://api.thingspeak.com/channels/939407/fields/4/last.json?api_key=%s' % API_read
readpumpURL = 'https://api.thingspeak.com/channels/939407/fields/5/last.json?api_key=%s' % API_read
readstartURL = 'https://api.thingspeak.com/channels/939407/fields/6/last.json?api_key=%s' % API_read
readdurationURL = 'https://api.thingspeak.com/channels/939407/fields/7/last.json?api_key=%s' % API_read

#GPIO_SETUP
GPIO.setmode(GPIO.BCM)

soil = 17
GPIO.setup(soil, GPIO.IN)

relay = 15
GPIO.setup(relay, GPIO.OUT)
GPIO.output(relay, False)
GPIO.setup(relay, GPIO.IN)

#Global variables
mode_status = -1
pump_status = -1
ready = -1

#Soil_Moisture
def get_soil_status():
    global soil
    if GPIO.input(soil):
        print("no water detected")
        return 0
    else:
        print("water detected")
        return 1

#read mode data
def read_mode_data(url):
    get_data = urllib2.urlopen(url)
    data = get_data.read()
    get_data.close()
    result = data.find('field4')
    index = result+9
    if int(data[index]) == 0:
        print("Manual Mode")
    if int(data[index]) == 1:
        print("Auto Mode")
    if int(data[index]) == 2:
        print("Schedule Mode")
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

#read time data
def read_time_data(url, id):
    get_data = urllib2.urlopen(url)
    data = get_data.read()
    get_data.close()
    if id == 6:
        result = data.find('field6')
    if id == 7:
        result = data.find('field7')
    index = result+9
    index2 = data.find('"}')
    return str(data[index:index2])

#read schedule data
def read_schedule_data(url, id):
    get_data = urllib2.urlopen(url)
    data = get_data.read()
    get_data.close()
    if id == 1:
        result = data.find('field1')
    if id == 2:
        result = data.find('field2')
    if id == 3:
        result = data.find('field3')
    if id == 4:
        result = data.find('field4')
    if id == 5:
        result = data.find('field5')
    if id == 6:
        result = data.find('field6')
    if id == 7:
        result = data.find('field7')
    index = result+9
    return int(data[index]*1 + data[index+1])

#read ready data
def read_ready_data(url):
    get_data = urllib2.urlopen(url)
    data = get_data.read()
    get_data.close()
    result = data.find('field8')
    index = result+9
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
#job
def job_on():
    pump(1)
    
def job_off():
    pump(0)
    
#Auto/Manual/Schedule
def mode(turn):
    global pump_status
    global out
    if turn == 0:
        pump_status = read_pump_data(readpumpURL)
        if pump_status == 0:
            pump(0)
        else:
            pump(1)
    if turn == 1:
        if out == 0:
            pump(1)
        else:
            pump(0)
    if turn == 2:
        if read_ready_data(readConfigURL) == 1:
            startHour = read_time_data(readstartURL, 6)
            durationHour = read_time_data(readdurationURL, 7)
            print(startHour + ":" + durationHour)
            if read_schedule_data(readMoURL,1) == 10:
                schedule.every().monday.at("00:00").do(job_off)
                print("Mo: OFF")
            if read_schedule_data(readMoURL,1) == 11:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().monday.at(startHour+":00").do(job_on)
                    schedule.every().monday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().monday.at("0"+startHour+":00").do(job_on)
                    schedule.every().monday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().monday.at(startHour+":00").do(job_on)
                    schedule.every().monday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().monday.at("0"+startHour+":00").do(job_on)
                    schedule.every().monday.at("0"+startHour+":"+durationHour).do(job_off)
                print("Mo: ON")
            if read_schedule_data(readTuURL,2) == 20:
                schedule.every().tuesday.at("00:00").do(job_off)
                print("Tu: OFF")
            if read_schedule_data(readTuURL,2) == 21:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().tuesday.at(startHour+":00").do(job_on)
                    schedule.every().tuesday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().tuesday.at("0"+startHour+":00").do(job_on)
                    schedule.every().tuesday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().tuesday.at(startHour+":00").do(job_on)
                    schedule.every().tuesday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().tuesday.at("0"+startHour+":00").do(job_on)
                    schedule.every().tuesday.at("0"+startHour+":"+durationHour).do(job_off)
                print("Tu: ON")
            if read_schedule_data(readWeURL,3) == 30:
                schedule.every().wednesday.at("00:00").do(job_off)
                print("We: OFF")
            if read_schedule_data(readWeURL,3) == 31:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().wednesday.at(startHour+":00").do(job_on)
                    schedule.every().wednesday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().wednesday.at("0"+startHour+":00").do(job_on)
                    schedule.every().wednesday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().wednesday.at(startHour+":00").do(job_on)
                    schedule.every().wednesday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().wednesday.at("0"+startHour+":00").do(job_on)
                    schedule.every().wednesday.at("0"+startHour+":"+durationHour).do(job_off)
                print("We: ON")
            if read_schedule_data(readThURL,4) == 40:
                schedule.every().thursday.at("00:00").do(job_off)
                print("Th: OFF")
            if read_schedule_data(readThURL,4) == 41:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().thursday.at(startHour+":00").do(job_on)
                    schedule.every().thursday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().thursday.at("0"+startHour+":00").do(job_on)
                    schedule.every().thursday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().thursday.at(startHour+":00").do(job_on)
                    schedule.every().thursday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().thursday.at("0"+startHour+":00").do(job_on)
                    schedule.every().thursday.at("0"+startHour+":"+durationHour).do(job_off)
                print("Th: ON")
            if read_schedule_data(readFrURL,5) == 50:
                schedule.every().friday.at("00:00").do(job_off)
                print("Fr: OFF")
            if read_schedule_data(readFrURL,5) == 51:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().friday.at(startHour+":00").do(job_on)
                    schedule.every().friday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().friday.at("0"+startHour+":00").do(job_on)
                    schedule.every().friday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().friday.at(startHour+":00").do(job_on)
                    schedule.every().friday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().friday.at("0"+startHour+":00").do(job_on)
                    schedule.every().friday.at("0"+startHour+":"+durationHour).do(job_off)
                print("Fr: ON")
            if read_schedule_data(readSaURL,6) == 60:
                schedule.every().saturday.at("00:00").do(job_off)
                print("Sa: OFF")
            if read_schedule_data(readSaURL,6) == 61:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().saturday.at(startHour+":00").do(job_on)
                    schedule.every().saturday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().saturday.at("0"+startHour+":00").do(job_on)
                    schedule.every().saturday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().saturday.at(startHour+":00").do(job_on)
                    schedule.every().saturday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().saturday.at("0"+startHour+":00").do(job_on)
                    schedule.every().saturday.at("0"+startHour+":"+durationHour).do(job_off)
                print("Sa: ON")
            if read_schedule_data(readSuURL,7) == 70:
                schedule.every().sunday.at("00:00").do(job_off)
                print("Su: OFF")
            if read_schedule_data(readSuURL,7) == 71:
                if int(startHour) > 9 and int(durationHour) > 9:
                    schedule.every().sunday.at(startHour+":00").do(job_on)
                    schedule.every().sunday.at(startHour+":"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) < 10:
                    schedule.every().sunday.at("0"+startHour+":00").do(job_on)
                    schedule.every().sunday.at("0"+startHour+":0"+durationHour).do(job_off)
                if int(startHour) > 9 and int(durationHour) < 10:
                    schedule.every().sunday.at(startHour+":00").do(job_on)
                    schedule.every().sunday.at(startHour+":0"+durationHour).do(job_off)
                if int(startHour) < 10 and int(durationHour) > 9:
                    schedule.every().sunday.at("0"+startHour+":00").do(job_on)
                    schedule.every().sunday.at("0"+startHour+":"+durationHour).do(job_off)
                print("Su: ON")

while True:
    try:
        out = get_soil_status()
        mode_status = read_mode_data(readmodeURL)
        humi, temp = DHT22_data()
        if isinstance(humi, float) and isinstance(temp, float):
            humi = '%.2f' % humi
            temp = '%.2f' % temp
            conn = urllib2.urlopen(writeURL + '&field1=%s&field2=%s&field3=%s' % (humi, temp, out))
            conn.close()
        else:
            print ('Error')
            time.sleep(20)
        if mode_status == 0:
            mode(0)
        if mode_status == 1:
            mode(1)
        if mode_status == 2:
            mode(2)
            schedule.run_pending()
            time.sleep(1)
        time.sleep(20)
    except:
        break

GPIO.cleanup()

