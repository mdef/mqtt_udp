#!/usr/bin/env python3

'''
Created on 24.12.2017

@author: dz

Listen to traffic generated by seq_storm_send.py, calc speed and error rate
'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import time

import mqttudp.engine



SEQ_STORM_TOPIC="sequential_storm"
STEP=10000





def recv_packet_from_udp(ptype,topic,value,pflags,addr):
    global last, errors, got, curr, start_time

    if ptype != "publish":
        return

    if topic != SEQ_STORM_TOPIC:
        return

# report
    if (last % STEP) == 0:
        now = time.clock();
        speed_s = "?"
        if now != start_time:
            speed_s = '{:.0f}'.format( STEP/(now-start_time) )
            err_percent_s = '{:.1f}'.format( errors*100.0/STEP )
            print("@ "+str(last)+"\terrors = "+str(errors)+"\tspeed is "+speed_s+" pkt/sec,\terr "+err_percent_s+"%" )
            errors = 0
        start_time = now

    curr = int( value )

    got = got + 1

    if curr == last:
        last = last + 1
        return

    errors = errors + 1
    last = curr + 1



if __name__ == "__main__":
    global last, errors, got, curr, start_time
    

    print( "Will listen for MQTT/UDP packets with sequential number as a payload, topic is '"+SEQ_STORM_TOPIC+"'" )
    print( "\nStart seq_storm_send now...")

    start_time = time.clock();

    last = 0
    errors = 0
    got = 0
    curr = 0

    mqttudp.engine.listen(recv_packet_from_udp)













