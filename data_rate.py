import os
import re

f = open('testAcrossRoom.txt', 'r')

contents = f.read()
rate = re.compile('Data Rate: [0-9]*\.[0-9]')
dataRate = re.compile('[0-9]+ bytes on wire')
allRates = rate.findall(contents)
allData = dataRate.findall(contents)

l =[]
p = []
print len(allRates)

for x in range(len(allRates)):
    for t in allRates[x].split():
        try:
            l.append(float(t))
        except ValueError:
            pass


for x in range(len(allData)):
    for t in allData[x].split():
        try:
            p.append(float(t))
        except ValueError:
            pass

print "Total data transmitted"
print sum(p)/(1024*1024)

print "Average data rate"
print sum(l)/float(len(l))
